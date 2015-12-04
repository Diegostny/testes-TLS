package com.testetls.testetls;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {
    private static final String DEBUG_TAG = "Teste HTTPS";
    private TextView textView;
    private EditText urlText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.myText);
        urlText = (EditText) findViewById(R.id.myUrl);
        button = (Button) findViewById(R.id.button);
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler(View view) {
        // Gets the URL from the UI's text field.
        String stringUrl = urlText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            textView.setText("Searching...");
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            textView.setText("No network connection available.");
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
    }

    /*
    public class MyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            // pop up an interactive dialog box
            // or insert additional matching logic
            if (hostname == "a248.e.akamai.net") {
                return true;
            } else return false;
        }
    }
    */ /*
    public static X509TrustManager findX509TrustManager(TrustManager[] tms){
        for (  TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager)tm;
            }
        }
        return null;
    } */

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        ///////////////////////////
            //conn.setHostnameVerifier(new MyHostnameVerifier());
            //Log.d(DEBUG_TAG, conn.getPeerPrincipal().toString());
        ///////////////////////////

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();

        ////////////////////////////////
            String dataCert;
            dataCert = "Suite: " + conn.getCipherSuite().toString() + "\n" +
                    "HostName: " + conn.getHostnameVerifier().toString() + "\n" +
                    "Certs in chain: " + conn.getServerCertificates().length + "\n" +
                    conn.getServerCertificates()[0].toString() + "\n" +
                    "Peer Host: " + conn.getPeerPrincipal();
            Log.d(DEBUG_TAG, dataCert);
        ////////////////////////////////

            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
