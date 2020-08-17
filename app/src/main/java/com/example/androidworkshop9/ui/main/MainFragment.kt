package com.example.androidworkshop9.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.androidworkshop9.R
import com.example.androidworkshop9.isMissingLocationPermission
import com.example.androidworkshop9.ui.LogFragment
import com.example.androidworkshop9.ui.devices.DevicesFragment
import kotlinx.android.synthetic.main.main_fragment.*

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
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}

