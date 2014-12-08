package cz.czu.pef.agriscz;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ListActivity {

    private static final String DEBUG_TAG = "DEBUG_TAG";
    FragmentManager FM;

    private ProgressDialog pDialog;

    // JSON Node names
    private static final String TAG_CLANKY = "clanky";
    private static final String TAG_ID = "id_text";
    private static final String TAG_T_CESTA = "t_cesta";
    private static final String TAG_NAZEV = "nazev";
    private static final String TAG_PEREX = "perex";
    private static final String TAG_DATUM = "datum";
    private static final String TAG_ZDROJ_NAZEV_CZ = "zdroj_nazev_cz";
    private static final String TAG_ZDROJ_NAZEV_EN = "zdroj_nazev_en";
    private static final String TAG_OBRAZEK_SRC = "obrazek_src";
    private static final String TAG_URL = "url";
    private static final String TAG_CESTA = "cesta";
    private static final String TAG_CESTA_ALT = "cesta_alt";
    private static final String TAG_ID_ZDROJ = "id_zdroj";
    private static final String TAG_ROW_NUM = "RowNum";

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListOfNewsFragment listFragment = ListOfNewsFragment.newInstance();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, listFragment)
                    .commit();
            // TODO: Naplnit oba spinnery od 1 do 10
        }

        triggerDownload("http://develop.agris.cz/dalsi-novinky?vratmi=json");
        //triggerDownload("http://najdi-hospodyni.cz/clanky.txt");

        contactList = new ArrayList<HashMap<String, String>>();
        ListView lv = getListView();

        // Listview on item click listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String name = ((TextView) view.findViewById(R.id.name))
                        .getText().toString();
                String contactId = ((TextView) view.findViewById(R.id.id))
                        .getText().toString();
                String perex = ((TextView) view.findViewById(R.id.perex))
                        .getText().toString();

                // Starting single contact activity
                Intent in = new Intent(getApplicationContext(),
                        SingleArticle.class);
                in.putExtra(TAG_NAZEV, name);
                in.putExtra(TAG_ID, contactId);
                in.putExtra(TAG_PEREX, perex);
                startActivity(in);

            }
        });
    }

    public void startSettings(MenuItem item) {
        FM = getFragmentManager();
        FragmentTransaction FT = FM.beginTransaction();
        SettingsFragment settings = new SettingsFragment();
        FT.replace(R.id.container, settings);
        FT.addToBackStack(null);
        FT.commit();
    }

    public void onClicknastav(View view) {

        Spinner mySpinner=(Spinner) findViewById(R.id.nonews);
        Spinner mySpinner1=(Spinner) findViewById(R.id.nooffline);
        int cislo = mySpinner.getSelectedItemPosition();
        int cislo_offline = mySpinner1.getSelectedItemPosition();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("pocet_clanku_online", cislo+1);
        editor.putInt("pocet_clanku_offline", cislo_offline+1);
        editor.apply();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            showToast("Připojení k internetu není k dispozici!");
        }

    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, Void> {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int pocet = sharedPreferences.getInt("pocet_clanku_online", 0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Načítám data...");
            pDialog.setCancelable(false);
            pDialog.show();

        }


        @Override
        protected Void doInBackground(String... urls) {

            // Creating service handler class instance
            ServiceHandle sh = new ServiceHandle();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(urls[0], ServiceHandle.GET);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    contacts = jsonObj.getJSONArray(TAG_CLANKY);

                    // looping through All articles
                    // for (int i = 0; i < contacts.length(); i++) {- zaloha
                    for (int i = 0; i < pocet; i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAZEV);
                        String datum = c.getString(TAG_DATUM);
                        datum = datum.replace("/Date(", "").replace(")/", "");
                        long date = Long.parseLong(datum);
                        datum = getDate(date, "dd.MM.yyyy | HH:mm:ss");
                        String perex = c.getString(TAG_PEREX);

                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_NAZEV, name);
                        contact.put(TAG_DATUM, datum);
                        contact.put(TAG_PEREX, perex);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
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
                    MainActivity.this, contactList,
                    R.layout.fragment_list_of_news, new String[] { TAG_NAZEV,
                    TAG_DATUM, TAG_ID, TAG_PEREX }, new int[] { R.id.name,
                    R.id.datum, R.id.id, R.id.perex });

            setListAdapter(adapter);
        }
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
