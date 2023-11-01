package com.example.visitortracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddVisitorActivity extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myDeviceRef = database.getReference("VisitorTracker/device");
    private final DatabaseReference myVisitorRef = database.getReference("VisitorTracker/visitor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visitor);

        EditText visitorUsername = findViewById(R.id.eTxtVisitorUsername);
        Button btnSave = findViewById(R.id.btnSaveVisitor);
        Spinner spinner = findViewById(R.id.device_spinner);


        Date nCurrDate = Calendar.getInstance().getTime();
        Date nCurrTime = Calendar.getInstance().getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String currDate = dateFormat.format(nCurrDate);
        String currTime = timeFormat.format(nCurrTime);
        myDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> devices = new ArrayList<String>();
                for (DataSnapshot deviceSnap : snapshot.getChildren()) {
                    Boolean isUsed = deviceSnap.child("used").getValue(Boolean.class);
                    if (isUsed != null && isUsed) {
                        // The device is used (isUsed is true)
                    } else {
                        // The device is not used (isUsed is false or is null)
                        devices.add(deviceSnap.getKey());
                    }
                }

                ArrayAdapter<String> devicesAdapter = new ArrayAdapter<String>(AddVisitorActivity.this, android.R.layout.simple_spinner_dropdown_item, devices);
//                devicesAdapter.setDropDownViewResource(R.layout.activity_add_visitor);

                spinner.setAdapter(devicesAdapter);
                devicesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String spinnerValue = spinner.getSelectedItem().toString();
                String username = visitorUsername.getText().toString();
                String uniqueKey = myVisitorRef.push().getKey();
                myVisitorRef.child(uniqueKey).child("username").setValue(username);
                myVisitorRef.child(uniqueKey).child("date").setValue(currDate);
                myVisitorRef.child(uniqueKey).child("time").setValue(currTime);
                myVisitorRef.child(uniqueKey).child("deviceId").setValue(spinnerValue);
                myDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            if (Objects.equals(spinnerValue, dataSnapshot.getKey())){
                                myDeviceRef.child(dataSnapshot.getKey()).child("used").setValue(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                finish();
            }
        });
    }
}