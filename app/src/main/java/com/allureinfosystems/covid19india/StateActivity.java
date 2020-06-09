package com.allureinfosystems.covid19india;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class StateActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {

    private GestureDetectorCompat gestureDetectorCompat;
    private TextView sStConfirmed;
    private TextView sStActive;
    private TextView sStDeceased;
    private TextView sStRecovered;
    private TextView sStConfirmedIncr;
    private TextView sStActiveIncr;
    private TextView sStDeceasedIncr;
    private TextView sStRecoveredIncr;
    private TextView stateNameTV;
    private String stateName = null;
    static ArrayList<JSONObject> listStates;
    private RecyclerView districtRecyclerView;
    private RecyclerView.Adapter districtAdapter;
    private RecyclerView.LayoutManager districtLayoutManager;
    static ArrayList<HashMap<String, String>> districtData;
    HashMap<String, String> map;
    static JSONArray districtMasterArray;

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
        DownloadDistricts districts = new DownloadDistricts();
        districts.execute("https://api.covid19india.org/v2/state_district_wise.json");
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
                        startActivity(new Intent(StateActivity.this, HelpPopUp.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    case R.id.credit:
                        startActivity(new Intent(StateActivity.this, CreditPopUp.class));
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
        setContentView(R.layout.activity_state);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        stateName = intent.getStringExtra("stateName");

        sStConfirmed = (TextView) findViewById(R.id.sStConfirmed);
        sStActive = (TextView) findViewById(R.id.sStActive);
        sStDeceased = (TextView) findViewById(R.id.sStDeceased);
        sStRecovered = (TextView) findViewById(R.id.sStRecovered);

        sStConfirmedIncr = (TextView) findViewById(R.id.sStConfirmedIncr);
        sStActiveIncr = (TextView) findViewById(R.id.sStActiveIncr);
        sStDeceasedIncr = (TextView) findViewById(R.id.sStDeceasedIncr);
        sStRecoveredIncr = (TextView) findViewById(R.id.sStRecoveredIncr);
        stateNameTV = (TextView) findViewById(R.id.stateNameTV);

        districtRecyclerView = findViewById(R.id.district_recycler_view);
        districtRecyclerView.setHasFixedSize(true);
        districtLayoutManager = new LinearLayoutManager(getApplicationContext());
        districtRecyclerView.setLayoutManager(districtLayoutManager);
        checkConnection();

    }

    private class DownloadDistricts extends AsyncTask<String, Void, String> {
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
                districtMasterArray = new JSONArray(s);
                processDistricts();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void processDistricts() {


        try {
            districtData = new ArrayList<>();
            ArrayList<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < districtMasterArray.length(); i++) {
                JSONObject explrObject = districtMasterArray.getJSONObject(i);
                JSONArray districtArray = explrObject.getJSONArray("districtData");
                if (explrObject.getString("state").equals(stateName)) {
                    for (int j = 0; j < districtArray.length(); j++) {
                        list.add((JSONObject) districtArray.get(j));
                    }
                    Collections.sort(list, new MyJSONComparator());
                }
            }
            for (int i = 0; i < list.size(); i++) {
                JSONObject explrObject = list.get(i);
                map = new HashMap<>();
                map.put("districtName", explrObject.get("district").toString());
                map.put("dConfirm", explrObject.get("confirmed").toString());
                map.put("dActive", explrObject.get("active").toString());
                map.put("dRecovered", explrObject.get("recovered").toString());
                map.put("dDeceased", explrObject.get("deceased").toString());
                map.put("dConfirmedIncr", explrObject.getJSONObject("delta").getString("confirmed"));
                map.put("dDeceasedIncr", explrObject.getJSONObject("delta").getString("deceased"));
                map.put("dActiveIncr", explrObject.getJSONObject("delta").getString("confirmed"));
                map.put("dRecoveredIncr", explrObject.getJSONObject("delta").getString("recovered"));
                districtData.add(map);
            }
            districtAdapter = new DistrictAdapter(districtData);
            districtRecyclerView.setAdapter(districtAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private class DownloadStates extends AsyncTask<String, Void, String> {
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

        if (stateName != null) {
            for (int i = 1; i < listStates.size(); i++) {
                JSONObject explrObject = listStates.get(i);
                if (explrObject.get("state").toString().equals(stateName)) {
                    stateNameTV.setText(explrObject.get("state").toString());
                    sStConfirmed.setText(explrObject.get("confirmed").toString());
                    sStActive.setText(explrObject.get("active").toString());
                    sStRecovered.setText(explrObject.get("recovered").toString());
                    sStDeceased.setText(explrObject.get("deaths").toString());
                    sStConfirmedIncr.setText(explrObject.get("deltaconfirmed").toString());
                    sStActiveIncr.setText(explrObject.get("deltaconfirmed").toString());
                    sStRecoveredIncr.setText(explrObject.get("deltarecovered").toString());
                    sStDeceasedIncr.setText(explrObject.get("deltadeaths").toString());
                    CreatePieChart(Float.parseFloat(explrObject.get("confirmed").toString()), Float.parseFloat(explrObject.get("active").toString()),
                            Float.parseFloat(explrObject.get("recovered").toString()), Float.parseFloat(explrObject.get("deaths").toString()));


                }
            }
        } else {
            JSONObject maxState = listStates.get(1);

            stateNameTV.setText(maxState.get("state").toString());
            sStConfirmed.setText(maxState.get("confirmed").toString());
            sStActive.setText(maxState.get("active").toString());
            sStRecovered.setText(maxState.get("recovered").toString());
            sStDeceased.setText(maxState.get("deaths").toString());
            sStConfirmedIncr.setText(maxState.get("deltaconfirmed").toString());
            sStActiveIncr.setText(maxState.get("deltaconfirmed").toString());
            sStRecoveredIncr.setText(maxState.get("deltarecovered").toString());
            sStDeceasedIncr.setText(maxState.get("deltadeaths").toString());
            CreatePieChart(Float.parseFloat(maxState.get("confirmed").toString()), Float.parseFloat(maxState.get("active").toString()),
                    Float.parseFloat(maxState.get("recovered").toString()), Float.parseFloat(maxState.get("deaths").toString()));

        }


        for (int i = 1; i < listStates.size(); i++) {
            JSONObject explrObject = listStates.get(i);
            if (explrObject.get("state").toString().equals(stateName)) {
                stateNameTV.setText(explrObject.get("state").toString());
                sStConfirmed.setText(explrObject.get("confirmed").toString());
                sStActive.setText(explrObject.get("active").toString());
                sStRecovered.setText(explrObject.get("recovered").toString());
                sStDeceased.setText(explrObject.get("deaths").toString());
                sStConfirmedIncr.setText(explrObject.get("deltaconfirmed").toString());
                sStActiveIncr.setText(explrObject.get("deltaconfirmed").toString());
                sStRecoveredIncr.setText(explrObject.get("deltarecovered").toString());
                sStDeceasedIncr.setText(explrObject.get("deltadeaths").toString());
            }
        }
    }

    private void CreatePieChart(float confirmState, float activeState, float recoveredState, float deadState) {
        PieChart piechart = findViewById(R.id.statePieChart);
        ArrayList<PieEntry> CovidCase = new ArrayList<PieEntry>();
        float activePercent;
        float recoveredPercent;
        float mortalityPercent;

        if (confirmState != 0) {
            activePercent = (activeState / confirmState) * 100;
            recoveredPercent = (recoveredState / confirmState) * 100;
            mortalityPercent = (deadState / confirmState) * 100;
        } else {
            activePercent = 0;
            recoveredPercent = 0;
            mortalityPercent = 0;
        }

        if (activePercent != 0.0f) {
            CovidCase.add(new PieEntry(activePercent, "Active"));
        }

        if (recoveredPercent != 0.0f) {
            CovidCase.add(new PieEntry(recoveredPercent, "Recovery"));
        }


        if (mortalityPercent != 0.0f) {
            CovidCase.add(new PieEntry(mortalityPercent, "Mortality"));
        }


        PieDataSet pieDataset = new PieDataSet(CovidCase, "");
        int[] color = {Color.rgb(216, 28, 28), Color.rgb(76, 175, 80), Color.rgb(254, 174, 101)};
        pieDataset.setColors(color);
        pieDataset.setValueTextColor(Color.WHITE);
        pieDataset.setValueTextSize(10f);

        PieData piData = new PieData(pieDataset);
        piechart.setData(piData);
        piechart.notifyDataSetChanged();
        piechart.invalidate();
        piechart.getDescription().setEnabled(false);
        piechart.getLegend().setEnabled(false);
        piechart.setCenterText("Rates");
        piechart.animate();

    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
