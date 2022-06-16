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
import kotlinx.coroutines.isActive

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

        networkStatus = binding.status
        networkActivity = binding.networkActivity
        binding.viewmodel = viewModel

        NetworkStatusHelper(this).observe(this, {
            changeCnt++
            val status = it == NetworkStatus.Available
            Log.e(TAG, "onCreate - NetworkStatusHelper observe - network change count: $changeCnt status: $status")
            Log.e(TAG,"onCreate - job: ${viewModel.getJob()} job started: ${viewModel.getJob()?.start()}, active: ${viewModel.getJob()?.isActive}, completed: ${viewModel.getJob()?.isCompleted}, cancelled: ${viewModel.getJob()?.isCancelled}")
            Log.e(TAG,"onCreate - scope: ${viewModel.workerScope}  scope active: ${viewModel.workerScope.isActive}")

            when(it) {
                NetworkStatus.Available -> {
                    setNetworkText(true)
                }
                NetworkStatus.Unavailable -> {
                    setNetworkText(false)
                    if (viewModel.getDataFetchStarted() &&
                        !viewModel.getDataFetchCompleted() &&
                        !viewModel.getDataFetchNetworkCompleted()
                    ) {
                        viewModel.cancelJob()
                    }
                }
            }
        })

        viewModel.networkEnabled = this.isConnected
        setNetworkText(viewModel.networkEnabled)
        Log.e(TAG, "onCreate - initial network setting - enabled: ${this.isConnected}")

        viewModel.networkResults.observe(this, {
            Log.e(TAG, "onCreate - networkResults - observing network data: ${it}")
            if (it is NetworkResult.Success) {
                val result = it.data as QuoteList
                val myPage = result.page
                val elements = result.count
                networkActivity.text = "OK - pg: $myPage - # $elements"
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
            Log.e(TAG, "onCreate - network enabled - before startLiveDataCollection")
            viewModel.startLiveDataCollection()
            Log.e(TAG, "onCreate - network enabled - before initNetworkStart")
            viewModel.initRepositoryFetchStart()
            Log.e(TAG, "onCreate - network enabled - after initNetworkStart")
            val job = viewModel.getJob()
            Log.e(TAG, "onCreate - job started: ${job?.start()}, active: ${job?.isActive}, completed ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
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