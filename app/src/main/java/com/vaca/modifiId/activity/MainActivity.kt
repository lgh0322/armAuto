package com.vaca.modifiId.activity

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.vaca.modifiId.R
import com.vaca.modifiId.adapter.BleViewAdapter
import com.vaca.modifiId.bean.BleBean
import com.vaca.modifiId.ble.BleDataManager
import com.vaca.modifiId.ble.BleDataWorker
import com.vaca.modifiId.ble.BleScanManager
import com.vaca.modifiId.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import no.nordicsemi.android.ble.data.Data
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), BleViewAdapter.ItemClickListener {



    private val bleList: MutableList<BleBean> = ArrayList()
    var nrfConnect = false

    private val dataScope = CoroutineScope(Dispatchers.IO)
   var  bleWorker: BleDataWorker?=null
    val scan = BleScanManager()
    val mainVisible = MutableLiveData<Boolean>()


    lateinit var bleViewAdapter: BleViewAdapter



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


    val buttonSelect = MutableLiveData<Boolean>()

    val deviceInfo = MutableLiveData<ByteArray>()



    var mySendByteArray:ByteArray?=null
    val hintToast=MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timer().schedule(xx, Date(),50)



        bleWorker= BleDataWorker(object: BleDataManager.OnNotifyListener{
            override fun onNotify(device: BluetoothDevice?, data: Data?) {
                data?.value?.run {
                    if(this.size==6){
                        var same=true
                        for(k in 0 until 6){
                            System.out.println("sdf  "+this[k].toUByte().toInt().toString())
                            if(this[k]!=mySendByteArray!![k]){
                                same=false
                                break;
                            }
                        }
                        if(same){
                            hintToast.postValue("设置成功")
                        }
                    }else if(this.size==7){
                        if(this[6].toUByte().toInt()==243){
                            deviceInfo.postValue(this)
                        }
                    }

                    Log.e("sdf",String(this))

                }
            }

        })



        buttonSelect.value = false








        bleViewAdapter = BleViewAdapter(this)
        binding.bleTable.adapter = bleViewAdapter
        binding.bleTable.layoutManager = GridLayoutManager(this, 2);
        bleViewAdapter.setClickListener(this)




        Thread {
            runBlocking {
                setScan(getLeScan())
            }
            startServer(application)


        }.start()


    }



    override fun onScanItemClick(bluetoothDevice: BluetoothDevice?) {

        bleWorker?.initWorker(application, bluetoothDevice)
        binding.bleTable.visibility=View.GONE
        binding.mainView.visibility=View.VISIBLE
    }




    val xx=RtDataTask()

    inner class RtDataTask() : TimerTask() {
        override fun run() {
            val bb=ByteArray(11){
                0x32.toByte()
            }

            var cc=binding.seekBar1.progress;
            Log.e("fuck",cc.toString())
            bb[1]=(cc%256).toByte()
            bb[2]=(cc/256).toByte()

            cc=binding.seekBar2.progress;
            bb[3]=(cc%256).toByte()
            bb[4]=(cc/256).toByte()


            cc=binding.seekBar3.progress;
            bb[5]=(cc%256).toByte()
            bb[6]=(cc/256).toByte()


            cc=binding.seekBar4.progress;
            bb[7]=(cc%256).toByte()
            bb[8]=(cc/256).toByte()


            cc=binding.seekBar5.progress;
            bb[9]=(cc%256).toByte()
            bb[10]=(cc/256).toByte()

            bleWorker?.sendCmd(bb)
        }
    }



}


