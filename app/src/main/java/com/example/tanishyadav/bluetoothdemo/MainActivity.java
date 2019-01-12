package com.example.tanishyadav.bluetoothdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore;
import android.icu.text.UnicodeSetSpanner;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    int REQUEST_ENABLE_BT = 1;
    Button b_on,b_off,b_show,scan,discoverable,send,listen;
    BluetoothAdapter myBluetoothAdapter;
    Intent btnEnablingIntent;
    ListView listview,discoverable_listview;
    ArrayList<String> stringdiscoverablelist = new ArrayList<String>();
    ArrayAdapter mydiscoverableAdapter;
    EditText WriteMessage;
    TextView status,msg_box;
    SendRecive sendRecive;

    BluetoothDevice[] btArray;

    private static final String APP_NAME = "BluetoothDemo";
    private static final UUID MY_UUID = UUID.fromString("5809ed9e-127c-4d5d-ba68-25548226b5b7");

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 5;


    TextView text;

    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

    BroadcastReceiver scanModeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity.this","00002");
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);
                if(modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE)
                {
                    text.setText("discoverable and recieve connection");
                }
                else if(modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
                {
                    text.setText("not discoverable but recieve connection");
                }
                else if(modeValue == BluetoothAdapter.SCAN_MODE_NONE)
                {
                    text.setText("not discoverable and not recieve connection");
                }
            }
        }
    };


    private final BroadcastReceiver myReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity.this","00001");
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                Log.d("MainActivity.this","0000");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                stringdiscoverablelist.add(device.getName());
                Log.d("MainActivity.this","33333");
                Toast.makeText(MainActivity.this,"click",Toast.LENGTH_SHORT).show();

                mydiscoverableAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,stringdiscoverablelist);
                Log.d("MainActivity.this","22224");
                discoverable_listview.setAdapter(mydiscoverableAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initalize bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        b_on = (Button)findViewById(R.id.b_on);
        b_off = (Button)findViewById(R.id.b_off);
        b_show = (Button)findViewById(R.id.b_show);
        discoverable = (Button)findViewById(R.id.discoverable);
        scan = (Button)findViewById(R.id.scan);
        WriteMessage = (EditText)findViewById(R.id.WriteMessage);
        send = (Button)findViewById(R.id.send);
        listview = (ListView)findViewById(R.id.list_item);
        discoverable_listview = (ListView)findViewById(R.id.discoverable_list_item);
        btnEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        text = (TextView)findViewById(R.id.text);
        status = (TextView)findViewById(R.id.status);
        msg_box = (TextView)findViewById(R.id.msg_box);
        listen = (Button)findViewById(R.id.listen);


        discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,100);
                startActivity(intent);
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clientClass = new ClientClass(btArray[position]);
                clientClass.start();
                status.setText("Connecting");

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = String.valueOf(WriteMessage.getText());
                sendRecive.write(string.getBytes());
            }
        });




        bluetoothOnMethod();
        bluetoothOffMethod();
        showBluetoothPairedDevice();
        showBluetoothDiscoverableDevice();
        Log.v("MainActivity.this","11110");


        registerReceiver(myReciever,intentFilter);
        registerReceiver(scanModeReciever,scanIntentFilter);

    }

    private void showBluetoothDiscoverableDevice() {
        scan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                check();

                myBluetoothAdapter.startDiscovery();
                Log.v("MainActivity.this","1111");

                Log.v("MainActivity.this","22223");
                mydiscoverableAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,stringdiscoverablelist);
                Log.v("MainActivity.this","22224");
                discoverable_listview.setAdapter(mydiscoverableAdapter);
                Log.v("MainActivity.this","2222");


            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void check()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            {
                if(permissionCheck != 0)
                {
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                }
            }
        }
    }

    private void showBluetoothPairedDevice() {
        //Bluetooth devices are represented by the BluetoothDevice object. A list of paired devices can be obtained by invoking the getBondedDevices() method, which returns a set of BluetoothDevice objects. We invoke the getBondedDevices() method in the DeviceListFragment's onCreate() method.
        b_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
                String[] st = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int i=0;
                for(BluetoothDevice device:bt)
                {
                    btArray[i] = device;
                    st[i] = device.getName();
                    i++;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,st);
                listview.setAdapter(arrayAdapter);
            }
        });
    }


    private void bluetoothOffMethod() {
        b_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothAdapter.isEnabled())
                {
                    myBluetoothAdapter.disable();
                    Toast.makeText(MainActivity.this,"Disable",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                Toast.makeText(MainActivity.this,"fsfsdfsd",Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(MainActivity.this,"ttttt",Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void bluetoothOnMethod() {
        b_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothAdapter == null)
                {
                    Log.v("MainActivity.this","1111");
                }
                else
                {//If Bluetooth is available on the device, we need to enable it. To enable Bluetooth, we start an intent provided to us by the Android SDK, BluetoothAdapter.ACTION_REQUEST_ENABLE. This will present a dialog to the user, asking them for permission to enable Bluetooth on the device. REQUEST_BLUETOOTH is a static integer we set to identify the activity request.
                    if(!myBluetoothAdapter.isEnabled())
                    {Log.v("MainActivity.this","111111");
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetoothIntent,REQUEST_ENABLE_BT);
                    }
                }
            }
        });
    }
//handler
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECIEVED:
                    byte[] readbuff = (byte[])msg.obj;
                    String tempMsg = new String(readbuff,0,msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });
//Accept thread
    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public ServerClass()
        {
            try  {
                serverSocket = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void run()
        {
            BluetoothSocket socket = null;

            while (socket == null)
            {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                    Log.v("MainActivity.this","kkkk");
                    Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);

                    e.printStackTrace();
                }

                if(socket!=null)
                {



                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendRecive = new SendRecive(socket);
                    sendRecive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;
        public ClientClass(BluetoothDevice device1)
        {
            device =device1;
            try{
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        public void run()
        {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;

                sendRecive = new SendRecive(socket);
                sendRecive.start();
                Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendRecive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;


        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true)
            {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
