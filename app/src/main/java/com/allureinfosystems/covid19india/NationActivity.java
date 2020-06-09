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
import java.util.Collections;
import java.util.HashMap;

public class NationActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {

    private TextView countryConfirmed;
    private TextView countryActive;
    private TextView countryDeceased;
    private TextView countryRecovered;
    private TextView cConfirmedIncr;
    private TextView cActiveIncr;
    private TextView cDeceasedIncr;
    private TextView cRecoveredIncr;
    private String stateName = null;
    static ArrayList<JSONObject> listStates;
    private RecyclerView stateRecyclerView;
    private RecyclerView.Adapter stateAdapter;
    private RecyclerView.LayoutManager statelayoutManager;
    static ArrayList<HashMap<String, String>> stateData;
    HashMap<String, String> map;

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
        DownloadStates states = new DownloadStates();
        states.execute("https://api.covid19india.org/data.json");
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
                        startActivity(new Intent(NationActivity.this, HelpPopUp.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    case R.id.credit:
                        startActivity(new Intent(NationActivity.this, CreditPopUp.class));
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
        setContentView(R.layout.activity_nation);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        countryConfirmed = (TextView) findViewById(R.id.countryConfirmed);
        countryActive = (TextView) findViewById(R.id.countryActive);
        countryDeceased = (TextView) findViewById(R.id.countryDeceased);
        countryRecovered = (TextView) findViewById(R.id.countryRecovered);

        cConfirmedIncr = (TextView) findViewById(R.id.cConfirmedIncr);
        cActiveIncr = (TextView) findViewById(R.id.cActiveIncr);
        cDeceasedIncr = (TextView) findViewById(R.id.cDeceasedIncr);
        cRecoveredIncr = (TextView) findViewById(R.id.cRecoveredIncr);

        stateRecyclerView = findViewById(R.id.country_recycler_view);
        stateRecyclerView.setHasFixedSize(true);
        statelayoutManager = new LinearLayoutManager(getApplicationContext());
        stateRecyclerView.setLayoutManager(statelayoutManager);

        checkConnection();

    }

    public class DownloadStates extends AsyncTask<String, Void, String> {
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
                JSONObject jsonObject = new JSONObject(s);
                JSONArray stateWise = (JSONArray) jsonObject.get("statewise");
                listStates = new ArrayList<>();

                for (int i = 0; i < stateWise.length(); i++) {
                    listStates.add((JSONObject) stateWise.get(i));
                }
                Collections.sort(listStates, new MyJSONComparator());
                processStates();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    private void processStates() throws JSONException {

        stateData = new ArrayList<>();
        JSONObject country = listStates.get(0);
        countryConfirmed.setText(country.get("confirmed").toString());
        countryActive.setText(country.get("active").toString());
        countryRecovered.setText(country.get("recovered").toString());
        countryDeceased.setText(country.get("deaths").toString());
        cConfirmedIncr.setText(country.get("deltaconfirmed").toString());
        cActiveIncr.setText(country.get("deltaconfirmed").toString());
        cRecoveredIncr.setText(country.get("deltarecovered").toString());
        cDeceasedIncr.setText(country.get("deltadeaths").toString());

        for (int i = 1; i < listStates.size(); i++) {
            JSONObject explrObject = listStates.get(i);
            map = new HashMap<>();
            map.put("stateName", explrObject.get("state").toString());
            map.put("stConfirm", explrObject.get("confirmed").toString());
            map.put("stActive", explrObject.get("active").toString());
            map.put("stRecovered", explrObject.get("recovered").toString());
            map.put("stDeceased", explrObject.get("deaths").toString());
            map.put("stConfirmedIncr", explrObject.get("deltaconfirmed").toString());
            map.put("stDeceasedIncr", explrObject.get("deltadeaths").toString());
            map.put("stActiveIncr", explrObject.get("deltaconfirmed").toString());
            map.put("stRecoveredIncr", explrObject.get("deltarecovered").toString());
            stateData.add(map);

        }
        stateAdapter = new StateAdapter(stateData);
        stateRecyclerView.setAdapter(stateAdapter);
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
