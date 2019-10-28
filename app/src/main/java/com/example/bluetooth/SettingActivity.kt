package com.example.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import a5.com.a5bluetoothlibrary.A5DeviceManager
import a5.com.a5bluetoothlibrary.A5BluetoothCallback
import a5.com.a5bluetoothlibrary.A5Device
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.widget.Toast
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.practice.*
import kotlinx.android.synthetic.main.practice.view.*
import kotlinx.android.synthetic.main.settings.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class SettingActivity : AppCompatActivity(), A5BluetoothCallback {

    private var MINUTEINMILLIS: Int = 60000
    private var connectedDevices = mutableListOf<A5Device?>()
    private var device: A5Device? = null
    private var counter: Int = 0
    private var countDownTimer: CountDownTimer? = null
    private var isPFShow = false
    private var isAFShow = false
    private var startTime = System.currentTimeMillis()
    private var pressureSum: Int = 0
    private var currentBPM: Int = 0
    private var numPeaks: Int = 0
    private var climbing = true
    private var pressureAvg = 0
    private var max = 0
    private var min = 0

    private lateinit var deviceAdapter: DeviceAdapter

    override fun bluetoothIsSwitchedOff() {
        Toast.makeText(this, "bluetooth is switched off", Toast.LENGTH_SHORT).show()
    }

    override fun searchCompleted() {
        Toast.makeText(this, "search completed", Toast.LENGTH_SHORT).show()
    }

    override fun didReceiveIsometric(device: A5Device, value: Int) {
        manageReceiveIsometric(device, value)
    }

    override fun onWriteCompleted(device: A5Device, value: String) {
    }

    override fun deviceConnected(device: A5Device) {
    }

    override fun deviceFound(device: A5Device) {
        deviceAdapter.addDevice(device)
        connectedDevices.add(device)
    }

    override fun deviceDisconnected(device: A5Device) {
    }

    override fun on133Error() {
    }

    object Values {
        const val REQUEST_ENABLE_INTENT = 999
        const val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 998
    }


    private fun loadPracticeFragment() {
        if(!isPFShow) {
            var manager = supportFragmentManager
            var transaction = manager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container, PracticeFragment()).commit()
            isPFShow = true
            isAFShow = false
        }
    }

    private fun loadAboutFragment() {
        if(!isAFShow) {
            var manager = supportFragmentManager
            var transaction = manager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container, AboutFragment()).commit()
            isAFShow = true
            isPFShow = false
        }
    }

    private fun hideContainers() {
        var manager = supportFragmentManager
        var transaction = manager.beginTransaction()

        transaction.addToBackStack(null)
        if(isPFShow) {
            isPFShow = false
            transaction.hide(PracticeFragment())
        }
        transaction.replace(R.id.fragment_container, InvisFragment()).commit()
        isPFShow = false
        isAFShow = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        requestPermission()
        initRecyclerView()

        bottom_navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.action_about -> {
                    loadAboutFragment()
                    true
                }
                R.id.action_practice -> {
                    loadPracticeFragment()
                    true
                }
                R.id.action_settings -> {
                    hideContainers()
                    true
                }else -> false
            }
        }

        connectButton.setOnClickListener {
            val device = this.device
            if (device != null) {
                A5DeviceManager.connect(this, device)
            }
        }


        disconnectButton.setOnClickListener {
            device?.disconnect()
        }

        scanDevices.setOnClickListener {
            for (device in connectedDevices) {
                device?.disconnect()
            }
            device?.disconnect()
            device = null
            connectedDevices.clear()
            deviceAdapter.clearDevices()

            A5DeviceManager.scanForDevices()
        }
    }

    fun startIso() {
        device?.startIsometric()
    }

    fun stopIso() {
        device?.stop()
        max = 0
        min = 0
        numPeaks = 0
        pressureAvg = 0
        pressureSum = 0
        currentBPM = 0
    }

    @Synchronized
    private fun analyze(name: String, value: Int) {
        runOnUiThread {

   //     val peakPressures = ArrayList<Int>()
            val currentTime = System.currentTimeMillis() - startTime
            val lbs = value * 0.2248
            if (climbing && value >= max) { //Case where we're just climbing
                max = value
            } else if (climbing && value < max) {
           //     peakPressures.add(max)
                numPeaks++
                pressureSum += max

                pressureAvg = pressureSum/numPeaks

                min = max
                max = 0
                climbing = false
                currentBPM = MINUTEINMILLIS * numPeaks / currentTime.toInt()
            } else if (!climbing && value < min) {
                min = value
            } else if (!climbing && value > min) {
                climbing = true
                max = value
            }

            if(isPFShow) {
                var info: String = "Pounds: " + lbs.toInt() + " PressureAver: " + pressureAvg + " BPM: " + currentBPM
                textView3.setText(info)
           /*     textView3.setText(String.format(
                    Locale.US, "Force: %d PressureAver: %d BPM: %f", lbs.toInt(), pressureAvg, currentBPM
                )) */
            }
        }
    }


    private fun manageReceiveIsometric(thisDevice: A5Device, thisValue: Int) {
        if (connectedDevices.isNotEmpty()) {
            if (connectedDevices[0]?.device?.address == thisDevice.device.address) {
                analyze(thisDevice.device.name, thisValue)
            }
        }
    }

    fun deviceSelected(device: A5Device) {
        this.device = device
        Toast.makeText(this, "device selected: " + device.device.name, Toast.LENGTH_SHORT).show()
    }

    private fun initRecyclerView() {
        deviceAdapter = DeviceAdapter(this)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = deviceAdapter
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        Values.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
                    )

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                startBluetooth()
            }
        } else {
            startBluetooth()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            Values.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startBluetooth()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Values.REQUEST_ENABLE_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                startBluetooth()
            }
        }
    }

    private fun startBluetooth() {
        val bluetoothManager = A5App().getInstance().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, Values.REQUEST_ENABLE_INTENT)
        } else {
            A5DeviceManager.setCallback(this)
            A5DeviceManager.scanForDevices()
        }
    }

    private fun startTimer() {
        counter = 0
        countDownTimer = object : CountDownTimer(420000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter++
            }

            override fun onFinish() {
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }
}



