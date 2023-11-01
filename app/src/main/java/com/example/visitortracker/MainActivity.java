package com.example.visitortracker;
import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.visitortracker.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {
    private static final int REQUEST_LOCATION = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocClient;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myRootRef = database.getReference("VisitorTracker");
    private final DatabaseReference myDeviceRef = database.getReference("VisitorTracker/device");
    private final DatabaseReference myVisitorRef = database.getReference("VisitorTracker/visitor");

    private View view1;

    private Toolbar toolbar;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocClient = LocationServices.getFusedLocationProviderClient(this);


    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        float zoom_level = 18.3f;
        LatLng defaultLoc = new LatLng(14.109356044984484, 122.95756385927663);
        LatLng southwest = new LatLng(14.108440112831428, 122.95683710457803);
        LatLng northeast = new LatLng(122.95901159884491, 14.10754846468422);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLngBounds restrictedBounds = new LatLngBounds(southwest, northeast);
        mMap.setLatLngBoundsForCameraTarget(restrictedBounds);
        mMap.setMinZoomPreference(zoom_level);

        float bearing = 87;
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(defaultLoc)
                        .zoom(zoom_level)
                        .bearing(bearing)
                        .tilt(mMap.getCameraPosition().tilt)
                        .build()
        ));

        myDeviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getUserCurrentLocation();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Double latitude = dataSnapshot.child("location").child("lat").getValue(Double.class);
                    Double longitude = dataSnapshot.child("location").child("long").getValue(Double.class);
                    if (latitude != null && longitude != null) {
                        LatLng location = new LatLng(latitude, longitude);
                        myVisitorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot visitorSnapshot) {
                                for (DataSnapshot visitorDataSnapshot: visitorSnapshot.getChildren()){
                                    if (Objects.equals(visitorDataSnapshot.child("deviceId").getValue(String.class), dataSnapshot.getKey())){
                                        String username = visitorDataSnapshot.child("username").getValue(String.class);
                                        MarkerOptions markeroptions = new MarkerOptions()
                                                .position(location)
                                                .title(username);
                                        mMap.addMarker(markeroptions);
                                    } else {
                                        Log.e("FirebaseError", "Error sa deviceId ng DEVICE at VISITOR ");
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        myDeviceRef.child("device-001").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUserCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocPermissions();
        } else {
            fusedLocClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    android.location.Location location = task.getResult();
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Your are here!")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
                    } else {
                        Log.e(TAG, "No location found");
                    }
                }
            });
        }
    }

    private void requestLocPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserCurrentLocation();
            } else {
                Log.e(TAG, "Location permission has been denied");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch(menuItem.getItemId()){
            case R.id.menu_visitor:
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
                view1= LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.activity_bottom_frame,(LinearLayout) findViewById(R.id.layout_container));

                myVisitorRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> visitorDetail= new ArrayList<String>();
                        for (DataSnapshot itemSnap: snapshot.getChildren()){
                            String username = itemSnap.child("username").getValue(String.class);
                            visitorDetail.add(username);
//                    visitorDetail.add(itemSnap.child("username").getValue(String.class));
                        }
                        ArrayAdapter<String> visitorAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, visitorDetail);
                        visitorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        Spinner spinnerInDialog = view1.findViewById(R.id.visitor_spinner);
                        spinnerInDialog.setAdapter(visitorAdapter);
                        spinnerInDialog.setOnItemSelectedListener(MainActivity.this);

                        visitorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Database Error: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

                bottomSheetDialog.setContentView(view1);
                bottomSheetDialog.show();
                return true;
            case R.id.menu_history:
                return true;
            case R.id.menu_add_visitor:
                Intent intent = new Intent(MainActivity.this, AddVisitorActivity.class);
                startActivity(intent);
                return true;
            default: return onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedUsername = (String) parent.getSelectedItem();
//        Button btnBuzz = view1.findViewById(R.id.btnBuzzDevice);
        Button btnRemove = view1.findViewById(R.id.btnRemoveVisitor);
        TextView txtDeviceId = view1.findViewById(R.id.textDeviceId);

        myVisitorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (Objects.equals(selectedUsername, dataSnapshot.child("username").getValue(String.class))){
                        txtDeviceId.setText(dataSnapshot.child("deviceId").getValue(String.class));
                        myDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot deviceDataSnapshot: snapshot.getChildren()){
                                    if (Objects.equals(dataSnapshot.child("deviceId").getValue(String.class), deviceDataSnapshot.getKey())){
                                        Double lat = deviceDataSnapshot.child("location/lat").getValue(Double.class);
                                        Double lng = deviceDataSnapshot.child("location/long").getValue(Double.class);
                                        if (lat != null || lng != null){
                                            LatLng updateLocCam = new LatLng(lat, lng);
                                            float zoom = 19.5f;
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(updateLocCam, zoom));
                                        }
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myVisitorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String deviceId = txtDeviceId.getText().toString();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            if (Objects.equals(deviceId, dataSnapshot.child("deviceId").getValue(String.class))){
                                DatabaseReference nodeToRemove = myVisitorRef.child(dataSnapshot.getKey());
                                nodeToRemove.removeValue();
                                myDeviceRef.child(deviceId).child("used").setValue(false);
                                txtDeviceId.setText("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}