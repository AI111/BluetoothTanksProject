package com.example.bluetoothtanks;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.bluetoothtanks.framework.Positions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Sasha on 07.09.13.
 */

public class BluetoothConnect {

    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    private static final String NAME = "BluetoothChat";
    public static volatile float[] READ_ARREY =new float[45] ;//{ 4.0f, 6.0f, -100, -100,50.0f,60.0f};

    public static volatile float[] WRITE_ARREY=new float[45];// { 2.0f, 5.0f, -100, -100,50.0f,60.0f };
    public static boolean SERVER = true;

    private static final UUID MY_UUID=
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;

    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    public static volatile int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device



    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context

     */
    public BluetoothConnect(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        Arrays.fill(READ_ARREY, -100);
        Arrays.fill(WRITE_ARREY, -100);

        READ_ARREY[0]=3;
        READ_ARREY[1]=3;
        READ_ARREY[2]=3;
        WRITE_ARREY[0]=3;
        WRITE_ARREY[1]=3;
        WRITE_ARREY[2]=3;
    }
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");
        Positions.SERVER=true;
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread =new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
       // mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect

     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        SERVER=false;
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        if (D) Log.d(TAG, "connected, Socket");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
//        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
      //  SERVER=false;
        setState(STATE_CONNECTED);

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     //* @param out The bytes to write
     * @see
     */
    public void write() {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(MainActivity.TOAST, "Unable to connect device");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        if (D) Log.d(TAG, "connectionFailed()");
        // Start the service over to restart listening mode
       // BluetoothConnect.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(MainActivity.TOAST, "Device connection was lost");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        if (D) Log.d(TAG, "connectionLost()");
        // Start the service over to restart listening mode
       // BluetoothConnect.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */







    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;


        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            SERVER=true;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME, MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothConnect.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: ");

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;


            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp= device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnect.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        long timeStart1 =0,timeStart2=0;
//        ObjectOutputStream oos;
//        ObjectInputStream ois;

        FloatBuffer floatInBufferf;
        ByteBuffer byteOutBuffer;
        FloatBuffer floatOutBuffer;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread: ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {

                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
             byteOutBuffer = ByteBuffer.allocate(WRITE_ARREY.length * 4);
             floatOutBuffer = byteOutBuffer.order(ByteOrder.BIG_ENDIAN).asFloatBuffer();



        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[READ_ARREY.length*4];
            floatInBufferf = ByteBuffer.wrap(buffer).asFloatBuffer();


            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    timeStart1= System.currentTimeMillis();
                    // Read from the InputStream
                    Log.d("ARRAY",Arrays.toString(READ_ARREY));

                    bytes = mmInStream.read(buffer);

                    floatInBufferf.get(READ_ARREY);

                    floatInBufferf.clear();
                    Log.d("READ_ARREY", "" + (System.currentTimeMillis() - timeStart1));


                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothConnect.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
     //    * @param buffer  The bytes to write
         */
        public void write() {

            try {
                timeStart2= System.currentTimeMillis();


                floatOutBuffer.put(WRITE_ARREY);

                mmOutStream.write(byteOutBuffer.array());

                byteOutBuffer.clear();
                floatOutBuffer.clear();
                // Share the sent message back to the UI Activity

               Log.v("WRITE_ARREY",""+ (System.currentTimeMillis()-timeStart2));
                timeStart2=0;
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
