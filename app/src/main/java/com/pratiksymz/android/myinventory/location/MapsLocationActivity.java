package com.pratiksymz.android.myinventory.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pratiksymz.android.myinventory.EditorActivity;
import com.pratiksymz.android.myinventory.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static String LOG_TAG = MapsLocationActivity.class.getName();

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private Location mLastKnownLocation;
    private LatLng mCurrentLocation;

    private FloatingActionButton mFloatingActionButton;
    private String mCurrentAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.map_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent parentIntent = new Intent(MapsLocationActivity.this, EditorActivity.class);
                parentIntent.putExtra("currentAddress", mCurrentAddress);
                setResult(RESULT_OK, parentIntent);
                finish();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Delete existing markers
                mMap.clear();

                mCurrentLocation = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                mMap.addMarker(new MarkerOptions()
                        .position(mCurrentLocation)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 18));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                mLocationManager.requestLocationUpdates(
                        provider,
                        0,
                        0,
                        mLocationListener
                );
            }

        };

        if (Build.VERSION.SDK_INT < 23) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    mLocationListener
            );

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Ask Permission
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1
                );

            } else {
                // We already have the permission
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,                                  // Minimum Time
                        0,                               // Minimum Distance
                        mLocationListener
                );

                // Add the current location of the user on the map when it is prepared
                mLastKnownLocation = getLastKnownLocation();

                // Delete existing markers
                mMap.clear();

                mCurrentLocation = new LatLng(
                        mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()
                );

                mMap.addMarker(new MarkerOptions()
                        .position(mCurrentLocation)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 18));

                // Use GeoCoder to extract the user's current location address
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude(),
                            1
                    );

                    // Check if addressList is NULL FIRSTLY and then CHECK FOR SIZE NEXT
                    if (addressList != null && addressList.size() > 0) {
                        if (addressList.get(0).getAddressLine(0) != null) {
                            mCurrentAddress = addressList.get(0).getAddressLine(0) + "\n";
                        }
                        if (addressList.get(0).getAddressLine(1) != null) {
                            mCurrentAddress += addressList.get(0).getAddressLine(1) + "\n";
                        }
                        if (addressList.get(0).getAddressLine(2) != null) {
                            mCurrentAddress += addressList.get(0).getAddressLine(2) + "\n";
                        }
                        if (addressList.get(0).getAddressLine(3) != null) {
                            mCurrentAddress += addressList.get(0).getAddressLine(3);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Helper method to retrieve the best last known location of the user
     */
    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = location;
            }
        }
        return bestLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    // All permissions granted
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            mLocationListener
                    );
                }
            }
        }
    }
}
