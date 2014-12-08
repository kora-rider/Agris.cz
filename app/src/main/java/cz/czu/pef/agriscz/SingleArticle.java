package cz.czu.pef.agriscz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class SingleArticle extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_article);

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        String name = in.getStringExtra(TAG_NAZEV);
        String perex = in.getStringExtra(TAG_PEREX);
        String id = in.getStringExtra(TAG_ID);

        // Displaying all values on the screen
        TextView lblName = (TextView) findViewById(R.id.name_label);
        TextView lblPerex = (TextView) findViewById(R.id.perex_label);
        TextView lblId = (TextView) findViewById(R.id.id_label);

        lblName.setText(name);
        lblPerex.setText(perex);
        lblId.setText(id);
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
}
