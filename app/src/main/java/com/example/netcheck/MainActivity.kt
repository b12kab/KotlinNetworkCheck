package com.example.netcheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.netcheck.data.NetworkResult
import com.example.netcheck.data.model.QuoteList
import com.example.netcheck.databinding.ActivityMainBinding
import com.example.netcheck.extension.isConnected

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName

    private lateinit var viewModel: NetworkViewModel
    private lateinit var binding: ActivityMainBinding

    private lateinit var networkStatus: TextView
    private lateinit var networkActivity: TextView
    var changeCnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val repository = QuoteRepository()
        val factory = NetworkViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(NetworkViewModel::class.java)

//        setContentView(R.layout.activity_main)
//        tv = findViewById(R.id.status)
        networkStatus = binding.status
        networkActivity = binding.networkActivity
        binding.viewmodel = viewModel

        NetworkStatusHelper(this).observe(this, {
            changeCnt++
            val status = it == NetworkStatus.Available
            Log.e(TAG, "network change count: $changeCnt status: $status")

            when(it) {
                NetworkStatus.Available -> setNetworkText(true)
                NetworkStatus.Unavailable -> setNetworkText(false)
            }
        })

        viewModel.networkEnabled = this.isConnected
        setNetworkText(viewModel.networkEnabled)
        Log.e(TAG, "initial network setting - enabled: ${this.isConnected}")

        viewModel.networkResults.observe(this, {
            Log.e(TAG, "observing network data: ${it}")
            if (it is NetworkResult.Success) {
                val result = it.data as QuoteList
                val elements = result.count
                networkActivity.text = "Success - quotes returned: $elements"
            } else if (it is NetworkResult.Failure) {
                val result = it.failure
                networkActivity.text = "Failure - http status: ${result.httpStatusCode}"
            } else if (it is NetworkResult.Loading) {
                val result = it.loading
                if (result) {
                    networkActivity.text = "Loading - started"
                } else {
                    networkActivity.text = "Loading - ended"
                }
            } else if (it is NetworkResult.Exception) {
                val ex = it.throwable
                networkActivity.text = "Exception - ${ex.message}"
            }
        })

        if (viewModel.networkEnabled) {
            Log.e(TAG, "network enabled - before initNetworkStart")
            viewModel.initNetworkStart()
            Log.e(TAG, "network enabled - after initNetworkStart")
            val job = viewModel.getJob()
//            Log.e(TAG, "network enabled - after getJob - ${job?.isActive}")
//            if (job != null) {
//                job.isActive
//            }
            viewModel.startLiveDataCollection()
        }
    }

    fun setNetworkText(enabled: Boolean) {
        if (enabled) {
            networkStatus.text = this.getString(R.string.network_enabled)
        } else {
            networkStatus.text = this.getString(R.string.network_disabled)
        }
    }
}