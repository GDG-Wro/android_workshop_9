package com.example.androidworkshop9.ui.devices

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidworkshop9.R
import com.example.androidworkshop9.model.Device
import com.example.androidworkshop9.ui.ChatFragment
import kotlinx.android.synthetic.main.devices_fragment.*

class DevicesFragment : Fragment(R.layout.devices_fragment) {

    private val devicesAdapter = DeviceAdapter(::onDeviceClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        with(devices) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
        }
    }


    private fun onDeviceClicked(device: Device) {
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, ChatFragment.newInstance(device.bluetoothDevice))
            .commit()
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }
}

