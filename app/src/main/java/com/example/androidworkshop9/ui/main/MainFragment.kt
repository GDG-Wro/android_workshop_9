package com.example.androidworkshop9.ui.main

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.androidworkshop9.R
import com.example.androidworkshop9.isMissingLocationPermission
import com.example.androidworkshop9.ui.ChatFragment
import com.example.androidworkshop9.ui.devices.DevicesFragment
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import ru.ldralighieri.corbind.widget.checkedChanges
import java.util.*

class MainFragment : Fragment(R.layout.main_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireContext().isMissingLocationPermission()) {
            Toast.makeText(requireContext(), "Missing location permission", Toast.LENGTH_LONG)
                .show()
            requireActivity().finish()
        }
        showDevices.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, DevicesFragment.newInstance())
                .commit()
        }
        createServer.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, ChatFragment.newInstance())
                .commit()
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}

