package com.allureinfosystems.covid19india;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GlobalActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {

    private RecyclerView countryrecyclerView;
    static ArrayList<HashMap<String, String>> countryData;
    HashMap<String, String> map;
    private RecyclerView.Adapter countryAdapter;
    private RecyclerView.LayoutManager countrylayoutManager;
    private TextView globalConfirmed;
    private TextView globalConfirmedIncr;
    private TextView globalActive;
    private TextView globalRecovered;
    private TextView globalDeceased;
    private TextView globalDeceasedIncr;

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            executeDetails();
        } else {
            Toast.makeText(this, R.string.checkInternetConnection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        Covid19India.getInstance().setConnectionListener(this);
        executeDetails();
    }


    private void checkConnection() {
        boolean isConnected = ConnectionReceiver.isConnected();
        if (isConnected) {
            executeDetails();
        } else {
            Toast.makeText(this, R.string.checkInternetConnection, Toast.LENGTH_SHORT).show();
        }
    }

    private void executeDetails() {
        DownloadGlobal global = new DownloadGlobal();
        global.execute("https://disease.sh/v2/all?yesterday=false&allowNull=false");
        DownloadCountries countries = new DownloadCountries();
        countries.execute("https://disease.sh/v2/countries?yesterday=false&sort=active&allowNull=false");
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.help:
                        startActivity(new Intent(GlobalActivity.this, HelpPopUp.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    case R.id.credit:
                        startActivity(new Intent(GlobalActivity.this, CreditPopUp.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        globalConfirmed = (TextView) findViewById(R.id.globalConfirmed);
        globalConfirmedIncr = (TextView) findViewById(R.id.globalConfirmedIncr);
        globalActive = (TextView) findViewById(R.id.globalActive);
        globalRecovered = (TextView) findViewById(R.id.globalRecovered);
        globalDeceased = (TextView) findViewById(R.id.globalDeceased);
        globalDeceasedIncr = (TextView) findViewById(R.id.globalDeceasedIncr);
        countryrecyclerView = findViewById(R.id.country_recycler_view);
        countryrecyclerView.setHasFixedSize(true);
        countrylayoutManager = new LinearLayoutManager(getApplicationContext());
        countryrecyclerView.setLayoutManager(countrylayoutManager);

        checkConnection();

    }

    class DownloadGlobal extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = HttpRequest.get(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {

                if (s != null) {

                    JSONObject jsonObject = new JSONObject(s);
                    globalConfirmed.setText(jsonObject.get("cases").toString());
                    globalConfirmedIncr.setText(jsonObject.get("todayCases").toString());
                    globalActive.setText(jsonObject.get("active").toString());
                    globalRecovered.setText(jsonObject.get("recovered").toString());
                    globalDeceased.setText(jsonObject.get("deaths").toString());
                    globalDeceasedIncr.setText(jsonObject.get("todayDeaths").toString());
                } else {
                    globalConfirmed.setText("0");
                    globalConfirmedIncr.setText("0");
                    globalActive.setText("0");
                    globalRecovered.setText("0");
                    globalDeceased.setText("0");
                    globalDeceasedIncr.setText("0");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadCountries extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = HttpRequest.get(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONArray jsonArray = new JSONArray(s);
                countryData = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject explrObject = jsonArray.getJSONObject(i);

                    map = new HashMap<>();
                    map.put("CName", explrObject.get("country").toString());
                    map.put("Confirm", explrObject.get("cases").toString());
                    map.put("Active", explrObject.get("active").toString());
                    map.put("Recovered", explrObject.get("recovered").toString());
                    map.put("Deceased", explrObject.get("deaths").toString());
                    map.put("ConfirmedIncr", explrObject.get("todayCases").toString());
                    map.put("DeceasedIncr", explrObject.get("todayDeaths").toString());
                    countryData.add(map);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            countryAdapter = new CountryAdapter(countryData);
            countryrecyclerView.setAdapter(countryAdapter);


        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
