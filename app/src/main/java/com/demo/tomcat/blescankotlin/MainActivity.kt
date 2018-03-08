package com.demo.tomcat.blescankotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast


// https://tw.saowen.com/a/19b732f80c3a438696a4ea7e2d3acfa2d4e4706928e317850a936b070b788a35

class MainActivity : AppCompatActivity()
{
    private final var TAG: String;  get() = this.localClassName;  set(value) = TODO()
    private var tvMsgBox : TextView? = null;

    private val REQUEST_BLUETOOTH_TURN_ON = 1
    private val BLE_SCAN_PERIOD : Long = 10000
    private lateinit var bleAdapter: BluetoothAdapter
    private lateinit var bleManager: BluetoothManager
    private lateinit var bleScanner: BluetoothLeScanner
    private lateinit var bleScanCallback: BleScanCallback
    private var bleScanResults = mutableMapOf<String?, BluetoothDevice?>()
    private lateinit var bleScanHandler:Handler

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMsgBox = findViewById(R.id.tvMsg);
        tvMsgBox?.setText(TAG);

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->Snackbar.make(  view, "Replace with your own action",
                                Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
        bleScanHandler = Handler()
        //藍芽管理，這是系統服務可以通過getSystemService(BLUETOOTH_SERVICE)的方法獲取例項
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        //通過藍芽管理例項獲取介面卡，然後通過掃描方法（scan）獲取裝置(device)
        bleAdapter = bleManager.adapter
        if (!bleAdapter.isEnabled) {
            val bluetoothTurnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(bluetoothTurnOn, REQUEST_BLUETOOTH_TURN_ON)
        } else {
            bleStartScan.run()
        }
    }

    //start scan
    private val bleStartScan = Runnable {
        bleScanner = bleAdapter.bluetoothLeScanner
        bleScanCallback = BleScanCallback(bleScanResults)
        bleScanCallback.setContext(this.applicationContext)
        bleScanner.startScan(bleScanCallback)
        Toast.makeText(this.applicationContext, "藍芽BLE掃描開始", Toast.LENGTH_SHORT).show()
        bleScanHandler.postDelayed(bleStopScan, this.BLE_SCAN_PERIOD)
    }

    private val bleStopScan = Runnable {
        if (bleScanner != null) {
            bleScanner.stopScan(bleScanCallback)
        }
        Toast.makeText(this.applicationContext, "藍芽BLE掃描結束", Toast.LENGTH_SHORT).show()
    }

    class BleScanCallback(resultMap: MutableMap<String?, BluetoothDevice?>) : ScanCallback() {
        var resultOfScan = resultMap
        private var context: Context? = null

        fun setContext(context: Context) {
            this.context = context
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result -> addScanResult(result) }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText( this.context,
                            "藍芽BLE掃描失敗" + "Error Code: " + errorCode,
                            Toast.LENGTH_SHORT).show()
        }

        fun addScanResult(scanResult: ScanResult?) {
            val bleDevice = scanResult?.device
            val deviceAddress = bleDevice?.address
            if (!resultOfScan.contains(deviceAddress)) {
                resultOfScan.put(deviceAddress, bleDevice)
                if (this.context != null) {
                    Toast.makeText( this.context,
                                    bleDevice?.name + ": " + bleDevice?.address,
                                    Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_BLUETOOTH_TURN_ON->{
                when (resultCode) {
                    RESULT_OK->{
                        Toast.makeText(this.applicationContext, "藍芽開啟成功", Toast.LENGTH_SHORT).show()
                        bleStartScan.run()
                    }
                    RESULT_CANCELED->{
                        Toast.makeText(this.applicationContext, "藍芽開啟失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
