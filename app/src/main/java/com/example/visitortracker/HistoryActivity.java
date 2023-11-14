package com.example.visitortracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference("VisitorTracker");
    private ArrayList<History> historyItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.hitoryItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyItem = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, (ArrayList<History>) historyItem);
        recyclerView.setAdapter(historyAdapter);

        initHistory();
    }

    private void initHistory() {
        database.child("history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyItem.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    History history = dataSnapshot.getValue(History.class);
                    historyItem.add(history);
                }
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}