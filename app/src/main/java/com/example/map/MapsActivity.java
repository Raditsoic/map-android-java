package com.example.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.map.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private EditText searchCityEditText;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Button searchCityButton;
    private Button searchCoordinatesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeUI(binding);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeUI(ActivityMapsBinding binding) {
        searchCityEditText = binding.editTextCity;
        latitudeEditText = binding.editTextLatitude;
        longitudeEditText = binding.editTextLongitude;
        searchCityButton = binding.buttonSearchCity;
        searchCoordinatesButton = binding.buttonSearchCoordinates;

        searchCityButton.setOnClickListener(v -> searchByCity());
        searchCoordinatesButton.setOnClickListener(v -> searchByCoordinates());
    }

    private void searchByCity() {
        String cityName = searchCityEditText.getText().toString().trim();
        if (!cityName.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocationName(cityName, 1);
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                            cityName);
                } else {
                    Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error searching for city", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchByCoordinates() {
        String latStr = latitudeEditText.getText().toString().trim();
        String lonStr = longitudeEditText.getText().toString().trim();

        if (!latStr.isEmpty() && !lonStr.isEmpty()) {
            try {
                double latitude = Double.parseDouble(latStr);
                double longitude = Double.parseDouble(lonStr);

                if (latitude >= -90 && latitude <= 90 &&
                        longitude >= -180 && longitude <= 180) {
                    LatLng location = new LatLng(latitude, longitude);
                    moveCamera(location, "Selected Location");
                } else {
                    Toast.makeText(this, "Invalid coordinates range", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter both latitude and longitude",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void moveCamera(LatLng location, String title) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng jakarta = new LatLng(-6.2088, 106.8456);
        mMap.addMarker(new MarkerOptions()
                .position(jakarta)
                .title("Marker in Jakarta"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 12f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        setUpMapListeners();

        enableMyLocation();
    }

    private void setUpMapListeners() {
        mMap.setOnMapClickListener(latLng -> {
            latitudeEditText.setText(String.format(Locale.US, "%.6f", latLng.latitude));
            longitudeEditText.setText(String.format(Locale.US, "%.6f", latLng.longitude));

            moveCamera(latLng, "Selected Location");
        });

        mMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(this, "Clicked: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            enableMyLocation();
        }
    }
}