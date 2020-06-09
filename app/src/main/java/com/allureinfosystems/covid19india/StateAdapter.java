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

public class StateAdapter extends RecyclerView.Adapter<StateAdapter.MyViewHolder> {
    private ArrayList<HashMap<String, String>> stateData;

    public StateAdapter(ArrayList<HashMap<String, String>> stateData) {
        this.stateData = stateData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.statedata, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        HashMap<String, String> StateDetails = stateData.get(position);
        holder.stateName.setText(StateDetails.get("stateName"));
        holder.stateConfirmed.setText(StateDetails.get("stConfirm"));
        holder.stateActive.setText(StateDetails.get("stActive"));
        holder.stateRecovered.setText(StateDetails.get("stRecovered"));
        holder.stateDeceased.setText(StateDetails.get("stDeceased"));
        holder.stateConfirmedIncr.setText(StateDetails.get("stConfirmedIncr"));
        holder.stateDeceasedIncr.setText(StateDetails.get("stDeceasedIncr"));
        holder.stateActiveIncr.setText(StateDetails.get("stActiveIncr"));
        holder.stateRecoveredIncr.setText(StateDetails.get("stRecoveredIncr"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> hashmap = stateData.get(holder.getAdapterPosition());
                String stateName2Intent = hashmap.get("stateName");
                Intent intent = new Intent(view.getContext(), StateActivity.class);
                intent.putExtra("stateName", stateName2Intent);
                view.getContext().startActivity(intent);
            }
        });
    }


    @Override

    public int getItemCount() {
        return (stateData == null) ? 0 : stateData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView stateName;
        public TextView stateConfirmed;
        public TextView stateActive;
        public TextView stateRecovered;
        public TextView stateDeceased;
        public TextView stateConfirmedIncr;
        public TextView stateDeceasedIncr;
        public TextView stateActiveIncr;
        public TextView stateRecoveredIncr;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            stateName = (TextView) itemView.findViewById(R.id.stName);
            stateConfirmed = (TextView) itemView.findViewById(R.id.stConfirmed);
            stateActive = (TextView) itemView.findViewById(R.id.stActive);
            stateRecovered = (TextView) itemView.findViewById(R.id.stRecovered);
            stateDeceased = (TextView) itemView.findViewById(R.id.stDeceased);
            stateConfirmedIncr = (TextView) itemView.findViewById(R.id.stConfirmedIncr);
            stateDeceasedIncr = (TextView) itemView.findViewById(R.id.stDeceasedIncr);
            stateActiveIncr = (TextView) itemView.findViewById(R.id.stActiveIncr);
            stateRecoveredIncr = (TextView) itemView.findViewById(R.id.stRecoveredIncr);


        }
    }
}