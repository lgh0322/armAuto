package com.vaca.modifiId.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.vaca.modifiId.BleServer
import com.vaca.modifiId.databinding.ActivityMainBinding
import com.vaca.modifiId.utils.InputFilterMinMax
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    lateinit var x1: EditText
    lateinit var x2: EditText
    lateinit var x3: EditText
    lateinit var x4: EditText


    lateinit var binding: ActivityMainBinding


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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.x1.filters = arrayOf(InputFilterMinMax("0", "255"))
        binding.x2.filters = arrayOf(InputFilterMinMax("0", "255"))
        binding.x3.filters = arrayOf(InputFilterMinMax("0", "255"))
        binding.x4.filters = arrayOf(InputFilterMinMax("0", "255"))






        Thread {
            runBlocking {
                BleServer.setScan(getLeScan())
            }
            BleServer.startServer(application)


        }.start()


    }

    fun writeId(view: View) {
        BleServer.BLE_DATA_WORKER.sendCmd(byteArrayOf(x1.text.toString().toInt().toByte(), x2.text.toString().toInt().toByte(), x3.text.toString().toInt().toByte(), x4.text.toString().toInt().toByte()))
    }
}