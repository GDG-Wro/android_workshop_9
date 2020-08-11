package com.example.androidworkshop9.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidworkshop9.R
import com.example.androidworkshop9.bluetoothDisconnects
import com.example.androidworkshop9.bluetoothStateChanges
import com.example.androidworkshop9.model.Message
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import ru.ldralighieri.corbind.widget.editorActions
import java.util.*

class ChatFragment : Fragment(R.layout.chat_fragment) {
    private val chatAdapter = ChatAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        var device = arguments?.getParcelable<BluetoothDevice>("BluetoothDevice")
        name.text = if (device == null) "Waiting for other device..." else "Connecting..."
        viewLifecycleOwner.lifecycleScope.launch {
            val socket = getSocket(device)
            name.text = socket.remoteDevice.name
            device = socket.remoteDevice
            input.isEnabled = true

            input.editorActions()
                .filter { it == EditorInfo.IME_ACTION_SEND }
                .map {
                    runCatching {
                        val message = input.text.toString().toByteArray()
                        socket.outputStream.write(message.size)
                        socket.outputStream.write(message)
                        return@runCatching input.text.toString()
                    }.getOrNull() ?: "⚠️ Failed to send message"
                }
                .flowOn(Dispatchers.IO)
                .onEach {
                    input.setText("")
                    chatAdapter.addMessage(Message(it, Message.Aligment.End))
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
            socket.messages()
                .catch { closeChat(it) }
                .onEach { chatAdapter.addMessage(Message(it, Message.Aligment.Start)) }
                .onCompletion { socket.close() }
                .launchIn(viewLifecycleOwner.lifecycleScope)
            requireContext().bluetoothDisconnects()
                .filter { it == device }
                .take(1)
                .onEach { closeChat() }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
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

    private fun closeChat(throwable: Throwable? = null) {
        if (throwable != null) Log.e("BT", "Receive error", throwable)
        chatAdapter.addMessage(Message("Chat ended", Message.Aligment.Center))
        input.isEnabled = false
    }

    private suspend fun getSocket(device: BluetoothDevice?): BluetoothSocket =
        withContext(Dispatchers.IO) {
            return@withContext if (device != null) {
                device.createRfcommSocketToServiceRecord(socketUuid).apply { connect() }
            } else {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                Log.e("BT", "Accepting connections")
                val socket = adapter.listenUsingRfcommWithServiceRecord("Messages", socketUuid)
                    .use { it.accept() }
                Log.e("BT", "Accepted")
                socket
            }
        }

    private fun BluetoothSocket.messages() = flow {
        while (true) {
            runCatching {
                val length = inputStream.read()
                if (length > 0) {
                    val messageBytes = ByteArray(length)
                    inputStream.read(messageBytes)
                    emit(messageBytes.decodeToString())
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private val socketUuid = UUID.fromString("b095e084-69b8-41c1-b71e-7670ca834f50")

        fun newInstance(bluetoothDevice: BluetoothDevice) = ChatFragment().apply {
            arguments = bundleOf("BluetoothDevice" to bluetoothDevice)
        }

        fun newInstance() = ChatFragment()
    }
}
