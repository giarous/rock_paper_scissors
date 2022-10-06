package com.example.btgame

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.example.btgame.databinding.ActivityGameBinding
import com.example.btgame.databinding.ActivityMainBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class GameActivity : AppCompatActivity() {

    companion object{

        private var bluetoothAdapter: BluetoothAdapter? = null
        var sendReceive: SendReceive? = null

        //Declaration of the STATES for communication
        val STATE_HOSTING = 1
        val STATE_CONNECTING = 2
        val STATE_CONNECTED = 3
        val STATE_CONNECTION_FAILED = 4
        val STATE_MESSAGE_RECEIVED = 5

        var myChoice : String? = null
        var opponentsChoice : String? = null

        private val APP_NAME = "BTChat"
        private val MY_UUID: UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66")
        var my_address: String? = "temp_address"
        var isHost : Boolean = false
    }

    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        isHost = intent.getBooleanExtra(MainActivity.HOST_DEVICE,false)

        if (isHost){
            Toast.makeText(this, "HOSTING", Toast.LENGTH_SHORT).show()
            val serverClass = ServerClass()
            serverClass.start()
        }

        else{
            my_address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS)
            val my_device: BluetoothDevice = bluetoothAdapter!!.getRemoteDevice(my_address)
            val clientClass = ClientClass(my_device)
            clientClass.start()
        }

        declareListeners()

    }

    private fun declareListeners() {

        binding.btnRock.setOnClickListener{
           sendChoice("Rock")
        }

        binding.btnPaper.setOnClickListener{
           sendChoice("Paper")
        }
        binding.btnScissors.setOnClickListener{
           sendChoice("Scissors")
        }
        binding.ivRock.setOnClickListener{
            sendChoice("Rock")
        }

        binding.ivPaper.setOnClickListener{
            sendChoice("Paper")
        }
        binding.ivScissors.setOnClickListener{
            sendChoice("Scissors")
        }

        //Send message to other device to start new game
        binding.btnNewGame.setOnClickListener{
            sendChoice("New Game")
            start_new_game()
        }

    }

    private fun start_new_game(){
        myChoice=null
        opponentsChoice=null
        binding.btnRock.isEnabled=true
        binding.btnPaper.isEnabled=true
        binding.btnScissors.isEnabled=true
        binding.ivPaper.isEnabled=true
        binding.ivScissors.isEnabled=true
        binding.ivRock.isEnabled=true
        binding.ivYourOpponent.setImageResource(R.drawable.loading)
        binding.ivYourChoice.setImageResource(R.drawable.loading)
        binding.tvGameState.text = "SELECT ONE OF THE OPTIONS"
        binding.btnNewGame.isEnabled=false
    }

    private fun sendChoice(selectedOption: String){

        myChoice = selectedOption
        when(myChoice){
            "Rock"-> binding.ivYourChoice.setImageResource(R.drawable.rock)
            "Paper"-> binding.ivYourChoice.setImageResource(R.drawable.paper)
            "Scissors"-> binding.ivYourChoice.setImageResource(R.drawable.scissors)}
        sendReceive?.write(myChoice!!.toByteArray())
        deactivateButtons()

        if(opponentsChoice!=null) {
            compareResults()
        }
        else {
            binding.tvGameState.text = "Waiting for opponent to play..."
        }
    }

    private fun deactivateButtons(){
        binding.btnPaper.isEnabled=false
        binding.btnRock.isEnabled=false
        binding.btnScissors.isEnabled=false
        binding.ivPaper.isEnabled=false
        binding.ivRock.isEnabled=false
        binding.ivScissors.isEnabled=false
    }

    private fun compareResults(){
        when(myChoice){
            "Rock"->
                when(opponentsChoice){
                "Rock"-> binding.tvGameState.text = "DRAW"
                "Paper"-> binding.tvGameState.text = "YOU LOST"
                "Scissors"-> binding.tvGameState.text = "YOU WON"
                }


            "Paper"->
                when(opponentsChoice){
                    "Rock"-> binding.tvGameState.text = "YOU WON"
                    "Paper"-> binding.tvGameState.text = "DRAW"
                    "Scissors"-> binding.tvGameState.text = "YOU LOST"
                }

            "Scissors"->
                when(opponentsChoice){
                    "Rock"-> binding.tvGameState.text = "YOU LOST"
                    "Paper"-> binding.tvGameState.text = "YOU WON"
                    "Scissors"-> binding.tvGameState.text = "DRAW"
                }
        }
        when(opponentsChoice){
            "Rock"-> binding.ivYourOpponent.setImageResource(R.drawable.rock)
            "Paper"-> binding.ivYourOpponent.setImageResource(R.drawable.paper)
            "Scissors"-> binding.ivYourOpponent.setImageResource(R.drawable.scissors)
        }
        binding.btnNewGame.isEnabled= true

    }

    private fun setResult(selectedOption : String){

        when(selectedOption){
            "Rock" -> {
                opponentsChoice = "Rock"
                binding.ivYourOpponent.setImageResource(R.drawable.selected)}
            "Paper" ->  {
                opponentsChoice = "Paper"
                binding.ivYourOpponent.setImageResource(R.drawable.selected)}

            "Scissors" ->  {
                opponentsChoice = "Scissors"
                binding.ivYourOpponent.setImageResource(R.drawable.selected)}

            "New Game"-> start_new_game()
        }

        if (myChoice != null) { compareResults() }



    }

    var handler = Handler{ msg ->
        when (msg.what) {
            STATE_HOSTING -> binding.status.text = "Connection status: HOSTING"
            STATE_CONNECTING -> binding.status.text = "Connection status: CONNECTING"
            STATE_CONNECTED -> binding.status.text = "Connection status: CONNECTED"
            STATE_CONNECTION_FAILED -> binding.status.text = "Connection status: CONNECTION FAILED"
            STATE_MESSAGE_RECEIVED -> {
                val readBuff = msg.obj as ByteArray
                val tempMsg = String(readBuff, 0, msg.arg1)
                setResult(tempMsg)

            }
        }
        true
    }

    private inner class ServerClass : Thread() {
        private var serverSocket: BluetoothServerSocket? = null
        override fun run() {
            var socket: BluetoothSocket? = null
            while (socket == null) {
                try {
                    val message: Message = Message.obtain()
                    message.what = STATE_HOSTING

                    handler.sendMessage(message)
                    socket = serverSocket!!.accept()
                } catch (e: IOException) {
                    e.printStackTrace()
                    val message: Message = Message.obtain()
                    message.what = STATE_CONNECTION_FAILED
                    handler.sendMessage(message)
                }
                if (socket != null) {
                    val message: Message = Message.obtain()
                    message.what = STATE_CONNECTED
                    handler.sendMessage(message)
                    sendReceive = SendReceive(socket)
                    sendReceive!!.start()
                    break
                }
            }
        }

        init {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    APP_NAME,
                    MY_UUID
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ClientClass(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null
        override fun run() {
            try {
                socket!!.connect()
                val message = Message.obtain()
                message.what = STATE_CONNECTED
                handler.sendMessage(message)
                sendReceive = SendReceive(socket!!)
                sendReceive!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
                val message = Message.obtain()
                message.what = STATE_CONNECTION_FAILED
                handler.sendMessage(message)
            }
        }

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    inner class SendReceive(mySocket: BluetoothSocket): Thread() {
        private val bluetoothSocket: BluetoothSocket = mySocket
        var inputStream: InputStream?
        var outputStream: OutputStream?


        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int = 1

            while (true) {
                try {
                    if (inputStream != null) {
                        bytes = inputStream!!.read(buffer)
                    }
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun write(bytes: ByteArray?) {
            try {
                outputStream?.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        init {
            var tempIn: InputStream? = null
            var tempOut: OutputStream? = null
            try {
                tempIn = bluetoothSocket.inputStream
                tempOut = bluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            inputStream = tempIn
            outputStream = tempOut

        }
    }
}