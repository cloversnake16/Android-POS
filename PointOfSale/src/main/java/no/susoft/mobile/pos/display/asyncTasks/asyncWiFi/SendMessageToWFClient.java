package no.susoft.mobile.pos.display.asyncTasks.asyncWiFi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import no.susoft.mobile.pos.display.initThreads.initWFThread.ServerInit;

/*
 * Created by Sami on 11/8/2016.
 */

public class SendMessageToWFClient extends AsyncTask<String, String, String> {
	private static final String TAG = "SendMessageToWFClient";
	private Context mContext;
	private static final int SERVER_PORT = 4749;

	public SendMessageToWFClient(Context context){
		mContext = context;
	}
	
	@Override
	protected String doInBackground(String... msg) {
		Log.v(TAG, "doInBackground");

		//Send the message to clients
		try {

			ArrayList<InetAddress> listClients = ServerInit.clients;
			for(InetAddress addr : listClients){

				Socket socket = new Socket();
				socket.setReuseAddress(true);
				socket.bind(null);
				Log.v(TAG,"Connect to client: " + addr.getHostAddress());
				socket.connect(new InetSocketAddress(addr, SERVER_PORT));
				Log.v(TAG, "doInBackground: connect to "+ addr.getHostAddress() +" succeeded");

				OutputStream outputStream = socket.getOutputStream();

				new ObjectOutputStream(outputStream).writeObject(msg[0]);
			    Log.v(TAG, "doInBackground: write to "+ addr.getHostAddress() +" succeeded");
			    socket.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "send message to client failed");
		}
		
		return msg[0];
	}

	@Override
	protected void onPostExecute(String result) {
		Log.v(TAG, "onPostExecute");
		super.onPostExecute(result);
	}
}