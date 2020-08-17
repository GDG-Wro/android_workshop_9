package com.example.androidworkshop9.model

import android.bluetooth.BluetoothDevice

data class Device(
    val name: String?,
    val address: String,
    val state: Int,
    val bluetoothDevice: BluetoothDevice
)
