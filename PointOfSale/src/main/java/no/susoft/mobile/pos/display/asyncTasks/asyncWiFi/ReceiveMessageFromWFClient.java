package no.susoft.mobile.pos.display.asyncTasks.asyncWiFi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.content.Context;
import android.os.AsyncTask;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/*
 * Created by Sami on 11/8/2016.
 */

public class ReceiveMessageFromWFClient extends AsyncTask<Void, String, Void> {
	private static final int SERVER_PORT = 4748;
	private Context mContext;
	private ServerSocket serverSocket;

	public ReceiveMessageFromWFClient(Context context){
		mContext = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			while(true){
				Socket clientSocket = serverSocket.accept();
				if (clientSocket != null) {
					InputStream inputStream = clientSocket.getInputStream();
					ObjectInputStream objectIS = new ObjectInputStream(inputStream);
					String message = (String) objectIS.readObject();
					clientSocket.close();
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
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onCancelled();
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		MainActivity.getInstance().messageManager(values[0]);
	}
}
