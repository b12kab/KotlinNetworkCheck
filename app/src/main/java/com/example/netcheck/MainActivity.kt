package com.example.netcheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var tv: TextView
    var changeCnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv = findViewById(R.id.status)

        NetworkStatusHelper(this@MainActivity).observe(this, {
            changeCnt++
            val status = it == NetworkStatus.Available
            Log.e(TAG, "network change count: $changeCnt status: $status")

            tv.text = when(it) {
                NetworkStatus.Available -> "Network Connection Established"
                NetworkStatus.Unavailable -> "No Internet"
            }
        })
    }
}