package com.vaca.modifiId.activity

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.vaca.modifiId.ble.BleDataWorker
import com.vaca.modifiId.ble.BleScanManager
import com.vaca.modifiId.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

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
    private val bleWorker: BleDataWorker = BleDataWorker()
    val scan = BleScanManager()
    val mainVisible = MutableLiveData<Boolean>()


    lateinit var bleViewAdapter: BleViewAdapter

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


    val buttonSelect = MutableLiveData<Boolean>()


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClick()

        binding.main.setOnClickListener {
            hideInput()
        }

        buttonSelect.value = false

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
            if (buttonSelect.value!!) {

            } else {
                try {
                    xx4 = binding.x4.text.toString().toInt()
                } catch (e: java.lang.Exception) {

                }
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
            bleWorker.sendCmd(byteArrayOf(
                    xx1.toByte(),
                    xx2.toByte(),
                    xx3.toByte(),
                    xx4.toByte(),
                    xx5.toByte(),
                    xx6.toByte(),
            ))
        } catch (e: Exception) {
            Toast(this).apply {
                val layout = layoutInflater.inflate(R.layout.toast_layout, null)
                layout.findViewById<TextView>(R.id.dada).apply {
                    text = "请输入正确参数"
                }
                setGravity(Gravity.CENTER, 0, 0)
                duration = Toast.LENGTH_SHORT
                setView(layout)
                show()
            }
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


