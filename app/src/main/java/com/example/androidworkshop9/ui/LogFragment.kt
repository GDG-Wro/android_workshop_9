package com.example.androidworkshop9.ui

import android.bluetooth.*
import android.bluetooth.BluetoothDevice.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidworkshop9.R
import com.example.androidworkshop9.model.Message
import com.movisens.smartgattlib.Characteristics
import com.movisens.smartgattlib.Services
import com.movisens.smartgattlib.attributes.HeartRateMeasurement
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume

class LogFragment : Fragment(R.layout.chat_fragment) {
    private val chatAdapter = ChatAdapter()
    private var bluetoothGatt: BluetoothGatt? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        val device = requireArguments().getParcelable<BluetoothDevice>("BluetoothDevice")!!
        name.text = Services.HEART_RATE.name
        viewLifecycleOwner.lifecycleScope.launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        val isConnected = when (newState) {
                            BluetoothProfile.STATE_DISCONNECTED -> false
                            BluetoothProfile.STATE_CONNECTED -> true
                            else -> error("Unsupported state")
                        }
                        val value: String
                        if (isConnected) {
                            value = "Connected"
                            gatt.discoverServices()
                        } else {
                            value = "Disconnected"
                        }
                        chat.post { chatAdapter.addMessage(Message(value, Message.Aligment.Center)) }
                    }

                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic
                    ) {
                        if (characteristic.uuid == Characteristics.HEART_RATE_MEASUREMENT.uuid) {
                            val flags = characteristic.properties
                            val format = when (flags and 0b1) {
                                0b1 -> BluetoothGattCharacteristic.FORMAT_UINT16
                                else -> BluetoothGattCharacteristic.FORMAT_UINT8
                            }
                            val value = characteristic.getIntValue(format, 1)
                            chat.post { chatAdapter.addMessage(Message(value.toString(), Message.Aligment.Start)) }
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        if (status == GATT_SUCCESS && continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                })
            }
            val characteristic = bluetoothGatt!!.getService(Services.HEART_RATE.uuid)
                .getCharacteristic(Characteristics.HEART_RATE_MEASUREMENT.uuid)
            bluetoothGatt!!.setCharacteristicNotification(characteristic, true)
        }
    }

    private fun setUpRecyclerView() = with(chat) {
        adapter = chatAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                layoutManager!!.scrollToPosition(positionStart)
            }
        })
    }

    override fun onDestroyView() {
        val characteristic = bluetoothGatt?.getService(Services.HEART_RATE.uuid)
            ?.getCharacteristic(Characteristics.HEART_RATE_MEASUREMENT.uuid)
        if (characteristic != null) {
            bluetoothGatt?.setCharacteristicNotification(characteristic, false)
        }
        super.onDestroyView()
    }

    companion object {
        fun newInstance(bluetoothDevice: BluetoothDevice) = LogFragment().apply {
            arguments = bundleOf("BluetoothDevice" to bluetoothDevice)
        }
    }
}
