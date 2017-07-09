package no.susoft.mobile.pos.ui.activity;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.error.ErrorReporter;

public class WebViewActivity extends BaseActivity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        webView = (WebView) findViewById(R.id.webview);
        Button btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Closing SecondScreen Activity
                finish();
            }
        });

        String url = getIntent().getStringExtra("url");
		Object jsInterface = getIntent().getSerializableExtra("jsInterface");
		ErrorReporter.INSTANCE.filelog("WebViewActivity url = " + url);

        if (url != null && url.length() > 0) {
            startWebView(url, jsInterface);
        } else {
            finish();
        }
    }

	@SuppressLint("JavascriptInterface")
    private void startWebView(String url, Object jsInterface) {

        //Create new webview Client to show progress dialog
        //When opening a url or click on link
        webView.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog;

            //If you will not use this method url links are opeen in new brower not in webview
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Map<String, String> extraHeaders = new HashMap<String, String>();
				extraHeaders.put("Content-Type", "text/html; charset=iso-8859-15");
				view.loadUrl(url, extraHeaders);
                return true;
            }

            //            //Show loader on url load
            //            public void onLoadResource (WebView view, String url) {
            //                if (progressDialog == null) {
            //                    // in standard case YourActivity.this
            //                    progressDialog = new ProgressDialog(WebViewActivity.this);
            //                    progressDialog.setMessage("Loading...");
            //                    progressDialog.show();
            //                }
            //            }
            //            public void onPageFinished(WebView view, String url) {
            //                try{
            //                if (progressDialog.isShowing()) {
            //                    progressDialog.dismiss();
            //                    progressDialog = null;
            //                }
            //                }catch(Exception exception){
            //                    exception.printStackTrace();
            //                }
            //            }

        });

        // Javascript inabled on webview
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

		if (jsInterface != null) {
			webView.addJavascriptInterface(jsInterface, "SusoftPOSAndroid");
		}

        webView.loadUrl(url);
    }

    // Open previous opened link from history on webview when back button pressed

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }

}