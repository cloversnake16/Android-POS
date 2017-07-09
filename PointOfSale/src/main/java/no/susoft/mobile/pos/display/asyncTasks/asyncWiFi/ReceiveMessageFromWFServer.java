package no.susoft.mobile.pos.display.asyncTasks.asyncWiFi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.content.Context;
import android.os.AsyncTask;

import no.susoft.mobile.pos.ui.activity.MainActivity;

/*
 * Created by Sami on 11/7/2016.
 */

public class ReceiveMessageFromWFServer extends AsyncTask<Void, String, Void> {
	private static final int SERVER_PORT = 4749;
	private Context mContext;
	private ServerSocket socket;

	public ReceiveMessageFromWFServer(Context context){
		mContext = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			socket = new ServerSocket(SERVER_PORT);
			while(true){

				Socket destinationSocket = socket.accept();
				if (destinationSocket != null) {
					InputStream inputStream = destinationSocket.getInputStream();
					BufferedInputStream buffer = new BufferedInputStream(inputStream);
					ObjectInputStream objectIS = new ObjectInputStream(buffer);
					String message = (String) objectIS.readObject();
					destinationSocket.close();
					publishProgress(message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        
		return null;
	}

	@Override
	protected void onCancelled() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onCancelled();
	}

	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		MainActivity.getInstance().messageManager(values[0]);
	}
}
