package com.example.androidworkshop9.ui.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidworkshop9.R
import com.example.androidworkshop9.bluetoothStateChanges
import com.example.androidworkshop9.model.Device
import com.example.androidworkshop9.ui.LogFragment
import com.movisens.smartgattlib.Services
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
        val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O || result.isConnectable) {
                    offer(result.device)
                }
            }
        }

        scanner.startScan(
            listOf(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(Services.HEART_RATE.uuid))
                    .build()
            ),
            ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).build(),
            callback
        )
        scan.isEnabled = false
        Log.e("Scan", "Start scanning")
        awaitClose {
            scanner.stopScan(callback)
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
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, LogFragment.newInstance(device.bluetoothDevice))
            .commit()
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }
}

