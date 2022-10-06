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

    private var my_bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var my_pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private var isHost : Boolean = false

    private lateinit var binding: ActivityMainBinding

    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
        val HOST_DEVICE : String = "isHost"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        my_bluetoothAdapter= BluetoothAdapter.getDefaultAdapter()

        if (my_bluetoothAdapter==null){
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (!my_bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            startActivityForResult(enableBluetoothIntent,REQUEST_ENABLE_BLUETOOTH)
        }
        showPairedDevices()

        binding.btnRefresh.setOnClickListener{
            showPairedDevices()
        }
        binding.btnHost.setOnClickListener{
            hostGame()
        }

    }

    private fun showPairedDevices(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        my_pairedDevices = my_bluetoothAdapter!!.bondedDevices
        val deviceList : ArrayList<BluetoothDevice> = ArrayList()
        val deviceNameList : ArrayList<String> = ArrayList()

        if (!my_pairedDevices.isEmpty()){
            for (device : BluetoothDevice in my_pairedDevices){
                deviceList.add(device)
                deviceNameList.add(device.name)
                Log.i("device"," " +device)
            }
        }
        else{ Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show() }

        val my_listAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, deviceNameList)
        binding.lvList.adapter = my_listAdapter
        binding.lvList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = deviceList[position]
            val address : String = device.address

            val intent = Intent(this, GameActivity::class.java )
            intent.putExtra(EXTRA_ADDRESS, address)
            intent.putExtra(HOST_DEVICE, isHost)
            startActivity(intent)
        }
    }

    private fun hostGame(){
        isHost=true
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(HOST_DEVICE, isHost)
        startActivity(intent)
    }
}


































