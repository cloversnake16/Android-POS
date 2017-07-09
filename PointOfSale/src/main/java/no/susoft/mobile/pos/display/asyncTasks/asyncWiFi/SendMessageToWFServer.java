package no.susoft.mobile.pos.display.asyncTasks.asyncWiFi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/*
 * Created by Sami on 11/7/2016.
 */

public class SendMessageToWFServer extends AsyncTask<String, String, String> {
	private static final String TAG = "SendMessageToWFServer";
	private Context mContext;
	private static final int SERVER_PORT = 4748;
	private InetAddress mServerAddr;
	
	public SendMessageToWFServer(Context context, InetAddress serverAddr){
		mContext = context;
		mServerAddr = serverAddr;
	}
	
	@Override
	protected String doInBackground(String... msg) {
		Log.v(TAG, "doInBackground");

		//Send the message
		Socket socket = new Socket();
		try {
			socket.setReuseAddress(true);
			socket.bind(null);
			socket.connect(new InetSocketAddress(mServerAddr, SERVER_PORT));
			Log.v(TAG, "doInBackground: connect succeeded");
			
			OutputStream outputStream = socket.getOutputStream();
			
			new ObjectOutputStream(outputStream).writeObject(msg[0]);
			
		    Log.v(TAG, "doInBackground: send message succeeded");
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if (socket.isConnected()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return msg[0];
	}

	@Override
	protected void onPostExecute(String result) {
		Log.v(TAG, "onPostExecute");
		super.onPostExecute(result);
	}
}
