package com.example.androidworkshop9.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.androidworkshop9.R
import com.example.androidworkshop9.ui.ChatFragment
import com.example.androidworkshop9.ui.devices.DevicesFragment
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment(R.layout.main_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

