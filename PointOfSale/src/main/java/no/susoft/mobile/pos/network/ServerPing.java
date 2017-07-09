package no.susoft.mobile.pos.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class ServerPing extends AsyncTask<String, Void, Integer> {

	@Override
	protected Integer doInBackground(String... params) {
		String response = null;

		try {
			Request request = Server.INSTANCE.getEncryptedPreparedRequest();
			request.appendState(Protocol.State.BOOTSTRAP);

			URL url = new URL(request.get());
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("User-Agent", "Android Application");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(10000);
			urlc.connect();

			return urlc.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 404;
	}

	@Override
	protected void onPostExecute(Integer responseCode) {
		super.onPostExecute(responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) {
			MainActivity.getInstance().networkAvailable();
		}
	}
}
