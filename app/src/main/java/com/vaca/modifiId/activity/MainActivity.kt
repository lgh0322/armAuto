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

class MainActivity : AppCompatActivity(), BleViewAdapter.ItemClickListener {
    lateinit var x1: EditText
    lateinit var x2: EditText
    lateinit var x3: EditText
    lateinit var x4: EditText


    var xx1 = 0;
    var xx2 = 0;
    var xx3 = 0
    var xx4 = 0
    var xx5 = 0
    var xx6 = 0


    private val bleList: MutableList<BleBean> = ArrayList()
    var nrfConnect = false

    private val dataScope = CoroutineScope(Dispatchers.IO)
   lateinit var  bleWorker: BleDataWorker
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


    /**
     * 显示键盘
     *
     * @param et 输入焦点
     */
    fun showInput(et: EditText) {
        et.requestFocus()
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏键盘
     */
    protected fun hideInput() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = window.peekDecorView()
        if (null != v) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    var mySendByteArray:ByteArray?=null
    val hintToast=MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClick()

        hintToast.observe(this,{
            Toast(this).apply {
                val layout = layoutInflater.inflate(R.layout.toast_layout, null)
                layout.findViewById<TextView>(R.id.dada).apply {
                    text = it
                }
                setGravity(Gravity.CENTER, 0, 0)
                duration = Toast.LENGTH_SHORT
                setView(layout)
                show()
            }
        })



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

        binding.main.setOnClickListener {
            hideInput()
        }

        buttonSelect.value = false

        deviceInfo.observe(this,{


            if(buttonSelect!!.value ==false){
                binding.x1.setText(it[0].toUByte().toInt().toString())
                binding.x2.setText(it[1].toUByte().toInt().toString())
                binding.x3.setText(it[2].toUByte().toInt().toString())
                binding.x4.setText(it[3].toUByte().toInt().toString())

            }else{
                binding.x1.setText(String.format("%02X",it[0].toUByte().toInt()))
                binding.x2.setText(String.format("%02X",it[1].toUByte().toInt()))
                binding.x3.setText(String.format("%02X",it[2].toUByte().toInt()))
                binding.x4.setText(String.format("%02X",it[3].toUByte().toInt()))

            }

            val num=it[4].toUByte().toInt()
            val num2=it[5].toUByte().toInt()
            val num3=num+num2*256
            binding.writeCount.setText(num3.toString())

            bleWorker.sendCmd(byteArrayOf(0x0.toByte()))
        })

//        binding.x1.filters = arrayOf(InputFilterMinMax("0", "255"))
//        binding.x2.filters = arrayOf(InputFilterMinMax("0", "255"))
//        binding.x3.filters = arrayOf(InputFilterMinMax("0", "255"))
//        binding.x4.filters = arrayOf(InputFilterMinMax("0", "255"))


        buttonSelect.observe(this, {
            if (it) {
                binding.x1.setText(String.format("%02X", xx1))
                binding.x2.setText(String.format("%02X", xx2))
                binding.x3.setText(String.format("%02X", xx3))
                binding.x4.setText(String.format("%02X", xx4))

            } else {
                binding.x1.setText(xx1.toString())
                binding.x2.setText(xx2.toString())
                binding.x3.setText(xx3.toString())
                binding.x4.setText(xx4.toString())

            }
        })



        binding.x1.doAfterTextChanged {
            if (buttonSelect.value!!) {
                try {
                    xx1 = Integer.valueOf(binding.x1.text.toString(), 16)
                } catch (e: java.lang.Exception) {

                }
            } else {
                try {
                    xx1 = binding.x1.text.toString().toInt()
                } catch (e: java.lang.Exception) {

                }

            }
        }

        binding.x2.doAfterTextChanged {
            if (buttonSelect.value!!) {
                try {
                    xx2 = Integer.valueOf(binding.x2.text.toString(), 16)
                } catch (e: java.lang.Exception) {

                }
            } else {
                try {
                    xx2 = binding.x2.text.toString().toInt()
                } catch (e: java.lang.Exception) {

                }
            }
        }

        binding.x3.doAfterTextChanged {
            if (buttonSelect.value!!) {
                try {
                    xx3 = Integer.valueOf(binding.x3.text.toString(), 16)
                } catch (e: java.lang.Exception) {

                }
            } else {
                try {
                    xx3 = binding.x3.text.toString().toInt()
                } catch (e: java.lang.Exception) {

                }
            }
        }

        binding.x4.doAfterTextChanged {
            if (buttonSelect.value!!) {
                try {
                    xx4 = Integer.valueOf(binding.x4.text.toString(), 16)
                } catch (e: java.lang.Exception) {

                }
            } else {
                try {
                    xx4 = binding.x4.text.toString().toInt()
                } catch (e: java.lang.Exception) {

                }
            }
        }



        binding.writeCount.doAfterTextChanged {

                try {
                    val wu = binding.writeCount.text.toString().toInt()
                    xx5=wu.and(0xff)
                    xx6=wu.and(0xff00).shr(8)
                } catch (e: java.lang.Exception) {

                }

        }






        bleViewAdapter = BleViewAdapter(this)
        binding.bleTable.adapter = bleViewAdapter
        binding.bleTable.layoutManager = GridLayoutManager(this, 2);
        bleViewAdapter.setClickListener(this)


        mainVisible.observe(this, {
            if (it) {
                binding.group.visibility = View.VISIBLE
                binding.bleTable.visibility = View.GONE
            } else {
                binding.group.visibility = View.INVISIBLE
                binding.bleTable.visibility = View.VISIBLE
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
        try {
            mySendByteArray=byteArrayOf(
                    xx1.toByte(),
                    xx2.toByte(),
                    xx3.toByte(),
                    xx4.toByte(),
                    xx5.toByte(),
                    xx6.toByte(),
            )
            bleWorker.sendCmd(mySendByteArray!!)
        } catch (e: Exception) {
          hintToast.postValue("请输入正确的参数")
        }

    }

    override fun onScanItemClick(bluetoothDevice: BluetoothDevice?) {

        bleWorker.initWorker(application, bluetoothDevice)
        mainVisible.postValue(true)
    }


    private fun setClick() {
        binding.ecgButton.apply {
            setOnClickListener {
                buttonSelect.postValue(false)
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

                buttonSelect.postValue(true)

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


