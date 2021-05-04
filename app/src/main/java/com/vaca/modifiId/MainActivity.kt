package com.vaca.modifiId

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    lateinit var x1:EditText
    lateinit var x2:EditText
    lateinit var x3:EditText
    lateinit var x4:EditText




    suspend fun getLeScan(): BluetoothLeScanner {
        while (true) {
            val bluetoothManager =
                    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter.bluetoothLeScanner == null) {
                startActivityForResult(
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_ENABLE_BT
                )
                bleChannel.receive()
                delay(500)
            } else {
                return bluetoothAdapter.bluetoothLeScanner
            }
        }


    }

    private val bleChannel = Channel<Boolean>(Channel.CONFLATED)
    private val REQUEST_ENABLE_BT = 224
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            BleServer.dataScope.launch {
                bleChannel.send(true)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        x1=findViewById(R.id.x1)
        x2=findViewById(R.id.x2)
        x3=findViewById(R.id.x3)
        x4=findViewById(R.id.x4)
        x1.filters= arrayOf(InputFilterMinMax("0","255"))
        x2.filters= arrayOf(InputFilterMinMax("0","255"))
        x3.filters= arrayOf(InputFilterMinMax("0","255"))
        x4.filters= arrayOf(InputFilterMinMax("0","255"))






        Thread {
            runBlocking {
                BleServer.setScan(getLeScan())
            }
            BleServer.startServer(application)


        }.start()


    }

    fun writeId(view: View) {
        BleServer.BLE_DATA_WORKER.sendCmd(byteArrayOf(x1.text.toString().toInt().toByte(),x2.text.toString().toInt().toByte(),x3.text.toString().toInt().toByte(),x4.text.toString().toInt().toByte()))
    }
}