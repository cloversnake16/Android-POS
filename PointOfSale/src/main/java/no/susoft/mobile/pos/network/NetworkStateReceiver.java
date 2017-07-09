package no.susoft.mobile.pos.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {

	protected List<NetworkStateReceiverListener> listeners;
	protected Boolean connected;

	public NetworkStateReceiver() {
		listeners = new ArrayList<NetworkStateReceiverListener>();
		connected = null;
	}

	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getExtras() == null)
			return;

		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = manager.getActiveNetworkInfo();

		boolean stateChanged = false;
		if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
			if (connected == null || !connected) {
				stateChanged = true;
			}
			connected = true;
		} else if (ni == null || ni.getState() == NetworkInfo.State.DISCONNECTED) {
			if (connected == null || connected) {
				stateChanged = true;
			}
			connected = false;
		} else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
			if (connected == null || connected) {
				stateChanged = true;
			}
			connected = false;
		}

		if (stateChanged) {
			notifyStateToAll();
		}
	}

	public boolean isConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = manager.getActiveNetworkInfo();

		if (ni != null && ni.isConnected()) {
			try {
                Request request = Server.INSTANCE.getEncryptedPreparedRequest();
                request.appendState(Protocol.State.BOOTSTRAP);

				URL url = new URL(request.get());
				HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
				urlc.setRequestProperty("User-Agent", "Android Application");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(10000);
				urlc.connect();

				return (urlc.getResponseCode() == 200);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	private void notifyStateToAll() {
		if (listeners != null) {
			for (NetworkStateReceiverListener listener : listeners) {
				notifyState(listener);
			}
		}
	}

	private void notifyState(NetworkStateReceiverListener listener) {
		if (connected == null || listener == null)
			return;

		if (connected) {
			listener.networkAvailable();
		} else {
			listener.networkUnavailable();
		}
	}

	public void addListener(NetworkStateReceiverListener l) {
		listeners.add(l);
		notifyState(l);
	}

	public void removeListener(NetworkStateReceiverListener l) {
		listeners.remove(l);
	}

	public interface NetworkStateReceiverListener {

		public void networkAvailable();

		public void networkUnavailable();
	}
}