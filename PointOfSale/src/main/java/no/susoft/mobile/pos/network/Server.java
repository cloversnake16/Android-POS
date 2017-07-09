package no.susoft.mobile.pos.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import com.google.gson.Gson;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.config.ConnectionConfig;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.SchemePortResolver;
import cz.msebera.android.httpclient.conn.UnsupportedSchemeException;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.socket.PlainConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.DefaultHostnameVerifier;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.TrustSelfSignedStrategy;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.ssl.SSLContexts;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.data.SynchronizeTransport;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.json.JSONSerializable;
import no.susoft.mobile.pos.ui.activity.MainActivity;

/**
 * This class maintains server-side protocol standards.
 *
 * @author Yesod, Mihail
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public enum Server {

    INSTANCE;
	private static String LOCAL = "192.168.1.105";
	private static String TEST = "85.200.224.62";
	private static String PRODUCTION = "81.27.43.30";
    private static String LOCALMM = "192.168.1.111";

	public final static String authority = PRODUCTION;
	
    public final static String path1 = "suservletroot";
    private final static String path2 = "no.susoft.mobile.pos.network.Android";
    private CloseableHttpClient closableClient;
	private final static boolean developmentMode = true;

    private Server() {
        closableClient = buildHttpClient();
    }

    public static CloseableHttpClient buildHttpClient() {

        CloseableHttpClient client;

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setCharset(Charset.forName("UTF-8"))
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(20000)
                .build();

        SchemePortResolver schemePortResolver = new SchemePortResolver() {

            @Override
            public int resolve(HttpHost host) throws UnsupportedSchemeException {

                switch (host.getSchemeName().toLowerCase()) {
                    default:
                    case "http":
                        return 80;
                    case "https":
                        return 8443;
                }
            }
        };

        try {

            // region SSL config
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream instream = SusoftPOSApplication.getContext().getResources().openRawResource(R.raw.ssl_keystore);

            try {
                trustStore.load(instream, "e3GQSjb9jisWvfAlihBG".toCharArray());
            } finally {
                instream.close();
            }

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    SSLContexts.custom()
                            .loadTrustMaterial(trustStore, TrustSelfSignedStrategy.INSTANCE)
                            .build(),
                    new String[]{"TLSv1", "SSLv3"}, null, new DefaultHostnameVerifier());
            // endregion

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslConnectionSocketFactory)
                    .register("http", PlainConnectionSocketFactory.getSocketFactory()).build();

            PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
            poolingHttpClientConnectionManager.setMaxTotal(512);
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(16);
            poolingHttpClientConnectionManager.setDefaultConnectionConfig(connectionConfig);

            client = HttpClients.custom()
                    .setSchemePortResolver(schemePortResolver)
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultConnectionConfig(connectionConfig)
                    .setConnectionManager(poolingHttpClientConnectionManager)
                    .build();

        } catch (Exception e) {
            // region build simple client
            e.printStackTrace();
            client = HttpClients.custom()
                    .setSchemePortResolver(schemePortResolver)
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultConnectionConfig(connectionConfig)
                    .build();
            // endregion
        }

        return client;
    }

    /**
     * Get a prepared request that transmits of an encrypted transport layer.
     *
     * @return
     */
    public Request getEncryptedPreparedRequest() {
        if (developmentMode) {
            return getUnencryptedPreparedRequest();
        }
        Uri.Builder uri = Uri.parse(String.format("https://%1$s:%2$s", authority, "8443")).buildUpon();
        uri.appendPath(Server.path1);
        uri.appendPath(Server.path2);
        // ...
        Request request = new Request(uri);
        return request;
    }

    /**
     * Get a prepared request that transmits of an unencrypted transport layer.
     *
     * @return
     */
    @Deprecated
    public Request getUnencryptedPreparedRequest() {
        Uri.Builder uri = Uri.parse(String.format("http://%1$s", authority)).buildUpon();
        uri.appendPath(Server.path1);
        uri.appendPath(Server.path2);
        // ...
        Request request = new Request(uri);
        return request;
    }

    /**
     * Get the default prepared request which may or may not be encrypted.
     *
     * @return
     */
    public Request getDefaultPreparedRequest() {
        return this.getEncryptedPreparedRequest();
    }

    /**
     * Process the request that is expected to return a bitmap.
     *
     * @return
     */
    public Bitmap getBitmap(Request request) {
        try {
            return this.closableClient.execute(new HttpGet(request.get()), new ServerResponseDelegate<>(Bitmap.class, null));
        } catch (Exception x) {
            x.printStackTrace();
            ErrorReporter.INSTANCE.filelog("Server.doPost()", x.getMessage(), x);
            MainActivity.getInstance().networkUnavailable();
            return null;
        }
    }

    /**
     * Get the server response object of the given type based on the given request protocol.
     *
     * @return
     */
    public <T extends JSONSerializable> T doGet(Gson gson, Request request, Class<T> response) {
        try {
//			ErrorReporter.INSTANCE.filelog("request.get() = " + request.get());
            return this.closableClient.execute(new HttpGet(request.get()), new ServerResponseDelegate<T>(response, gson));
        } catch (Exception x) {
            ErrorReporter.INSTANCE.filelog("Server.doPost()", x.getMessage(), x);
            MainActivity.getInstance().networkUnavailable();
            return null;
        }
    }

    /**
     * Get the server response as string.
     *
     * @return
     */
    public String doGet(Request request) {
        try {
//			ErrorReporter.INSTANCE.filelog("request.get() = " + request.get());
            return this.closableClient.execute(new HttpGet(request.get()), new ServerStringResponseDelegate());
        } catch (Exception x) {
            ErrorReporter.INSTANCE.filelog("Server.doGet()", x.getMessage(), x);
            MainActivity.getInstance().networkUnavailable();
            return null;
        }
    }

    /**
     * @param response
     * @param request
     * @param entity
     * @return
     */
    public <T extends JSONSerializable> T doPost(Class<T> response, Request request, MultipartEntityBuilder entity, Gson gson) {
        try {
//			ErrorReporter.INSTANCE.filelog("request.get() = " + request.get());
            return this.closableClient.execute(this.getPost(request, entity), new ServerResponseDelegate<T>(response, gson));
        } catch (Exception x) {
            ErrorReporter.INSTANCE.filelog("Server.doPost()", x.getMessage(), x);
            MainActivity.getInstance().networkUnavailable();
            return null;
        }
    }

    /**
     * @param request
     * @param entity
     * @return
     */
    private HttpPost getPost(Request request, MultipartEntityBuilder entity) {
        HttpPost post = new HttpPost(request.get());
        post.setEntity(entity.build());
        return post;
    }

    /**
     * Get the server response as string.
     *
     * @return
     */
    public SynchronizeTransport doSyncronize(Request request) {
        try {
            return this.closableClient.execute(new HttpGet(request.get()), new ServerSynchronizeResponseDelegate());
        } catch (Exception x) {
            ErrorReporter.INSTANCE.filelog("Server.doGet()", x.getMessage(), x);
			MainActivity.getInstance().networkUnavailable();
            return null;
        }
    }
    
    /**
     * Return whether there is either a wifi or Internet connection available.
     *
     * @return
     */
    @Deprecated
    public boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) SusoftPOSApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    /**
     * Return whether there is either a wifi or Internet connection available.
     *
     * @return
     */
    public boolean isNetworkActive() {
		NetworkInfo ni = ((ConnectivityManager) SusoftPOSApplication.getContext()
													.getSystemService(Context.CONNECTIVITY_SERVICE))
							 						.getActiveNetworkInfo();
	
		return ni != null && ni.isConnected();
	}
}