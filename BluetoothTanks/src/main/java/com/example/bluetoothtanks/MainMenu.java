package com.example.bluetoothtanks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Sasha on 9/15/13.
 */

public class MainMenu extends Activity implements View.OnClickListener{
   Button buttonServer;
    Button buttonClient;

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonClient =(Button) findViewById(R.id.button2);
        buttonServer=(Button)findViewById(R.id.button);
        buttonServer.setOnClickListener(this);
        buttonClient.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button :
                intent=new Intent(this,SimpleBluetoothGame.class);
                SimpleBluetoothGame.SERVER=true;
                startActivity(intent);
                break;
            case R.id.button2:
                intent=new Intent(this,SimpleBluetoothGame.class);
                SimpleBluetoothGame.SERVER=false;
                startActivity(intent);
                break;
            default:
                break;
        }

    }
}
