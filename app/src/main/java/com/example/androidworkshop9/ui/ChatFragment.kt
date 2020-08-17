package com.example.androidworkshop9.ui

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidworkshop9.R
import kotlinx.android.synthetic.main.chat_fragment.*
import java.util.*

class ChatFragment : Fragment(R.layout.chat_fragment) {
    private val chatAdapter = ChatAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
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

    companion object {
        private val socketUuid = UUID.fromString("b095e084-69b8-41c1-b71e-7670ca834f50")

        fun newInstance(bluetoothDevice: BluetoothDevice) = ChatFragment().apply {
            arguments = bundleOf("BluetoothDevice" to bluetoothDevice)
        }

        fun newInstance() = ChatFragment()
    }
}
