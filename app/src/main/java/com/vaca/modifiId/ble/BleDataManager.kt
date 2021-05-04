package com.vaca.modifiId.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import java.util.*

class BleDataManager(context: Context, val read: DataReceivedCallback) : BleManager(context) {
    private var ota_char: BluetoothGattCharacteristic? = null
    private var write_char: BluetoothGattCharacteristic? = null
    private var notify_char: BluetoothGattCharacteristic? = null
    private var listener: OnNotifyListener? = null
    fun setNotifyListener(listener: OnNotifyListener?) {
        this.listener = listener
    }


    interface OnNotifyListener {
        fun onNotify(device: BluetoothDevice?, data: Data?)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }


    fun sendCmdOTA(bytes: ByteArray?) {
        writeCharacteristic(ota_char, bytes)
                .split()
                .done { }
                .enqueue()
    }


    fun sendCmd(bytes: ByteArray?) {
        writeCharacteristic(write_char, bytes)
                .split()
                .done { }
                .enqueue()
    }


    fun read() {
        readCharacteristic(ota_char).with(read).enqueue()
    }


    override fun log(priority: Int, message: String) {}


    /**
     * BluetoothGatt callbacks object.
     */
    private inner class MyManagerGattCallback : BleManagerGattCallback() {

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val otaService = gatt.getService(ota_service_uuid)
            if (otaService != null) {
                ota_char = otaService.getCharacteristic(ota_uuid)
            }

            if (ota_char != null) {
                ota_char!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }


            val service = gatt.getService(service_uuid)
            if (service != null) {
                write_char = service.getCharacteristic(write_uuid)
                notify_char = service.getCharacteristic(notify_uuid)
            }
            // Validate properties
            var notify = false
            if (notify_char != null) {
                val properties = notify_char!!.properties
                notify = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
            }
            var writeRequest = false
            if (write_char != null) {
                val properties = write_char!!.properties
                write_char!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }


            // Return true if all required services have been found
            return ota_char != null
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            return super.isOptionalServiceSupported(gatt)
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        override fun initialize() {
            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginAtomicRequestQueue()
                    .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with { _: BluetoothDevice?, mtu: Int ->
                                log(
                                        Log.INFO,
                                        "MTU set to $mtu"
                                )
                            }
                            .fail { _: BluetoothDevice?, status: Int ->
                                log(
                                        Log.WARN,
                                        "Requested MTU not supported: $status"
                                )
                            })
                    .done { log(Log.INFO, "Target initialized") }
                    .enqueue()

//            readCharacteristic(ota_char).with(read).enqueue()

        }


        override fun onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            ota_char = null
        }
    }

    companion object {
        val ota_service_uuid: UUID =
                UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb")

        val ota_uuid: UUID =
                UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")

        val service_uuid: UUID =
                UUID.fromString("00000001-0000-1000-8000-00805f9b34fb")

        val notify_uuid: UUID =
                UUID.fromString("00000003-0000-1000-8000-00805f9b34fb")

        val write_uuid: UUID =
                UUID.fromString("00000002-0000-1000-8000-00805f9b34fb")


    }
}