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


class BleDataWorker {
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

    private val sendCallBack = object : DataSentCallback {
        override fun onDataSent(device: BluetoothDevice, data: Data) {
            dataScope.launch {
                data.value?.let {
                    sendChannel.send(it)
                }
            }
        }
    }


    private val readCallBack = object : DataReceivedCallback {
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            dataScope.launch {

                data.value?.run {
                    for (k in this) {

                    }
                    if (equalPkg(this, startPackage)) {
                        fileChannel.send(BleCallBack.START)
                    } else if (equalPkg(this, bodyPackage)) {
                        fileChannel.send(BleCallBack.BODY)
                    } else if (equalPkg(this, endPackage)) {
                        fileChannel.send(BleCallBack.END)
                    } else {
                        fileChannel.send(BleCallBack.OTHER)
                    }
                }
            }

        }

    }


    fun sendCmdOTA(bs: ByteArray) {
        bleDataManager?.sendCmdOTA(bs)
    }

    fun sendCmd(bs: ByteArray) {
        bleDataManager?.sendCmd(bs)
    }


    fun readBleCallBack() {
        bleDataManager?.read()

    }

    val MAX_TRANS_COUNT = 15
    val CMD_FW_WRITE: Byte = 0x17.toByte()


    private suspend fun erase() {
        mutex.withLock {
            sendCmdOTA(byteArrayOf(0x16.toByte(), 0x00.toByte()))
            delay(200)
            readBleCallBack()
            while (fileChannel.receive() != BleCallBack.START) {
                readBleCallBack()
                delay(200)
            }
        }
    }


    private suspend fun writeId(byteArray: ByteArray) {
        mutex.withLock {
            sendCmd(byteArray)
        }
    }


    fun OTA_Write_Flash_section_start(check: Int, size: Int, Address: Int): ByteArray {
        val WriteData = ByteArray(10)
        WriteData[0] = 0x14
        WriteData[1] = 0x13
        WriteData[2] = (Address and 0x000000FF).toByte()
        WriteData[3] = (Address and 0x0000FF00 shr 8).toByte()
        WriteData[4] = (Address and 0x00FF0000 shr 16).toByte()
        WriteData[5] = (Address and -0x1000000 shr 24).toByte()
        WriteData[6] = (size and 0x000000FF).toByte()
        WriteData[7] = (size and 0x0000FF00 shr 8).toByte()
        WriteData[8] = (check and 0x000000FF).toByte()
        WriteData[9] = (check and 0x0000FF00 shr 8).toByte()
        for (k in WriteData) {
            Log.e("掠夺式开发", k.toUByte().toInt().toString())
        }
        return WriteData
    }

    suspend fun updateDevice(byteArray: ByteArray) {
        erase()

        val x = byteArray.copyOfRange(0, 5120)
        var s = 0
        for (k in x) {
            s += k.toUByte().toInt()
        }

        sendCmdOTA(OTA_Write_Flash_section_start(s, 5120, 0))

        delay(1000)

        sendCmdOTA(x)


    }


    private suspend fun writePkg(ProgramData: ByteArray, address: Int) {
        mutex.withLock {
            val writeData = ByteArray(20)
            writeData[0] = CMD_FW_WRITE
            writeData[1] = 0x13.toByte()
            writeData[2] = (address and 0x000000FF).toByte()
            writeData[3] = (address and 0x0000FF00 shr 8).toByte()
            writeData[4] = ProgramData.size.toByte()
            var i = 0
            while (i < ProgramData.size) {
                writeData[i + 5] = ProgramData[i]
                i++
            }


            withTimeoutOrNull(1) {
                sendChannel.receive()
            }

            sendCmdOTA(writeData)

            val re = sendChannel.receive()

            if (equalPkg(writeData, re)) {

            }


        }
    }

    private val comeData = object : BleDataManager.OnNotifyListener {
        override fun onNotify(device: BluetoothDevice?, data: Data?) {

        }

    }


    fun initWorker(context: Context, bluetoothDevice: BluetoothDevice?) {
        bleDataManager = BleDataManager(context, readCallBack)
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