package com.example.bluetoothtanks;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Sasha on 04.09.13.
 */
public class  SearchDevice extends ListActivity {
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
private ArrayAdapter<String> mArrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {

        mArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_list_search);

        setListAdapter(mArrayAdapter);
// Регистрируем BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        Set<BluetoothDevice> pairedDevices = SimpleBluetoothGame.bluetooth.getBondedDevices();
// Если список спаренных устройств не пуст
        if (pairedDevices.size() > 0) {
            // проходимся в цикле по этому списку
            for (BluetoothDevice device : pairedDevices) {
                // Добавляем имена и адреса в mArrayAdapter, чтобы показать
                // через ListView

                mArrayAdapter.add(device.getName()  +"Paired"+"\n"+ device.getAddress());
            }
        }
        //Toast.makeText(this, pairedDevices.size()+"", Toast.LENGTH_LONG).show();
        setListAdapter(mArrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"SEARCH");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 0:SimpleBluetoothGame.bluetooth.startDiscovery();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        SimpleBluetoothGame.bluetooth.cancelDiscovery();
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17);
       // Toast.makeText(this, address, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
       // Log.v("device","add"+address);
        // Set result and finish this Activity
        setResult(RESULT_OK, intent);
        finish();
        //super.onListItemClick(l, v, position, id);
    }
    @Override
    protected void onDestroy() {
        if (SimpleBluetoothGame.bluetooth != null) {
            SimpleBluetoothGame.bluetooth.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // Когда найдено новое устройство
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Получаем объект BluetoothDevice из интента
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.v("device", "add" + device);

                 }


            }
        }

    };
}

