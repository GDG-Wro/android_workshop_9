package com.example.androidworkshop9

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Context.bluetoothStateChanges() = callbackFlow<Int> {
    val broadcatReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val element =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                offer(element)
            }
        }

    }
    offer(BluetoothAdapter.getDefaultAdapter().state)
    registerReceiver(broadcatReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    awaitClose { unregisterReceiver(broadcatReceiver) }
}

fun Context.bluetoothDisconnects() = callbackFlow<BluetoothDevice> {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_ACL_DISCONNECTED || intent.action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) {
                offer(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!)
            }
        }
    }
    registerReceiver(receiver, IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    })
    awaitClose { unregisterReceiver(receiver) }
}

fun Context.isMissingLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
}
