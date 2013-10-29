package com.example.bluetoothtanks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.example.bluetoothtanks.framework.*;

/**
 * Created by Sasha on 9/11/13.
 */
public class SimpleBluetoothTest extends Activity {
    private GameSurfaceView glView;
public static boolean SERVER;
    PowerManager.WakeLock wl;
    private final static int REQUEST_ENABLE_BT = 1;
    public static BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    public  BluetoothConnect myBluetoothConnect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        // hide statusbar

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        PowerManager powerManager=(PowerManager) getSystemService(this.POWER_SERVICE);
//         wl = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
         wl = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");



     glView = new GameSurfaceView(this,this);

        setContentView(glView);
        myBluetoothConnect = new BluetoothConnect(this);
        if (bluetooth.isEnabled()) {
            // Bluetooth включен. Работаем.
            setupChat();

            myBluetoothConnect.start();
        }
        else
        {
            // Bluetooth выключен. Предложим пользователю включить его.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


    }
        Log.v("GAME", SERVER + "");

//        setupChat();
//        myBluetoothConnect.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

        private void setupChat() {
            Log.v("GAME", SERVER + "");
        if(!SERVER){
            if(BluetoothConnect.mState<2){
           Intent serverIntent = new Intent(this,SearchDevice.class);
            startActivityForResult(serverIntent, 0);
            }
        }else{
//            if (myBluetoothConnect != null) {
//                // Only if the state is STATE_NONE, do we know that we haven't started already
//                if (myBluetoothConnect.getState() == myBluetoothConnect.STATE_NONE) {
                  // Start the Bluetooth chat services
                  // myBluetoothConnect.start();
                }
//            }
//        }

    }


    @Override
    protected void onResume() {
        super.onResume();

          wl.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // wl.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBluetoothConnect != null) myBluetoothConnect.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                Log.d("result++++++++++++++++++++++++++++++++++", "" + requestCode);
        switch (requestCode){

            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(SearchDevice.EXTRA_DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    //Toast.makeText(this, "Try connect to "+address, Toast.LENGTH_LONG).show();

                    BluetoothDevice device = bluetooth.getRemoteDevice(address);
                    myBluetoothConnect.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                setupChat();
               // myBluetoothConnect.start();
                break;
            default:

                break;
        }
        Log.d("result++++++++++++++++++++++++++++++++++", "" + requestCode);

    }
}
