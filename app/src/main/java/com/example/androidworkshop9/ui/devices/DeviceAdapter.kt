package com.example.androidworkshop9.ui.devices

import android.bluetooth.BluetoothDevice.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidworkshop9.R
import com.example.androidworkshop9.model.Device
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.device_item.*

class DeviceAdapter(
    private val onDeviceClick: (Device) -> Unit
) : ListAdapter<Device, DeviceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view, onDeviceClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.bluetoothDevice == newItem.bluetoothDevice
        }

        override fun areContentsTheSame(
            oldItem: Device,
            newItem: Device
        ): Boolean {
            return oldItem == newItem
        }
    }
}

class DeviceViewHolder(
    override val containerView: View,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    lateinit var device: Device

    init {
        containerView.setOnClickListener { onDeviceClick(device) }
    }


    fun bind(device: Device) {
        this.device = device
        name.text = device.name
        address.text = device.address
        state.text = when (device.state) {
            BOND_NONE -> "Not bonded"
            BOND_BONDING -> "Bonding..."
            BOND_BONDED -> "Bonded"
            else -> error("Unsupported bond state")
        }
    }
}
