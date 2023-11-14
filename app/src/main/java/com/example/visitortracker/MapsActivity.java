package com.example.visitortracker;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.visitortracker.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myRootRef = database.getReference("VisitorTracker");
    private final DatabaseReference myDeviceRef = database.getReference("VisitorTracker/device");
    private final DatabaseReference myVisitorRef = database.getReference("VisitorTracker/visitor");
    private Marker userMarker;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        userMarker.setVisible(false);
        float zoom_level = 18.0f;
        LatLng southwest = new LatLng(14.108440112831428, 122.95683710457803);
        LatLng northeast = new LatLng(122.95901159884491, 14.10754846468422);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLngBounds restrictedBounds = new LatLngBounds(southwest, northeast);
        mMap.setLatLngBoundsForCameraTarget(restrictedBounds);
        mMap.setMinZoomPreference(zoom_level);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        myDeviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double latitude;
                Double longitude;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    longitude = dataSnapshot.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null){
                        LatLng location = new LatLng(latitude, longitude);

                        myVisitorRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot visitorKey: dataSnapshot.getChildren()){
                                    String username = visitorKey.child("username").getValue(String.class);
                                    if (userMarker!= null) {
                                        userMarker.setVisible(true);
                                    }
                                    userMarker.setPosition(location);
                                    if (username != null) {
                                        userMarker.setTitle(username);
                                    } else {
                                        // Handle case where username is null
                                        userMarker.setVisible(false);
                                    }
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
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

        float bearing = 89; // Rotate the map by 90 degrees (modify as needed)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mMap.getCameraPosition().target) // Set the map's current position as the target
                        .zoom(mMap.getCameraPosition().zoom) // Set the zoom level
                        .bearing(bearing) // Set the bearing/rotation angle
                        .tilt(mMap.getCameraPosition().tilt)// Set the tilt if needed
                        .build()
        ));
    }
}