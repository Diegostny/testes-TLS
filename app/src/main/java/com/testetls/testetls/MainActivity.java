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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class MainActivity extends Activity {
    private static final String DEBUG_TAG = "Teste-TLS";
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


    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            // ***********************************************************************************//
            /*
            // modifica o Hostname Verifier Default para todas as conexões:
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d(DEBUG_TAG, "Custom_Default_Hostname_Verified: " + hostname);
                    if (hostname.equals("www.facebook.com") || hostname.equals("m.facebook.com"))
                        //if (hostname.equals("www.google.com") || hostname.equals("www.google.com.br"))
                        //if (hostname.equals("www.bradesco.com.br" ))
                        return true;
                    return false;
                }
            });
            */
            // cria uma conexão utilizando o Hostname Verfifier Default:
            HttpsURLConnection conn1 = (HttpsURLConnection) url.openConnection();
            conn1.connect();
            Log.d(DEBUG_TAG, "Conn1: \n" +
                    "Suite: " + conn1.getCipherSuite().toString() + "\n" +
                    "HostName: " + conn1.getHostnameVerifier().toString() + "\n" +
                    "Certs in chain: " + conn1.getServerCertificates().length + "\n" +
                    conn1.getServerCertificates()[0].toString());
            // ***********************************************************************************//

            // cria uma conexão utilizando o Hostname Verfifier Default que é modificado a seguir:
            HttpsURLConnection conn2 = (HttpsURLConnection) url.openConnection();
            // modifica o Hostname Verifier para apenas esta conexão (conn2):
            conn2.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d(DEBUG_TAG, "Custom_Hostname_Verified: " + hostname);
                    if (hostname.equals("www.facebook.com") || hostname.equals("m.facebook.com"))
                    //if (hostname.equals("www.google.com") || hostname.equals("www.google.com.br"))
                    //if (hostname.equals("www.bradesco.com.br"))
                        return true;
                    return false;
                }
            });
            conn2.setReadTimeout(10000 /* milliseconds */);
            conn2.setConnectTimeout(15000 /* milliseconds */);
            conn2.setRequestMethod("GET");
            conn2.setDoInput(true);
            // Starts the query
            conn2.connect();

            // ***********************************************************************************//
            Log.d(DEBUG_TAG, "Conn2: \n" +
                    "Suite: " + conn2.getCipherSuite().toString() + "\n" +
                    "HostName: " + conn2.getHostnameVerifier().toString() + "\n" +
                    "Certs in chain: " + conn2.getServerCertificates().length + "\n" +
                    conn2.getServerCertificates()[0].toString() );
            // ***********************************************************************************//

            int response = conn2.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn2.getInputStream();
            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e){
            e.printStackTrace();
            return e.getMessage().toString();
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
