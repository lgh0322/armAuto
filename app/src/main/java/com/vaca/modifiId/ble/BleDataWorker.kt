package com.vaca.modifiId.ble


import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver


class BleDataWorker(val comeData: BleDataManager.OnNotifyListener) {
    private var pool: ByteArray? = null

    private val dataScope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private val connectChannel = Channel<String>(Channel.CONFLATED)
    private var bleDataManager: BleDataManager? = null

    val fileChannel = Channel<BleCallBack>(Channel.CONFLATED)

    val sendChannel = Channel<ByteArray>(Channel.CONFLATED)

    enum class BleCallBack {
        START, BODY, END, OTHER
    }

    val startPackage = byteArrayOf(0x0e.toByte(), 0x02.toByte(), 0x16.toByte(), 0x00.toByte())
    val bodyPackage = byteArrayOf(0x0e.toByte(), 0x02.toByte(), 0x17.toByte(), 0x00.toByte())
    val endPackage = byteArrayOf(0x0e.toByte(), 0x02.toByte(), 0x18.toByte(), 0x00.toByte())


    private val connectState = object : ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {

        }

        override fun onDeviceConnected(device: BluetoothDevice) {


        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {

        }

        override fun onDeviceReady(device: BluetoothDevice) {

        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {

        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {

        }

    }

    fun equalPkg(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }

        for (k in a.indices) {
            if (a[k] != b[k]) {
                return false
            }
        }
        return true
    }







    fun sendCmd(bs: ByteArray) {
        bleDataManager?.sendCmd(bs)
    }



    val MAX_TRANS_COUNT = 15
    val CMD_FW_WRITE: Byte = 0x17.toByte()




    private suspend fun writeId(byteArray: ByteArray) {
        mutex.withLock {
            sendCmd(byteArray)
        }
    }











    fun initWorker(context: Context, bluetoothDevice: BluetoothDevice?) {
        bleDataManager = BleDataManager(context)
        bleDataManager?.setNotifyListener(comeData)
        bleDataManager?.setConnectionObserver(connectState)
        bluetoothDevice?.let {
            bleDataManager?.connect(it)
                    ?.useAutoConnect(true)
                    ?.timeout(10000)
                    ?.retry(85, 100)
                    ?.done {
                        Log.i("BLE", "连接成功了.>>.....>>>>")
                        dataScope.launch {
                            connectChannel.send("yes")
                        }
                    }
                    ?.enqueue()


        }
    }


    fun disconnect() {
        bleDataManager?.disconnect()
        bleDataManager?.close()
    }


}