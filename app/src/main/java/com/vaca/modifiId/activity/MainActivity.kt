package com.vaca.modifiId.activity

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.vaca.modifiId.R

import com.vaca.modifiId.adapter.BleViewAdapter
import com.vaca.modifiId.bean.BleBean
import com.vaca.modifiId.ble.BleDataWorker
import com.vaca.modifiId.ble.BleScanManager
import com.vaca.modifiId.databinding.ActivityMainBinding
import com.vaca.modifiId.utils.InputFilterMinMax
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class MainActivity : AppCompatActivity(),BleViewAdapter.ItemClickListener {
    lateinit var x1: EditText
    lateinit var x2: EditText
    lateinit var x3: EditText
    lateinit var x4: EditText
    private val bleList: MutableList<BleBean> = ArrayList()
    var nrfConnect = false

    private val dataScope = CoroutineScope(Dispatchers.IO)
    private val bleWorker: BleDataWorker = BleDataWorker()
    val scan = BleScanManager()
    val mainVisible=MutableLiveData<Boolean>()


    lateinit var bleViewAdapter : BleViewAdapter

    fun updateDevice(byteArray: ByteArray) {
        dataScope.launch {
            bleWorker.updateDevice(byteArray)
        }

    }

    private fun setScan(bluetoothLeScanner: BluetoothLeScanner) {
        scan.setScan(bluetoothLeScanner)
    }


    fun startServer(app: Application) {
        scan.start()
        scan.setCallBack(object : BleScanManager.Scan {
            override fun scanReturn(name: String, bluetoothDevice: BluetoothDevice) {
                var z: Int = 0;
                for (ble in bleList) run {
                    if (ble.name == bluetoothDevice.name) {
                        z = 1
                    }
                }
                if (z == 0) {
                    bleList.add(BleBean(name, bluetoothDevice))
                    bleViewAdapter.addDevice(name, bluetoothDevice)
                }
            }
        })
    }

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
           dataScope.launch {
                bleChannel.send(true)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClick()

        binding.x1.filters = arrayOf(InputFilterMinMax("0", "255"))
        binding.x2.filters = arrayOf(InputFilterMinMax("0", "255"))
        binding.x3.filters = arrayOf(InputFilterMinMax("0", "255"))
        binding.x4.filters = arrayOf(InputFilterMinMax("0", "255"))

        bleViewAdapter = BleViewAdapter(this)
        binding.bleTable.adapter=bleViewAdapter
        binding.bleTable.layoutManager= GridLayoutManager(this, 2);
        bleViewAdapter.setClickListener(this)


        mainVisible.observe(this,{
            if(it){
                binding.group.visibility=View.VISIBLE
                binding.bleTable.visibility=View.GONE
            }else{
                binding.group.visibility=View.INVISIBLE
                binding.bleTable.visibility=View.VISIBLE
            }

        })

        Thread {
            runBlocking {
               setScan(getLeScan())
            }
          startServer(application)


        }.start()


    }

    fun writeId(view: View) {
       bleWorker.sendCmd(byteArrayOf(
               binding.x1.text.toString().toInt().toByte(),
               binding.x2.text.toString().toInt().toByte(),
               binding.x3.text.toString().toInt().toByte(),
               binding.x4.text.toString().toInt().toByte(),
               0.toByte(),
               0.toByte(),
       ))
    }

    override fun onScanItemClick(bluetoothDevice: BluetoothDevice?) {

        bleWorker.initWorker(application, bluetoothDevice)
        mainVisible.postValue(true)
    }








    private fun setClick() {
        binding.ecgButton.apply {
            setOnClickListener {

                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                background =
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.top_button_bg)
                binding.prButton.setTextColor(
                        ContextCompat.getColor(
                                this@MainActivity,
                                R.color.login_hint_black
                        )
                )
                binding.prButton.background = null
            }
        }

        binding.prButton.apply {
            setOnClickListener {



                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                background =
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.top_button_bg)
                binding.ecgButton.setTextColor(
                        ContextCompat.getColor(
                                this@MainActivity,
                                R.color.login_hint_black
                        )
                )
                binding.ecgButton.background = null
            }
        }


    }




    override fun onBackPressed() {
        moveTaskToBack(false)

    }
}


