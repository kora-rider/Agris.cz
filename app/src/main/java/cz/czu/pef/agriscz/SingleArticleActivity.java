package cz.czu.pef.agriscz;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class SingleArticleActivity extends ListActivity {

    private static final String DEBUG_TAG = "DEBUG_TAG";
    FragmentManager FM;

    private ProgressDialog pDialog;

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    // JSON Node names
    private static final String TAG_CLANKY = "clanky";
    private static final String TAG_ID = "id_text";
    private static final String TAG_T_CESTA = "t_cesta";
    private static final String TAG_NAZEV = "title";
    private static final String TAG_PEREX = "perex";
    private static final String TAG_DATUM = "date";
    private static final String TAG_ZDROJ_NAZEV_CZ = "zdroj_nazev_cz";
    private static final String TAG_ZDROJ_NAZEV_EN = "zdroj_nazev_en";
    private static final String TAG_OBRAZEK_SRC = "obrazek_src";
    private static final String TAG_URL = "url";
    private static final String TAG_CESTA = "cesta";
    private static final String TAG_CESTA_ALT = "cesta_alt";
    private static final String TAG_ID_ZDROJ = "id_zdroj";
    private static final String TAG_ROW_NUM = "RowNum";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_article);

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        String id = in.getStringExtra(TAG_ID);

        triggerDownload("http://develop.agris.cz/clanek/"+id+"?vratmi=json");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void triggerDownload(String stringUrl) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask2().execute(stringUrl);
        } else {
            showToast("Připojení k internetu není k dispozici!");
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask2 extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(SingleArticleActivity.this);
            pDialog.setMessage("Načítám data...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... urls) {

            String jsonStr = null;
            String jsonStrEmail = null;
            // params comes from the execute() call: params[0] is the url.
            try {
                jsonStr = downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    String name = jsonObj.getString(TAG_NAZEV);
                    String datum = jsonObj.getString(TAG_DATUM);
                    String perex = jsonObj.getString(TAG_PEREX);

                    // tmp hashmap for single contact
                    HashMap<String, String> contact = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    contact.put(TAG_NAZEV, name);
                    contact.put(TAG_DATUM, datum);
                    contact.put(TAG_PEREX, perex);

                    // adding contact to contact list
                    //contactList.add(contact);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    SingleArticleActivity.this, contactList,
                    R.layout.list_article, new String[] { TAG_NAZEV,
                    TAG_PEREX }, new int[] { R.id.name,
                    R.id.content });

            setListAdapter(adapter);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 100000;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = null;
            contentAsString = readIt(is, len);

            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
