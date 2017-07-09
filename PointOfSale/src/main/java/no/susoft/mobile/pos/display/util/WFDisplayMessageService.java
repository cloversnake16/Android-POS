package no.susoft.mobile.pos.display.util;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import no.susoft.mobile.pos.display.broadcast.WifiDirectBroadcastReceiver;
import no.susoft.mobile.pos.display.asyncTasks.asyncWiFi.ReceiveMessageFromWFClient;
import no.susoft.mobile.pos.display.asyncTasks.asyncWiFi.ReceiveMessageFromWFServer;

/**
 * Created by Sami on 11/8/2016.
 */

public class WFDisplayMessageService extends Service {
	private static final String TAG = "WFDisplayMessageService";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WifiDirectBroadcastReceiver mReceiver = WifiDirectBroadcastReceiver.createInstance();
		
		//Start the AsyncTask for the server to receive messages
        if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER){
        	Log.v(TAG, "Start the AsyncTask for the server to receive messages");
        	new ReceiveMessageFromWFClient(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
        else if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT){
        	Log.v(TAG, "Start the AsyncTask for the client to receive messages");
        	new ReceiveMessageFromWFServer(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
		return START_STICKY;
	}
}
