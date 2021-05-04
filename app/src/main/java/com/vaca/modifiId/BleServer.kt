package com.vaca.modifiId

import android.app.ActivityManager
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import com.vaca.modifiId.ble.BleDataWorker
import com.vaca.modifiId.ble.BleScanManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object BleServer {
    var nrfConnect = false
    val scan = BleScanManager()
    val dataScope = CoroutineScope(Dispatchers.IO)
    val BLE_DATA_WORKER: BleDataWorker = BleDataWorker()


    fun setScan(bluetoothLeScanner: BluetoothLeScanner) {
        scan.setScan(bluetoothLeScanner)
    }


    fun updateDevice(byteArray: ByteArray) {
        dataScope.launch {
            BLE_DATA_WORKER.updateDevice(byteArray)
        }

    }

    fun startServer(app: Application) {
        scan.start()
        scan.setCallBack(object : BleScanManager.Scan {
            override fun scanReturn(name: String, bluetoothDevice: BluetoothDevice) {
                if (!name.contains("LGH")) {
                    return
                }
                if (!nrfConnect) {
                    nrfConnect = true
                    BLE_DATA_WORKER.initWorker(app, bluetoothDevice)
                }
            }
        })
    }

    var rtDataPause = false


    fun setTopApp(context: Context) {
        if (!isRunningForeground(context)) {
            /**获取ActivityManager*/
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager;

            /**获得当前运行的task(任务)*/
            val taskInfoList = activityManager.getRunningTasks(100);
            for (taskInfo in taskInfoList) {
                /**找到本应用的 task，并将它切换到前台*/
                if (taskInfo.topActivity?.packageName == context.packageName) {
                    activityManager.moveTaskToFront(taskInfo.id, 0);
                    break;
                }
            }
        }
    }


    fun isRunningForeground(context: Context): Boolean {
        val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as (ActivityManager);
        val appProcessInfoList = activityManager.getRunningAppProcesses();
        /**枚举进程*/
        for (appProcessInfo in appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private fun timeConvertM(s: String): String {
        val base = 0
        var t: String = s.substring(base + 1, base + 5)
        t += "-"
        t += s.substring(base + 5, base + 7)
        t += "-"
        t += s.substring(base + 7, base + 9)
        t += " "
        t += s.substring(base + 9, base + 11)
        t += ":"
        t += s.substring(base + 11, base + 13)
        t += ":"
        t += s.subSequence(base + 13, base + 15)
        return t
    }


}