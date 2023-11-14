package com.example.visitortracker;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.internal.ConnectionTelemetryConfiguration;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private ArrayList<History> historyItem;
    private Context context;
    public HistoryAdapter(Context context, ArrayList<History> historyItem){
        this.historyItem = historyItem;
        this.context = context;
    }
    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        History currentVisitor = historyItem.get(position);
        holder.bindTo(currentVisitor);
    }

    @Override
    public int getItemCount() {
        return historyItem.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView visitorName;
        private TextView visitorTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            visitorName = itemView.findViewById(R.id.txtVisitorName);
            visitorTime = itemView.findViewById(R.id.txtVisitorTime);
        }

        public void bindTo(History currentVisitor) {
            StringBuilder string = new StringBuilder();
            string.append(currentVisitor.getDate() + "\n");
            string.append(currentVisitor.getTime());
            visitorTime.setText(string.toString());
            visitorName.setText(currentVisitor.getName());
        }
    }
}
