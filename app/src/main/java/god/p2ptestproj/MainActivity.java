package god.p2ptestproj;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener, View.OnClickListener {
    //filter create
    private final IntentFilter intentFilter = new IntentFilter();
    //channel is to keep connect with Wifi P2P communication
    WifiP2pManager.Channel mChannel;
    //Manager is a class to manage all the function of Wifi P2P
    WifiP2pManager mManager;
    NumberPicker picker;
    TextView textView, peerInfo, ownInfo;
    Button sendBut, connBut;
    EditText textEditer, ipEditer;
    private final String TAG = "MainActivity";
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private String[] peersName;
    //receiver is create to receiver the broadcast information

    //build Server socket
    private SocketServer server = new SocketServer(6666);
    private SocketClient client;
    private boolean isClient = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d(TAG, "WIFI_P2P is able to use");
                    } else {
                        Log.d(TAG, "WIFI_P2P is unable to use");
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
                    if (mManager != null) {
                        mManager.requestPeers(mChannel, MainActivity.this);
                    }
                    Log.d(TAG, "P2P peers changed");
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:

                    Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                    if (mManager == null) {
                        return;
                    }
                    NetworkInfo networkInfo = intent
                            .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    if (networkInfo.isConnected()) {
                        mManager.requestConnectionInfo(mChannel, MainActivity.this);
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //to set the filter
        //check Wifi P2P is available
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        //peers list change
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        //wifi P2P connection change
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //Wifi P2P device information change
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        picker = (NumberPicker) findViewById(R.id.pick);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        peersName = new String[1];
        peersName[0] = "No Devices";
        picker.setDisplayedValues(peersName);
        sendBut = (Button) findViewById(R.id.sendButton);
        sendBut.setEnabled(false);
        connBut = (Button) findViewById(R.id.connectButton);
        connBut.setEnabled(false);
        sendBut.setOnClickListener(this);
        connBut.setOnClickListener(this);
        textEditer = (EditText) findViewById(R.id.editText);
        ipEditer = (EditText) findViewById(R.id.ipEdit);

        textView = (TextView) findViewById(R.id.text);
        peerInfo = (TextView) findViewById(R.id.peerInfo);
        ownInfo = (TextView) findViewById(R.id.textView);

        server.startListen();

        SocketClient.mHandler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                sendBut.setEnabled(true);
                peerInfo.append("\nReceive msg: " + msg.obj.toString());
            }
        };

        SocketServer.ServerHandler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                sendBut.setEnabled(true);
                peerInfo.append("\nReceive msg: " + msg.obj.toString());
            }
        };

        ownInfo.append("\nIP: "+this.getLocalIpAddress());
        ownInfo.append("\nMAC: "+this.getLocalMacAddress());

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sendButton:
                String s = textEditer.getEditableText().toString();
                if (isClient) {
                    client.sendMessage(s);
                } else {
                    server.sendMessage(s);
                }
                peerInfo.append("\nMe: " + s);
                Snackbar.make(v, "Sending...", Snackbar.LENGTH_LONG)
                        .setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this,
                                        "send Canceled", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                break;
            case R.id.connectButton:
                try {
                    isClient = true;
                    //String ip = connect(picker.getValue());
                    String ip = ipEditer.getEditableText().toString();
                    client = new SocketClient(MainActivity.this, ip, 6666);
                    client.startClientThread();
                    sendBut.setEnabled(true);
                    Toast.makeText(this,"Success Connect!",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                break;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        //register the receiver to application
        registerReceiver(mReceiver, intentFilter);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "onFailure");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        //set all peer list into comportment TODO: 2017/09/17 0017
        if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
            textView.setVisibility(View.INVISIBLE);
            if (peersName.length > 0) {
                peersName[0] = "No Devices";
            } else {
                peersName = new String[1];
                peersName[0] = "No Devices";
            }
            return;
        } else {
            peersName = new String[peers.size()];
            int i = 0;
            for (WifiP2pDevice device : peers) {
                peersName[i++] = device.deviceName;
            }
            textView.setVisibility(View.VISIBLE);
            textView.setText("(available)");
            connBut.setEnabled(true);
        }
        picker.setDisplayedValues(peersName);
    }

    public String connect(final int num) {
        // Picking the first device found on the network.
        final WifiP2pDevice device = peers.get(num);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Successfully connect " + device.deviceName, Toast.LENGTH_SHORT).show();
//                peerInfo.setText("" + device.toString() + "\n\nDEVICE address" + device.deviceAddress);
                Log.d(TAG, "connect success");
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Fail to connect peer", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "connect fail");
            }
        });
        return device.deviceAddress;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        textView.setVisibility(View.VISIBLE);
        textView.setText("(connected)");
        // InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = info.groupOwnerAddress;
        Log.d(TAG, "onConnectionInfoAvailable");
        Log.d(TAG, info.toString());
        if (info.groupFormed && info.isGroupOwner) {

        } else if (info.groupFormed) {

        }
    }

    public String getLocalIpAddress() {


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        try {
            return InetAddress.getByName(String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff))).toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

}
