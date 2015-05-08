package phantom.edltracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.*;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ActionBarActivity implements BTPairFragment.OnCompleteListener{

    private final static int REQUEST_ENABLE_BT = 1;
    double[] gps_data = {-76.62051380,39.32716308,-76.62048966,39.32693422,-76.62007660,39.32695769,-76.62008733,39.32713081,-76.62010878,39.32737141,-76.62011951,39.32751812,-76.62012219,39.32765896,-76.62013561,39.32779100,-76.62014365,39.32791424,-76.62015170,39.32803747,-76.62017047,39.32817831,-76.62019461,39.32827807,-76.62035286,39.32826340,-76.62034214,39.32838664,-76.62036628,39.32850400,-76.62031800,39.32858029,-76.62038773,39.32865364,-76.62035823,39.32884143,-76.62017584,39.32892945,-76.62004173,39.32900574,-76.62003905,39.32900574,-76.61994785,39.32910844,-76.62006319,39.32916418,-76.62019461,39.32921406,-76.62029386,39.32927568,-76.62043601,39.32929035,-76.62053257,39.32930502,-76.62073910,39.32929328,-76.62100196,39.32929622,-76.62098318,39.32901748,-76.62097514,39.32884730,-76.62083566,39.32883850,-76.62069350,39.32884436,-76.62051648,39.32885023,-76.62045479,39.32884436,-76.62044138,39.32869472,-76.62043869,39.32860963,-76.62041724,39.32854508,-76.62043065,39.32844239,-76.62044138,39.32834849,-76.62041992,39.32827807,-76.62052989,39.32824580,-76.62058353,39.32822819,-76.62057549,39.32804627,-76.62057817,39.32793478,-76.62057281,39.32781741,-76.62055939,39.32767950,-76.62055403,39.32760028,-76.62054062,39.32748291,-76.62053794,39.32737141,-76.62052453,39.32725698};
    int[] time_delta = {5000,11201,15519,19495,23682,28685,33857,38343,42734,48345,52051,57979,61912,66569,72822,79001,82524,88331,94607,98334,103871,109811,115183,120687,124342,129796,135667,140608,145754,149567,153270,157477,162624,166809,171893,178154,184106,189676,194522,198975,203643,209066,213426,217884,221896,225578,231587,237111,241018,247486,252919};
    int[] altitude = {240,248,251,252,262,266,269,276,286,293,299,307,315,323,327,331,341,341,343,353,361,240,239,237,236,237,235,235,233,231,240,240,230,232,232,223,216,206,240,239,240,241,239,237,238,239,240,241,240,239,237};

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    Bluetooth bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt = new Bluetooth(this, mHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMap(View view) {
        Intent intent = new Intent(this, Map.class);

        intent.putExtra("gps coords", gps_data);
        startActivity(intent);
    }

    public void viewStats(View view) {
        Intent intent = new Intent(this, Stats.class);

        intent.putExtra("time data", time_delta);
        intent.putExtra("gps coords", gps_data);
        intent.putExtra("altitude", altitude);

        startActivity(intent);
    }

    public void syncBT(View view) {

        if (mBluetoothAdapter == null) {
            System.out.println("Bluetooth not supported");
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            ArrayList<String> bt_array_list = new ArrayList<String>();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    bt_array_list.add(device.getName() + "\n" + device.getAddress());
                    //System.out.println(bt_array_list.toString());
                }

                // popup dialog to select the BT device
                DialogFragment popup = new BTPairFragment();
                Bundle args = new Bundle();
                args.putStringArrayList("bt_list_opts",bt_array_list);
                popup.setArguments(args);
                popup.show(getFragmentManager(), "bt_select_fragment");
            }
        }

    }

    public void onComplete(String selection) {

        //find the bluetooth device and connect
        for (BluetoothDevice device : pairedDevices) {
            if (selection.contains(device.getAddress())) { //found device mac address from selection

                bt.start();
                bt.connect(device);
                String toast_str = "Connected to " + device.getName();
                Toast toast = Toast.makeText(this, toast_str, Toast.LENGTH_SHORT);
                toast.show();


                /*//generate the socket
                BluetoothSocket btsock = null;

                mBluetoothAdapter.cancelDiscovery();

                try {
                    // INSECURE "8ce255c0-200a-11e0-ac64-0800200c9a66"
                    // SECURE "fa87c0d0-afac-11de-8a39-0800200c9a66"
                    // SPP "0001101-0000-1000-8000-00805F9B34FB"

                    System.out.println("UUID PROTOCOL: " + device.getUuids()[0].getUuid().toString());
                    btsock = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                    if (!btsock.isConnected())
                        btsock.connect();
                    String toast_str = "Connected to " + device.getName();
                    Toast toast = Toast.makeText(this, toast_str, Toast.LENGTH_SHORT);
                    toast.show();

                } catch (IOException connectException) {
                    System.out.println("Connect exception: " + connectException);
                    // Unable to connect; close the socket and get out
                    String fail_toast_str = "Failed connection to " + device.getName();
                    Toast toast = Toast.makeText(this, fail_toast_str, Toast.LENGTH_SHORT);
                    toast.show();

                    try {
                        btsock.close();
                    } catch (IOException closeException) { }
                    return;
                }

                // Do work to manage the connection (in a separate thread)*/

            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    System.out.println("MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    System.out.println("MESSAGE_WRITE ");
                    break;
                case Bluetooth.MESSAGE_READ:
                    System.out.println("MESSAGE_READ ");
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    System.out.println("MESSAGE_DEVICE_NAME " + msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    System.out.println("MESSAGE_TOAST " + msg);
                    break;
            }
        }
    };
}
