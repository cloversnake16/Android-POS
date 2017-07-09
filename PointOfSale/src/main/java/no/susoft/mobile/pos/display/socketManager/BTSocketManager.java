package no.susoft.mobile.pos.display.socketManager;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * Created by Sami on 12/24/2016.
 */

public class BTSocketManager {

    public static final int BODY_LENGTH_END = 255;
    public static final int BODY_LENGTH_END_SIGNED = -1;

    public static final int MESSAGE_ID = 1;
    public static final int MESSAGE_NAME = 2;
    public static final int MESSAGE_SEND = 3;
    public static final int MESSAGE_RECEIVE = 4;

    private boolean isHost;
    private ArrayList<ConnectedThread> connections;
    private int id;

    private Activity mActivity;

    private ConnectedThread mConnectedThread;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            byte[] packet = (byte[]) msg.obj;
            int senderLength = msg.arg1;
            int senderId = msg.arg2;

            String sender = new String(Arrays.copyOfRange(packet, 0, senderLength));
            byte[] body = Arrays.copyOfRange(packet, senderLength, packet.length);

            switch (msg.what) {
                case MESSAGE_ID:
                    id = body[0];
                case MESSAGE_SEND:
                    if (isHost) {
                        byte[] sendPacket = buildPacket(MESSAGE_SEND, senderId, sender, body);
                        writeMessage(sendPacket, senderId);
                    }
                    break;
                case MESSAGE_RECEIVE:
                    MainActivity.getInstance().receiveMessageFromSecondaryBTDisplay(new String(body));
                    break;
            }
        }

    };

    public BTSocketManager(Activity activity, boolean isHost) {
        mActivity = activity;
        this.isHost = isHost;

        if (isHost) {
            id = 0;
            connections = new ArrayList<>();
        }

    }

    public void startConnection(BluetoothSocket socket) {
        String username = "Susoft";
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        if (isHost) {

            connections.add(mConnectedThread);
            byte[] idAssignmentPacket = buildPacket(
                    MESSAGE_ID,
                    username,
                    new byte[] { (byte) connections.size() }
            );
            mConnectedThread.write(idAssignmentPacket);
        }
    }

    public byte[] buildPacket(int type, int senderId, String sender, byte[] body) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(type);
        output.write(sender.length());

        int bodyLength = body.length;
        do {
           output.write(bodyLength % 10);
           bodyLength = bodyLength / 10;
        } while (bodyLength > 0);

        try {
            output.write(BODY_LENGTH_END);
            output.write(senderId);
            output.write(sender.getBytes());
            output.write(body);
        } catch (IOException e) {
            System.err.println("Error in building packet.");
            return null;
        }

        return output.toByteArray();
    }

    public byte[] buildPacket(int type, String sender, byte[] body) {
        return buildPacket(type, id, sender, body);
    }

    public void writeChatRoomName(byte[] byteArray) {
        connections.get(connections.size() - 1).write(byteArray);
    }

    public void writeMessage(byte[] byteArray, int senderId) {
        int type = byteArray[0];
        int receiveType = 0;
        if (type == MESSAGE_SEND) {
            receiveType = MESSAGE_RECEIVE;
        }

        int currIndex = 2;
        do {
            currIndex++;
        } while (byteArray[currIndex] != BODY_LENGTH_END_SIGNED);

        if (isHost) {
            new DistributeThread(receiveType, senderId, byteArray).start();
        } else {
            mConnectedThread.write(byteArray);
        }
    }

    public void writeMessage(byte[] byteArray) {
        writeMessage(byteArray, id);
    }

    private class DistributeThread extends Thread {

        int mReceiveType;
        int mSenderId;
        private byte[] mByteArray;

        public DistributeThread(int receiveType, int senderId, byte[] byteArray) {
            mReceiveType = receiveType;
            mSenderId = senderId;
            mByteArray = byteArray;
        }

        public void run() {
            mByteArray[0] = (byte) mReceiveType;
            if(connections.size()> 0) {
                connections.get(connections.size()-1).write(mByteArray);
            }

        }

    }

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e1) {
                }

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            while (true) {
                try {
                    int type = mmInStream.read();
                    int senderLength = mmInStream.read();

                    int bodyLength = 0;
                    int currPlace = 1;
                    int currDigit = mmInStream.read();
                    do {
                        bodyLength += (currDigit * currPlace);
                        currPlace *= 10;
                        currDigit = mmInStream.read();
                    } while (currDigit != BODY_LENGTH_END);

                    int senderId = mmInStream.read();

                    ByteArrayOutputStream packetStream = new ByteArrayOutputStream();
                    for (int i = 0; i < senderLength + bodyLength; i++) {
                        packetStream.write(mmInStream.read());
                    }
                    byte[] packet = packetStream.toByteArray();

                    mHandler.obtainMessage(type, senderLength, senderId, packet)
                            .sendToTarget();
                } catch (IOException e) {
                    System.err.println("Error in receiving packets");
                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "Disconnected with a Display", Toast.LENGTH_SHORT).show();
                            MainActivity.getInstance().initBT();
                        }
                    });
                    break;
                }
            }
        }

        public void write(byte[] byteArray) {
            try {
                mmOutStream.write(byteArray);
                mmOutStream.flush();
            } catch (IOException e) {
                String byteArrayString = "";
                for (byte b : byteArray) {
                    byteArrayString += b + ", ";
                }
                System.err.println("Failed to write bytes: " + byteArrayString);
                System.err.println(e.toString());
            }
        }

    }

}
