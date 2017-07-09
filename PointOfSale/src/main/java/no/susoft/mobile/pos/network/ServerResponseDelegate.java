package no.susoft.mobile.pos.network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.ResponseHandler;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.ProtocolMismatchActivity;

/**
 * This nested class handles HTTP responses and subsequent JSON serialization.
 *
 * @param <T>
 * @author Yesod
 */
public final class ServerResponseDelegate<T> implements ResponseHandler<T> {

    private final Class<T> type;
    private final Gson gson;

    /**
     * @param type
     */
    public ServerResponseDelegate(Class<T> type, Gson gson) {
        super();
        this.type = type;
        this.gson = gson;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.http.client.ResponseHandler#handleResponse(org.apache.http.HttpResponse)
     */
    @Override
    public T handleResponse(HttpResponse response) {
        // Drop to the status code returned by the server.
//		ErrorReporter.INSTANCE.filelog("response.getStatusLine().getStatusCode() = " + response.getStatusLine().getStatusCode());
		switch (response.getStatusLine().getStatusCode()) {
            // 200 OK (HTTP/1.0 - RFC 1945)
            case HttpStatus.SC_OK: {
                Reader reader = null;
                try {
                    HttpEntity entity = response.getEntity();
                    // Empty response?
                    if (entity != null) {
                        if (type.isAssignableFrom(Bitmap.class)) {
                            //return (T) new BufferedInputStream(entity.getContent());
                            Bitmap b = BitmapFactory.decodeStream(entity.getContent());
                            entity.consumeContent();
                            return type.cast(b);
                        } else {
                            reader = new InputStreamReader(entity.getContent(), Protocol.CHARSET);
                            //return JSONFactory.INSTANCE.getFactory().fromJson(reader, type);
                            T r = this.gson.fromJson(reader, type);
                            entity.consumeContent();
                            return r;
                        }
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