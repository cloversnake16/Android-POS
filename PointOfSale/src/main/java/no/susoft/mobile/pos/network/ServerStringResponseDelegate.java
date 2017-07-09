package no.susoft.mobile.pos.network;

import java.io.*;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.ResponseHandler;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.ProtocolMismatchActivity;
import org.apache.commons.io.IOUtils;

/**
 * This nested class handles HTTP responses and subsequent JSON serialization.
 */
public final class ServerStringResponseDelegate implements ResponseHandler<String> {

	/**
	 *
	 */
	public ServerStringResponseDelegate() {
		super();
	}

	@Override
	public String handleResponse(HttpResponse response) {
		// Drop to the status code returned by the server.
		switch (response.getStatusLine().getStatusCode()) {
			// 200 OK (HTTP/1.0 - RFC 1945)
			case HttpStatus.SC_OK: {
				Reader reader = null;
				try {
					HttpEntity entity = response.getEntity();
					// Empty response?
					if (entity != null) {
						reader = new InputStreamReader(entity.getContent(), Protocol.CHARSET);

						StringWriter writer = new StringWriter();
						IOUtils.copy(reader, writer);
						entity.consumeContent();
						return writer.toString();
					}
					break;
				} catch (NullPointerException e) {
					// The response was empty?
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (JsonIOException e) {
					// The response was unable to be read.
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (JsonSyntaxException e) {
					// The response is an invalid JSON document.
					e.printStackTrace();
					return null;
				} catch (UnsupportedEncodingException e) {
					// The response encoding was unexpected.
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (IllegalStateException e) {
					// The response is not repeatable and the stream has already been previously obtained.
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					// TODO Error Reporting
					e.printStackTrace();
					return null;
				} finally {
					if (reader != null)
						try {
							reader.close();
						} catch (Exception e) {
						}
				}
			}
            case HttpStatus.SC_NO_CONTENT: {
				return null;
			}
            case HttpStatus.SC_INTERNAL_SERVER_ERROR: {
				return null;
			}
			case HttpStatus.SC_UNAUTHORIZED: {
				ErrorReporter.INSTANCE.filelog("ServerStringResponseDelegate caught HttpStatus.SC_UNAUTHORIZED..");
				AccountManager.INSTANCE.reauthenticateAccounts();
//				// Invalidate the active account.
//				AccountManager.INSTANCE.doActiveAccountInvalidation();
//				MainActivity.getInstance().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						AccountBar.getInstance().showAccountLoginDialog();
//					}
//				});
				return null;
			}
			case Protocol.PROTOCOL_MISMATCH: {
				SusoftPOSApplication.startActivity(ProtocolMismatchActivity.class);
				// Nothing.
				return null;
			}
			default: {
				MainActivity.getInstance().networkUnavailable();
				return null;
			}
		}
		return null;
	}
}