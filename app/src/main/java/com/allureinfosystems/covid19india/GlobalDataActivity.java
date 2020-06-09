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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GlobalDataActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {

    private String Countryname;
    TextView CountryName;
    private TextView cnConfirmed;
    private TextView cnConfirmedIncr;
    private TextView cnActive;
    private TextView cnActiveIncr;
    private TextView cnRecovered;
    private TextView cnRecoveredIncr;
    private TextView cnDeceased;
    private TextView cnDeceasedIncr;

    private TextView casemill;
    private TextView critcal;
    private TextView cnRecovmill;
    private TextView cnDeathmill;
    private TextView cntests;
    private TextView cntestmill;
    private TextView cnpopulation;

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
        DownloadCountries countries = new GlobalDataActivity.DownloadCountries();
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
                        startActivity(new Intent(GlobalDataActivity.this, HelpPopUp.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    case R.id.credit:
                        startActivity(new Intent(GlobalDataActivity.this, CreditPopUp.class));
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
        setContentView(R.layout.activity_global_data);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = getIntent();
        Countryname = intent.getStringExtra("CName");

        CountryName = (TextView) findViewById(R.id.countryName);
        CountryName.setText(Countryname);
        cnConfirmed = (TextView) findViewById(R.id.cnConfirmed);
        cnConfirmedIncr = (TextView) findViewById(R.id.cnConfirmedIncr);
        cnActive = (TextView) findViewById(R.id.cnActive);
        cnActiveIncr = (TextView) findViewById(R.id.cnActiveIncr);
        cnRecovered = (TextView) findViewById(R.id.cnRecovered);
        cnRecoveredIncr = findViewById(R.id.cnRecoveredIncr);
        cnDeceased = (TextView) findViewById(R.id.cnDeceased);
        cnDeceasedIncr = (TextView) findViewById(R.id.cnDeceasedIncr);
        cntests = (TextView) findViewById(R.id.cntest);
        cntestmill = (TextView) findViewById(R.id.cnTestPM);
        casemill = (TextView) findViewById(R.id.cnCasePOM);
        cnRecovmill = (TextView) findViewById(R.id.cnRPOM);
        cnpopulation = (TextView) findViewById(R.id.cnpopulation);
        critcal = findViewById(R.id.cnCritical);
        cnDeathmill = findViewById(R.id.cnDPOM);
        //continent = (TextView) findViewById(R.id.continent);

        checkConnection();
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

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject explrObject = jsonArray.getJSONObject(i);

                    if (Countryname.equals(explrObject.get("country").toString())) {

                        cnConfirmed.setText(explrObject.get("cases").toString());
                        cnConfirmedIncr.setText(explrObject.get("todayCases").toString());
                        cnActive.setText(explrObject.get("active").toString());
                        cnActiveIncr.setText(explrObject.get("todayCases").toString());
                        cnRecovered.setText(explrObject.get("recovered").toString());
                        cnRecoveredIncr.setText(explrObject.get("todayRecovered").toString());
                        cnDeceased.setText(explrObject.get("deaths").toString());
                        cnDeceasedIncr.setText(explrObject.get("todayDeaths").toString());
                        cntests.setText(explrObject.get("tests").toString());
                        cntestmill.setText(explrObject.get("testsPerOneMillion").toString());
                        casemill.setText(explrObject.get("casesPerOneMillion").toString());
                        cnRecovmill.setText(explrObject.get("recoveredPerOneMillion").toString());
                        cnpopulation.setText(explrObject.get("population").toString());
                        critcal.setText(explrObject.get("critical").toString());
                        cnDeathmill.setText(explrObject.get("deathsPerOneMillion").toString());
                        // continent.setText(explrObject.get("continent").toString());


                        CreatePieChart(Float.parseFloat(explrObject.get("cases").toString()), Float.parseFloat(explrObject.get("active").toString()),
                                Float.parseFloat(explrObject.get("recovered").toString()), Float.parseFloat(explrObject.get("deaths").toString()));


                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    private void CreatePieChart(float confirmState, float activeState, float recoveredState, float deadState) {
        PieChart piechart = findViewById(R.id.countryPieChart);
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
