package com.example.mapsdemo2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Marker currentLocationMarker;
    private Location lastLocation;
    public static final int REQUEST_LOCATION_CODE = 99;
    public LatLng latLng;
    MarkerOptions markerOptions;
    double latitude, longitude;
    int PROXIMITY_RADIUS = 10000;
    DatabaseReference myRef;
    // double end_latitude, end_longitude;
    Button accident;
    jam j;
    long maxid=0,jamCount=0;



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
       // ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);


        accident = findViewById(R.id.accident);

        myRef = FirebaseDatabase.getInstance().getReference().child("JAM_LOCATIONS");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                { maxid = dataSnapshot.getChildrenCount(); }


                   /* //String ii = Integer.toString(i);
                    double y = (double) dataSnapshot.child("1").child("lat").getValue();
                    double z = (double) dataSnapshot.child("1").child("lng").getValue();

                    ///////////////////////////////////

                    markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(y, z));
                    markerOptions.title("Traffic Jam");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    markerOptions.snippet("..TRAFFIC JAM..");
                    Toast.makeText(MapsActivity.this, "" + maxid, Toast.LENGTH_SHORT).show();
                    //mMap.clear();
                    mMap.addMarker(markerOptions);

                    ////////////////////////////////////

                    */


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiclient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

       // accident = findViewById(R.id.accident);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiclient();
                mMap.setMyLocationEnabled(true);
                mMap.setMinZoomPreference(5);
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
//            mMap.setOnMarkerDragListener(this);
              mMap.setOnMarkerClickListener(this);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PackageManager.PERMISSION_GRANTED);
            mMap.setMyLocationEnabled(true);
            mMap.setMinZoomPreference(5);
        }


    }

    protected synchronized void buildGoogleApiclient() {
        client = new GoogleApiClient
                .Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        latitude = lastLocation.getLatitude();
        longitude = lastLocation.getLongitude();
        if (currentLocationMarker != null)
            currentLocationMarker.remove();

        latLng = new LatLng(lastLocation.getLatitude(), location.getLongitude());
        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("I'm Here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        //currentLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(0));

        if (client == null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }



    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
                {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

            }
            return false;
        } else
            return true;
    }


    public void onClick(View v) {

        if (v.getId() == R.id.jambtn) {
            j = new jam();
            mMap.clear();
            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latitude, longitude));
            markerOptions.title("Traffic Jam");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markerOptions.snippet("..TRAFFIC JAM..");

            Toast.makeText(this, "reporting jam", Toast.LENGTH_SHORT).show();
            //mMap.clear();
            mMap.addMarker(markerOptions);

            j.setLat(latitude);
            j.setLng(longitude);


            myRef.child(String.valueOf(maxid+1)).setValue(j);
            Toast.makeText(this,"Jam location Updated to DataBase",Toast.LENGTH_SHORT).show();

            //Showing all Jam Markers


        }
        final DatabaseReference Rootref;
        Rootref = FirebaseDatabase.getInstance().getReference();

        Rootref.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child( "JAM_LOCATIONS" ).exists()))
                {

                    jamCount = (int) dataSnapshot.getChildrenCount();
                    for (int k=1; k<=jamCount;k++) {


                        if (dataSnapshot.child( "JAM_LOCATIONS" ).child( String.valueOf( k ) ).exists()) {
                            double l1 = (double) dataSnapshot.child( "JAM_LOCATIONS" ).child( String.valueOf( k ) ).child( "lat" ).getValue();
                            double l2 = (double) dataSnapshot.child( "JAM_LOCATIONS" ).child( String.valueOf( k ) ).child( "lng" ).getValue();

                            showjamMarkers(l1,l2);

                        }
                    }
                }
                else {
                    Toast.makeText(MapsActivity.this, "Kahi jam nahi laga h bro.. bhagao gaadi..", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        accident = findViewById(R.id.accident);
        if (v.getId() == R.id.accident) {

            Toast.makeText(this, "reporting Accident", Toast.LENGTH_LONG).show();
            String num = "6376021150";
            String msg = "ACCIDENT AT.." + latitude + " " + longitude;
            SmsManager mySMSManger = SmsManager.getDefault();
            mySMSManger.sendTextMessage(num, null, msg, null, null);

        }


        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();


        //schools hospitals xyz........

        EditText tf = (EditText) findViewById(R.id.textFieldLocation);
        String s = tf.getText().toString();
        if (s.toUpperCase().equals("SCHOOL") || s.toUpperCase().equals("RESTAURANT") || s.toUpperCase().equals("POLICE STATION") || s.toUpperCase().equals("HOSPITAL") || s.toUpperCase().equals("SCHOOLS") ||
                s.toUpperCase().equals("RESTAURANTS") || s.toUpperCase().equals("HOSPITALS") || s.toUpperCase().equals("ATM") || s.toUpperCase().equals("BANK") || s.toUpperCase().equals("BUS STOP")) {
            if ((v.getId() == R.id.B_search)) {
                mMap.clear();
                String url = getUrl(latitude, longitude, s);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby " + s, Toast.LENGTH_LONG).show();
            }
        }


        //SEARCH BUTTON KI CODING
        if (v.getId() == R.id.B_search) {
            EditText tf_location = (EditText) findViewById(R.id.textFieldLocation);
            String location = tf_location.getText().toString();
            List<Address> addressList = null;
            MarkerOptions mo = new MarkerOptions();

            if (!location.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < addressList.size(); i++) {
                    Address myAddress = addressList.get(i);
                    LatLng latlng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                    mo.position(latlng);
                    //mo.title("");
                    mMap.addMarker(mo);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            }

            if (location.equals("")) {
                mMap.clear();
                mMap.addMarker(markerOptions);
            }
        }


        if ((v.getId() == R.id.directions)) {


            finish();
            System.exit(0);

            /*
            dataTransfer = new Object[3];
            String url = getDirectionsUrl();
            GetDirectionsData getDirectionsData = new GetDirectionsData();
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            dataTransfer[2] = new LatLng(end_latitude, end_longitude);
            getDirectionsData.execute(dataTransfer);
            */
        }

    }

    /*
    private String getDirectionsUrl() {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin=" + latitude + "," + longitude);
        googleDirectionsUrl.append("&destination=" + end_latitude + "," + end_longitude);
        googleDirectionsUrl.append("&key=" + "AIzaSyAbk1ETKQPregAh8Ag2nd2JsMNiEmlh78E");    ///////DIRECTION API KEYYYY

        return googleDirectionsUrl.toString();
    }*/

    private void showjamMarkers(double l1, double l2) {

        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(l1, l2));
        markerOptions.title("Traffic Jam");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        markerOptions.snippet("..TRAFFIC JAM..");

        // Toast.makeText(this, "reporting jam", Toast.LENGTH_SHORT).show();
        //mMap.clear();
        mMap.addMarker(markerOptions);


    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyBwTLt2NuGEiP-Y9_bF6yj2BdnhxywF8qM");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public boolean onMarkerClick(final Marker marker) {


            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Verify JAM");
            builder.setMessage("Is there a Traffic JAM?");


            // Set the alert dialog yes button click listener
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    marker.setTitle("Verified Jam");
                    // Do something when user clicked the Yes button

                }
            });

            // Set the alert dialog no button click listener
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Do something when No button clicked
                    marker.remove();

                }
            });

            AlertDialog dialog = builder.create();
            // Display the alert dialog on interface
            dialog.show();



        return false;
    }

}
