package com.example.btgame

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.btgame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var myBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var myPairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private var isHost: Boolean = false

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val EXTRA_ADDRESS: String = "Device_address"
        const val HOST_DEVICE: String = "isHost"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the default Bluetooth adapter
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if the device supports Bluetooth
        if (myBluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if Bluetooth is enabled, and if not, request to enable it
        if (!myBluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider requesting permissions here
                return
            }
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        // Show the list of paired devices and set up event listeners
        showPairedDevices()

        // Set a click listener for the "Refresh" button to refresh the paired device list
        binding.btnRefresh.setOnClickListener {
            showPairedDevices()
        }

        // Set a click listener for the "Host Game" button to start hosting a game
        binding.btnHost.setOnClickListener {
            hostGame()
        }
    }

    private fun showPairedDevices() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider requesting permissions here
            return
        }
        // Get the list of paired Bluetooth devices
        myPairedDevices = myBluetoothAdapter!!.bondedDevices
        val deviceList: ArrayList<BluetoothDevice> = ArrayList()
        val deviceNameList: ArrayList<String> = ArrayList()

        if (myPairedDevices.isNotEmpty()) {
            // Populate the lists with paired device information
            for (device: BluetoothDevice in myPairedDevices) {
                deviceList.add(device)
                deviceNameList.add(device.name)
                Log.i("device", " $device")
            }
        } else {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
        }

        // Create an ArrayAdapter to display the list of paired device names
        val myListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNameList)
        binding.lvList.adapter = myListAdapter

        // Set a click listener for the list items to connect to the selected device
        binding.lvList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = deviceList[position]
                val address: String = device.address

                // Start the GameActivity with the selected device's address and set as host
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra(EXTRA_ADDRESS, address)
                intent.putExtra(HOST_DEVICE, isHost)
                startActivity(intent)
            }
    }

    private fun hostGame() {
        // Set as the host and start the GameActivity
        isHost = true
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(HOST_DEVICE, isHost)
        startActivity(intent)
    }
}
