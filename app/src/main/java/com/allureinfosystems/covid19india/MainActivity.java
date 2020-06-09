package com.allureinfosystems.covid19india;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {

    protected LocationManager locationManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> myDataset;

    private TextView currentLocation;
    private TextView currentLocationZone;
    private TextView locationConfirmed;
    private TextView locationActive;
    private TextView locationDeceased;
    private TextView locationRecovered;
    private TextView stateNameTextView;
    private TextView stateConfirmed;
    private TextView stateActive;
    private TextView stateDeceased;
    private TextView stateRecovered;
    private TextView stateConfPMill;
    private TextView stateTestPMill;

    private TextView lConfirmedIncr;
    private TextView lActiveIncr;
    private TextView lDeceasedIncr;
    private TextView lRecoveredIncr;
    private TextView sConfirmedIncr;
    private TextView sActiveIncr;
    private TextView sDeceasedIncr;
    private TextView sRecoveredIncr;
    ConstraintLayout mainConstraintLayout;
    private double latitude;
    private double longitude;
    private String districtName = null;
    private String stateName = null;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private Location net_loc = null, gps_loc = null, finalLoc = null;
    static JSONArray districtMasterArray;
    static ArrayList<JSONObject> listStates;

    private Button globalButton;
    private Button nationButton;
    private Button refreshButton;

    HashMap<String, Integer> populationmap;

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
        checkConnection();
    }


    private void checkConnection() {
        boolean isConnected = ConnectionReceiver.isConnected();
        if (isConnected) {
            executeDetails();
        } else {
            Toast.makeText(this, R.string.checkInternetConnection, Toast.LENGTH_SHORT).show();
        }
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
                        startActivity(new Intent(MainActivity.this, HelpPopUp.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    case R.id.credit:
                        startActivity(new Intent(MainActivity.this, CreditPopUp.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<String> myDataset;
        TextView currentTextView;

        public MyAdapter(ArrayList<String> myDataset, TextView current, Context context) {
            this.myDataset = myDataset;
            this.currentTextView = current;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.districts, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull final MyAdapter.MyViewHolder holder, int position) {

            holder.mTitle.setText(myDataset.get(position));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String selectDistrict = myDataset.get(holder.getAdapterPosition());
                    currentTextView.setText(selectDistrict);
                    changeDistrict(selectDistrict);
                }
            });

        }

        @Override
        public int getItemCount() {
            return myDataset.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView mTitle;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.mTitle);
            }
        }
    }


    public void changeDistrict(String district) {
        districtName = district;
        initDistTextViews();
        DownloadZones districtZone = new DownloadZones();
        districtZone.execute("https://api.covid19india.org/zones.json");
        DownloadDistricts districts = new DownloadDistricts();
        districts.execute("https://api.covid19india.org/v2/state_district_wise.json");
    }

    private void initDistTextViews() {
        currentLocation = (TextView) findViewById(R.id.textLocation);
        currentLocationZone = (TextView) findViewById(R.id.textZone);
        locationConfirmed = (TextView) findViewById(R.id.locationConfirmed);
        locationActive = (TextView) findViewById(R.id.locationActive);
        locationDeceased = (TextView) findViewById(R.id.locationDeceased);
        locationRecovered = (TextView) findViewById(R.id.locationRecovered);
        lConfirmedIncr = (TextView) findViewById(R.id.lConfirmedIncr);
        lActiveIncr = (TextView) findViewById(R.id.lActiveIncr);
        lDeceasedIncr = (TextView) findViewById(R.id.lDeceasedIncr);
        lRecoveredIncr = (TextView) findViewById(R.id.lRecoveredIncr);

    }

    // oncreate   ....


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        createPopulationMap();
        initDistTextViews();

        stateNameTextView = (TextView) findViewById(R.id.textViewState);
        stateConfirmed = (TextView) findViewById(R.id.stateConfirmed);
        stateActive = (TextView) findViewById(R.id.stateActive);
        stateDeceased = (TextView) findViewById(R.id.stateDeceased);
        stateRecovered = (TextView) findViewById(R.id.stateRecovered);
        mainConstraintLayout = (ConstraintLayout) findViewById(R.id.mainConstraintLayout);
        globalButton = (Button) findViewById(R.id.buttonglobal);
        nationButton = (Button) findViewById(R.id.buttonnation);
        refreshButton = (Button) findViewById(R.id.buttonrefresh);
        globalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GlobalActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        nationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkConnection();

            }
        });


        mainConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        sConfirmedIncr = (TextView) findViewById(R.id.sConfirmedIncr);
        sActiveIncr = (TextView) findViewById(R.id.sActiveIncr);
        sDeceasedIncr = (TextView) findViewById(R.id.sDeceasedIncr);
        sRecoveredIncr = (TextView) findViewById(R.id.sRecoveredIncr);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        myDataset = new ArrayList<String>();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                mainConstraintLayout.setOnTouchListener(new OnSwipeTouchListener() {

                    @Override
                    public boolean onSwipeLeft() {
                        Intent intent = new Intent(MainActivity.this, NationActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true;
                    }

                    @Override
                    public boolean onSwipeRight() {
                        Intent intent = new Intent(MainActivity.this, GlobalActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    }

                    @Override
                    public boolean onSwipeBottom() {
                        // TODO Auto-generated method stub
                        return true;
                    }

                    @Override
                    public boolean onSwipeTop() {
                        // TODO Auto-generated method stub
                        return true;
                    }

                });
                checkConnection();
            }
        } else {
            mainConstraintLayout.setOnTouchListener(new OnSwipeTouchListener() {

                @Override
                public boolean onSwipeLeft() {
                    Intent intent = new Intent(MainActivity.this, NationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }

                @Override
                public boolean onSwipeRight() {
                    Intent intent = new Intent(MainActivity.this, GlobalActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                }

                @Override
                public boolean onSwipeBottom() {
                    // TODO Auto-generated method stub
                    return true;
                }

                @Override
                public boolean onSwipeTop() {
                    // TODO Auto-generated method stub
                    return true;
                }

            });
            checkConnection();
        }
    }

    private void executeDetails() {
        getLocationCoordinates();
        if (districtName != null) {
            DownloadZones districtZone = new DownloadZones();
            districtZone.execute("https://api.covid19india.org/zones.json");
        }
        DownloadStates states = new DownloadStates();
        states.execute("https://api.covid19india.org/data.json");
        DownloadDistricts districts = new DownloadDistricts();
        districts.execute("https://api.covid19india.org/v2/state_district_wise.json");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mainConstraintLayout.setOnTouchListener(new OnSwipeTouchListener() {

                @Override
                public boolean onSwipeLeft() {
                    Intent intent = new Intent(MainActivity.this, NationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }

                @Override
                public boolean onSwipeRight() {
                    Intent intent = new Intent(MainActivity.this, GlobalActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                }

                @Override
                public boolean onSwipeBottom() {
                    // TODO Auto-generated method stub
                    return true;
                }

                @Override
                public boolean onSwipeTop() {
                    // TODO Auto-generated method stub
                    return true;
                }

            });
            checkConnection();
        }
    }

    private void getLocationCoordinates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (gps_enabled)
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled)
                net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (gps_loc != null && net_loc != null) {

            //smaller the number more accurate result will
            if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                finalLoc = net_loc;
            else
                finalLoc = gps_loc;

        } else {

            if (gps_loc != null) {
                finalLoc = gps_loc;
            } else if (net_loc != null) {
                finalLoc = net_loc;
            }
        }
        if (finalLoc != null) {
            latitude = finalLoc.getLatitude();
            longitude = finalLoc.getLongitude();
            getLocationDetails(latitude, longitude);
        }
    }

    // Get GetLocation...............//


    private void getLocationDetails(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> listAddress = geocoder.getFromLocation(latitude, longitude, 1);

            if (listAddress != null && listAddress.size() > 0) {

                if (listAddress.get(0).getSubAdminArea() != null) {
                    districtName = listAddress.get(0).getSubAdminArea();

                }

                if (listAddress.get(0).getAdminArea() != null) {
                    stateName = listAddress.get(0).getAdminArea();
                }
            }

        } catch (Exception e) {


            e.printStackTrace();
        }
    }

    public class DownloadDistricts extends AsyncTask<String, Void, String> {
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

    private class DownloadZones extends AsyncTask<String, Void, String> {
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
                JSONArray districtZones = (JSONArray) jsonObject.get("zones");
                ArrayList<JSONObject> list = new ArrayList<>();

                for (int i = 0; i < districtZones.length(); i++) {
                    list.add((JSONObject) districtZones.get(i));
                }

                for (JSONObject obj : list) {

                    if (obj.get("district").toString().equals(districtName)) {
                        String zoneColor = obj.get("zone").toString();

                        switch (zoneColor) {
                            case "Red":
                                currentLocationZone.setBackgroundColor(Color.RED);
                                currentLocationZone.setText("District Zone - Red");
                                break;
                            case "Orange":
                                currentLocationZone.setBackgroundColor(Color.rgb(255, 165, 0));
                                currentLocationZone.setText("District Zone - Orange");
                                break;
                            case "Green":
                                currentLocationZone.setBackgroundColor(Color.GREEN);
                                currentLocationZone.setText("District Zone - Green");
                                break;
                        }


                        if (obj.get("state").toString() != stateName) {
                            stateName = obj.get("state").toString();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadTests extends AsyncTask<String, Void, String> {
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
                JSONArray stateTests = (JSONArray) jsonObject.get("states_tested_data");
                ArrayList<JSONObject> list = new ArrayList<>();

                int population = 0;
                int tests = 0;
                int positive = 0;

                for (int i = 0; i < stateTests.length(); i++) {
                    list.add((JSONObject) stateTests.get(i));
                }
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(c);

                Date myDate = df.parse(formattedDate);
                // Use the Calendar class to subtract one day
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(myDate);
                calendar.add(Calendar.DAY_OF_YEAR, -1);

                // Use the date formatter to produce a formatted date string
                stateTestPMill = (TextView) findViewById(R.id.stateTestPMill);
                stateConfPMill = (TextView) findViewById(R.id.stateConfPMill);
                float cpm = 0;
                float tpm = 0;

                for (JSONObject obj : list) {

                    population = populationmap.get(stateName);
                    if (obj.get("state").toString().equals(stateName) && obj.get("updatedon").toString().equals(formattedDate)) {
                        if (obj.get("totaltested").toString() != null || obj.get("totaltested").toString() != "")
                            tests = Integer.parseInt(obj.get("totaltested").toString());
                        else
                            tests = 0;
                        if (obj.get("positive").toString() != null || obj.get("positive").toString() != "")
                            positive = Integer.parseInt(obj.get("positive").toString());
                        else
                            positive = 0;
                    }
                }

                if (population != 0 && tests != 0) {
                    tpm = (float) tests / population * 1000000;
                } else {
                    tpm = 0;
                }

                if (population != 0 && positive != 0) {
                    cpm = (float) positive / population * 1000000;
                } else {
                    cpm = 0;
                }

                if (tpm != 0) {
                    stateTestPMill.setText(Float.toString(tpm));
                } else {
                    stateTestPMill.setText("Not Available");
                }

                if (cpm != 0) {
                    stateConfPMill.setText(Float.toString(cpm));
                } else {
                    stateConfPMill.setText("Not Available");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processDistricts() {

        try {
            for (int i = 0; i < districtMasterArray.length(); i++) {
                JSONObject explrObject = districtMasterArray.getJSONObject(i);
                JSONArray districtArray = explrObject.getJSONArray("districtData");
                if (explrObject.getString("state").equals(stateName)) {
                    stateNameTextView.setText(explrObject.get("state").toString());
                    ArrayList<JSONObject> list = new ArrayList<>();

                    for (int j = 0; j < districtArray.length(); j++) {
                        list.add((JSONObject) districtArray.get(j));

                    }
                    Collections.sort(list, new MyJSONComparator());
                    createRecyclervViewDistricts(list);

                    if (districtName != null) {

                        for (JSONObject obj : list) {
                            if (obj.getString("district").equals(districtName)) {
                                currentLocation.setText(obj.get("district").toString());
                                locationConfirmed.setText(obj.get("confirmed").toString());
                                locationActive.setText(obj.get("active").toString());
                                locationRecovered.setText(obj.get("recovered").toString());
                                locationDeceased.setText(obj.get("deceased").toString());
                                lConfirmedIncr.setText(obj.getJSONObject("delta").getString("confirmed"));
                                lActiveIncr.setText(obj.getJSONObject("delta").getString("confirmed"));
                                lRecoveredIncr.setText(obj.getJSONObject("delta").getString("recovered"));
                                lRecoveredIncr.setTextColor(Color.rgb(255, 0, 0));
                                lDeceasedIncr.setText(obj.getJSONObject("delta").getString("deceased"));
                            }

                        }


                    } else {
                        JSONObject maxDistrict = list.get(0);
                        currentLocation.setText(maxDistrict.get("district").toString());
                        locationConfirmed.setText(maxDistrict.get("confirmed").toString());
                        locationActive.setText(maxDistrict.get("active").toString());
                        locationRecovered.setText(maxDistrict.get("recovered").toString());
                        locationDeceased.setText(maxDistrict.get("deceased").toString());
                        districtName = maxDistrict.get("district").toString();
                        lConfirmedIncr.setText(maxDistrict.getJSONObject("delta").getString("confirmed"));
                        lActiveIncr.setText(maxDistrict.getJSONObject("delta").getString("confirmed"));
                        lRecoveredIncr.setText(maxDistrict.getJSONObject("delta").getString("recovered"));
                        lRecoveredIncr.setTextColor(Color.rgb(255, 0, 0));
                        lDeceasedIncr.setText(maxDistrict.getJSONObject("delta").getString("deceased"));
                        DownloadZones districtZone = new DownloadZones();
                        districtZone.execute("https://api.covid19india.org/zones.json");
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    private void processStates() {

        try {
            if (stateName != null) {
                for (JSONObject obj : listStates) {
                    if (obj.get("state").toString().equals(stateName)) {
                        stateConfirmed.setText(obj.get("confirmed").toString());
                        stateActive.setText(obj.get("active").toString());
                        stateRecovered.setText(obj.get("recovered").toString());
                        stateDeceased.setText(obj.get("deaths").toString());
                        sConfirmedIncr.setText(obj.get("deltaconfirmed").toString());
                        sActiveIncr.setText(obj.get("deltaconfirmed").toString());
                        sRecoveredIncr.setText(obj.get("deltarecovered").toString());
                        sRecoveredIncr.setTextColor(Color.rgb(255, 0, 0));
                        sDeceasedIncr.setText(obj.get("deltadeaths").toString());
                        DownloadTests tests = new DownloadTests();
                        tests.execute("https://api.covid19india.org/state_test_data.json");
                        CreatePieChart(Float.parseFloat(obj.get("confirmed").toString()), Float.parseFloat(obj.get("active").toString()),
                                Float.parseFloat(obj.get("recovered").toString()), Float.parseFloat(obj.get("deaths").toString()));

                    }
                }
            } else {
                JSONObject maxState = listStates.get(1);
                stateConfirmed.setText(maxState.get("confirmed").toString());
                stateActive.setText(maxState.get("active").toString());
                stateRecovered.setText(maxState.get("recovered").toString());
                stateDeceased.setText(maxState.get("deaths").toString());
                sConfirmedIncr.setText(maxState.get("deltaconfirmed").toString());
                sActiveIncr.setText(maxState.get("deltaconfirmed").toString());
                sRecoveredIncr.setText(maxState.get("deltarecovered").toString());
                sRecoveredIncr.setTextColor(Color.rgb(255, 0, 0));
                sDeceasedIncr.setText(maxState.get("deltadeaths").toString());
                stateName = maxState.get("state").toString();
                DownloadTests tests = new DownloadTests();
                tests.execute("https://api.covid19india.org/state_test_data.json");
                CreatePieChart(Float.parseFloat(maxState.get("confirmed").toString()), Float.parseFloat(maxState.get("active").toString()),
                        Float.parseFloat(maxState.get("recovered").toString()), Float.parseFloat(maxState.get("deaths").toString()));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void CreatePieChart(float confirmState, float activeState, float recoveredState, float deadState) {
        PieChart piechart = findViewById(R.id.idPieChart);
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

    private void createRecyclervViewDistricts(ArrayList<JSONObject> list) throws JSONException {

        for (JSONObject obj : list) {
            myDataset.add(obj.getString("district"));

        }
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(myDataset, currentLocation, this);
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                        finishAffinity();
                    }
                }).create().show();
    }

    public void createPopulationMap() {

        populationmap = new HashMap<>();

        populationmap.put("Uttar Pradesh", 237882725);
        populationmap.put("Bihar", 124799926);
        populationmap.put("Maharashtra", 123144223);
        populationmap.put("West Bengal", 99609303);
        populationmap.put("State Unassigned", 1);

        populationmap.put("Madhya Pradesh", 85358965);
        populationmap.put("Rajasthan", 81032689);
        populationmap.put("Tamil Nadu", 77841267);
        populationmap.put("Karnataka", 67562686);

        populationmap.put("Gujarat", 63872399);
        populationmap.put("Andhra Pradesh", 53903393);
        populationmap.put("Odisha", 46356334);
        populationmap.put("Telangana", 39362732);

        populationmap.put("Jharkhand", 38593948);
        populationmap.put("Kerala", 35699443);
        populationmap.put("Assam", 35607039);
        populationmap.put("Punjab", 30141373);

        populationmap.put("Chhattisgarh", 29436231);
        populationmap.put("Haryana", 28204692);
        populationmap.put("Delhi", 18710922);
        populationmap.put("Jammu and Kashmir", 13606320);

        populationmap.put("Uttarakhand", 11250858);
        populationmap.put("Himachal Pradesh", 7451955);
        populationmap.put("Tripura", 4169794);
        populationmap.put("Meghalaya", 3366710);

        populationmap.put("Manipur", 3091545);
        populationmap.put("Nagaland", 2249695);
        populationmap.put("Goa", 1586250);
        populationmap.put("Arunachal Pradesh", 1570458);

        populationmap.put("Puducherry", 1413542);
        populationmap.put("Sikkim", 690251);
        populationmap.put("Dadra and Nagar Haveli and Daman and Diu", 615724);
        populationmap.put("Andaman and Nicobar Islands", 417036);
        populationmap.put("Chandigarh", 1158473);
        populationmap.put("Mizoram", 1239244);

        populationmap.put("Ladakh", 289023);
        populationmap.put("Lakshadweep", 73183);

    }

}
