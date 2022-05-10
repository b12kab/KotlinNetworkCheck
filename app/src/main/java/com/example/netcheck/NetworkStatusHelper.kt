package com.example.netcheck

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import java.net.Socket

// https://betterprogramming.pub/how-to-monitor-internet-connection-in-android-using-kotlin-and-livedata-135de9447796

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}

class NetworkStatusHelper(private val context: Context) : LiveData<NetworkStatus>() {
    private val TAG = this::class.java.simpleName

    var valideNetworkConnections = mutableListOf<Network>()
    var connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    fun announceStatus() {
        if (valideNetworkConnections.isNotEmpty()){
            postValue(NetworkStatus.Available)
        } else {
            postValue(NetworkStatus.Unavailable)
        }
    }

    fun getConnectivityManagerCallback() =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.e(TAG, "onAvailable()")
                val networkCapability = connectivityManager.getNetworkCapabilities(network)
                val hasNetworkConnection = networkCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)?:false
                if (hasNetworkConnection) {
                    determineInternetAccess(network)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.e(TAG, "onLost()")
                removeNetwork(network)
                announceStatus()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                Log.e(TAG, "onCapabilitiesChanged()")
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    determineInternetAccess(network)
                } else {
                    removeNetwork(network)
                }
                announceStatus()
            }
        }

    private fun addNetwork(network: Network) {
        if (!valideNetworkConnections.contains(network)) {
            valideNetworkConnections.add(network)
        }
    }

    private fun removeNetwork(network: Network) {
        valideNetworkConnections.remove(network)
    }

    private fun determineInternetAccess(network: Network) {
        runBlocking(Dispatchers.IO) {
            val working = InernetAvailablity.check()
            Log.e(TAG, "Internet check: $working")
            if (working) {
                withContext(Dispatchers.Main){
                    addNetwork(network)
                    announceStatus()
                }
            }
        }
    }

    override fun onActive() {
        super.onActive()
        Log.e(TAG, "onActive()")
        connectivityManagerCallback = getConnectivityManagerCallback()
        val networkRequest = NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, connectivityManagerCallback)
    }

    override fun onInactive() {
        super.onInactive()
        Log.e(TAG, "onInactive()")
        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    }

}

object InernetAvailablity {
    fun check() : Boolean {
        return try {
            val socket = Socket("1.1.1.1",53)
            socket.outputStream.write(0)
            socket.close()
            true
        } catch ( e: Exception){
            false
        }
    }
}