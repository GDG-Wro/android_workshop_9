package com.example.androidworkshop9

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.androidworkshop9.ui.devices.DevicesFragment
import com.example.androidworkshop9.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DevicesFragment.newInstance())
                .commitNow()
        }
    }
}
