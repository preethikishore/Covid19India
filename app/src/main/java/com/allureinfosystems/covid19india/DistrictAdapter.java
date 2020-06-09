package com.allureinfosystems.covid19india;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.MyViewHolder> {
    private ArrayList<HashMap<String, String>> districtData;

    public DistrictAdapter(ArrayList<HashMap<String, String>> districtData) {
        this.districtData = districtData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.districtdata, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        HashMap<String, String> StateDetails = districtData.get(position);
        holder.districtName.setText(StateDetails.get("districtName"));
        holder.districtConfirmed.setText(StateDetails.get("dConfirm"));
        holder.districtActive.setText(StateDetails.get("dActive"));
        holder.districtRecovered.setText(StateDetails.get("dRecovered"));
        holder.districtDeceased.setText(StateDetails.get("dDeceased"));
        holder.districtConfirmedIncr.setText(StateDetails.get("dConfirmedIncr"));
        holder.districtDeceasedIncr.setText(StateDetails.get("dDeceasedIncr"));
        holder.districtActiveIncr.setText(StateDetails.get("dActiveIncr"));
        holder.districtRecoveredIncr.setText(StateDetails.get("dRecoveredIncr"));
    }


    @Override

    public int getItemCount() {
        return (districtData == null) ? 0 : districtData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView districtName;
        public TextView districtConfirmed;
        public TextView districtActive;
        public TextView districtRecovered;
        public TextView districtDeceased;
        public TextView districtConfirmedIncr;
        public TextView districtDeceasedIncr;
        public TextView districtActiveIncr;
        public TextView districtRecoveredIncr;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            districtName = (TextView) itemView.findViewById(R.id.dName);
            districtConfirmed = (TextView) itemView.findViewById(R.id.dConfirmed);
            districtActive = (TextView) itemView.findViewById(R.id.dActive);
            districtRecovered = (TextView) itemView.findViewById(R.id.dRecovered);
            districtDeceased = (TextView) itemView.findViewById(R.id.dDeceased);
            districtConfirmedIncr = (TextView) itemView.findViewById(R.id.dConfirmedIncr);
            districtDeceasedIncr = (TextView) itemView.findViewById(R.id.dDeceasedIncr);
            districtActiveIncr = (TextView) itemView.findViewById(R.id.dActiveIncr);
            districtRecoveredIncr = (TextView) itemView.findViewById(R.id.dRecoveredIncr);


        }
    }
}