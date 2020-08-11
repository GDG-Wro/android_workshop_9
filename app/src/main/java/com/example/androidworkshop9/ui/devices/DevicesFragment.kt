package com.example.androidworkshop9.ui.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidworkshop9.R
import com.example.androidworkshop9.bluetoothStateChanges
import com.example.androidworkshop9.model.Device
import com.example.androidworkshop9.ui.ChatFragment
import kotlinx.android.synthetic.main.devices_fragment.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.ldralighieri.corbind.view.clicks

class DevicesFragment : Fragment(R.layout.devices_fragment) {

    private val devicesAdapter = DeviceAdapter(::onDeviceClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startBluetooth()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        with(devices) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
        }
    }

    private fun startBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothSwitch.isEnabled = true
        bluetoothSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (bluetoothAdapter.isEnabled == isChecked) return@setOnCheckedChangeListener

            if (isChecked) bluetoothAdapter.enable()
            else bluetoothAdapter.disable()
        }
        requireContext().bluetoothStateChanges()
            .onEach { invalidateBluetoothSwitch(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launch {
            scan.clicks()
                .flatMapLatest { scan() }
                .collect { devicesAdapter.submitList(it.toList()) }
        }
    }

    private suspend fun scan() = callbackFlow<BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothDevice.ACTION_FOUND || intent.action == ACTION_BOND_STATE_CHANGED) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                    offer(device)
                } else if (intent.action == ACTION_DISCOVERY_FINISHED) {
                    scan.isEnabled = bluetoothAdapter.isEnabled
                }
            }
        }
        requireContext().registerReceiver(receiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(ACTION_BOND_STATE_CHANGED)
            addAction(ACTION_DISCOVERY_FINISHED)
        })
        bluetoothAdapter.startDiscovery()
        scan.isEnabled = false
        Log.e("Scan", "Start scanning")
        awaitClose {
            bluetoothAdapter.cancelDiscovery()
            requireContext().unregisterReceiver(receiver)
            Log.e("Scan", "Stop scanning")
        }
    }.map { Device(it.name, it.address, it.bondState, it) }
        .scan(emptyList<Device>()) { list, device -> (listOf(device) + list).distinctBy { it.name to it.address } }

    private fun invalidateBluetoothSwitch(state: Int) {
        scan.isEnabled = state == BluetoothAdapter.STATE_ON
        when (state) {
            BluetoothAdapter.STATE_OFF -> {
                bluetoothSwitch.isChecked = false
                bluetoothSwitch.isEnabled = true
            }
            BluetoothAdapter.STATE_ON -> {
                bluetoothSwitch.isEnabled = true
                bluetoothSwitch.isChecked = true
            }
            BluetoothAdapter.STATE_TURNING_OFF,
            BluetoothAdapter.STATE_TURNING_ON -> bluetoothSwitch.isEnabled = false
            else -> error("Unsupported bluetooth state")
        }
    }

    private fun onDeviceClicked(device: Device) {
        if (device.bluetoothDevice.bondState == BOND_NONE) {
            device.bluetoothDevice.createBond()
        } else {
            requireActivity().supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, ChatFragment.newInstance(device.bluetoothDevice))
                .commit()
        }
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }
}

