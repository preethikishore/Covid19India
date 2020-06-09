package com.allureinfosystems.covid19india;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;


public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.MyViewHolder> {

    private ArrayList<HashMap<String, String>> countryData;


    public CountryAdapter(ArrayList<HashMap<String, String>> countryData) {

        this.countryData = countryData;

    }

    @NonNull
    @Override
    public CountryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.countrydata, parent, false);
        CountryAdapter.MyViewHolder vh = new CountryAdapter.MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull final CountryAdapter.MyViewHolder holder, int position) {

        HashMap<String, String> CountryDetails = countryData.get(position);

        holder.countryName.setText(CountryDetails.get("CName"));
        holder.cConfirmed.setText(CountryDetails.get("Confirm"));
        holder.cActive.setText(CountryDetails.get("Active"));
        holder.cRecovered.setText(CountryDetails.get("Recovered"));
        holder.cDeceased.setText(CountryDetails.get("Deceased"));
        holder.cConfirmedIncr.setText(CountryDetails.get("ConfirmedIncr"));
        holder.cDeceasedIncr.setText(CountryDetails.get("DeceasedIncr"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> hashmap = countryData.get(holder.getAdapterPosition());
                String countryIntent = hashmap.get("CName");
                Intent intent = new Intent(view.getContext(), GlobalDataActivity.class);
                intent.putExtra("CName", countryIntent);
                view.getContext().startActivity(intent);
            }
        });


    }


    @Override
    public int getItemCount() {
        return (countryData == null) ? 0 : countryData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        public TextView countryName;
        public TextView cConfirmed;
        public TextView cActive;
        public TextView cRecovered;
        public TextView cDeceased;
        public TextView cConfirmedIncr;
        public TextView cDeceasedIncr;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName = (TextView) itemView.findViewById(R.id.countryName);
            cConfirmed = (TextView) itemView.findViewById(R.id.cConfirmed);
            cActive = (TextView) itemView.findViewById(R.id.cActive);
            cRecovered = (TextView) itemView.findViewById(R.id.cRecovered);
            cDeceased = (TextView) itemView.findViewById(R.id.cDeseased);
            cConfirmedIncr = (TextView) itemView.findViewById(R.id.cConfirmedIncr);
            cDeceasedIncr = (TextView) itemView.findViewById(R.id.cDeceasedIncr);
        }


    }
}

