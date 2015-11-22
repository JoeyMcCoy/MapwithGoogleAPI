package com.joeymccoy.mapexample;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleClient;

    public static final String TAG = MapsActivity.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        //Action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);



        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        //creating locationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(60*1000)       //10 seconds
                .setFastestInterval(1*1000);//1 second

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        setUpMapIfNeeded();

        checkNetwork();

        mGoogleClient.connect();

    }

    private void checkNetwork() {
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled){
            //notify user
            AlertDialog.Builder gpsAlert = new AlertDialog.Builder(this);
            gpsAlert.setMessage("Please enable location settings on your device. Don't worry, we'll take you there :D");
            gpsAlert.setPositiveButton("Take me there", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settings);
                }
            });
            gpsAlert.setCancelable(false);

            gpsAlert.show();
        }

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleClient, this);
            mGoogleClient.disconnect();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }



    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);

        if(location == null){

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocationRequest, this);
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();


        }else{
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location){
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Im here!");

        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Zooms in on user's current location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(8)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //Tells the user their current location in the form of a toast
        Toast.makeText(getApplicationContext(),
                String.valueOf("Current location: " + currentLatitude + "," + currentLongitude),
                Toast.LENGTH_LONG).show();

        //Added to find the exact latitude and longitude
        Log.d("User's current Lat = ", Double.toString(currentLatitude));
        Log.d("User's current Long = ",Double.toString(currentLongitude));

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);


    }
}