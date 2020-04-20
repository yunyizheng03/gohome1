package com.example.hellomap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;

import java.io.IOException;
import java.util.List;
//Name: yunyi zheng
//Student number:s1923021
//description: Based on Google map API, using location manager to recieve the data of the location from the user input,
// and transfer the url to longitude and lotitude. User can input the location(URL) into the program, using onQueryTextSubmit method
// to transfer get the longitude and latitude.This program also contain several onlick method, which allow user to set the input location as hospital or home, and also allow them to
//press go home or go hospital, then the program will automatically arrange the route for the user.
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback , TaskLoadedCallback{
    //declaring string and variables and names
    private static final String FILE_NAME = "mapInformation.txt";
    private static final String TAG = "MapsActivity";
    public SearchView searchView;
    public GoogleMap mMap;
    public LocationManager locationManager;
    public LocationListener locationListener;
    public String bestProvider;
    public double personalLat=0f;
    public double personalLnt=0f;
    public double CurrLat=0f;
    public double CurrLnt=0f;
    public double homeLat=0f;
    public double homeLnt=0f;
    public double careLat=0f;
    public double careLnt=0f;
    public Polyline currentPolyline;
    public MarkerOptions locationHome, locationCareCenter,myLocation;
    private GeoApiContext mGeoApiContext = null;
    public DatabaseHelper myDb;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connecting the UI interface to this program
        setContentView(R.layout.activity_maps);
        Button homeSet = findViewById(R.id.homeSet);
        Button careSet = findViewById(R.id.careSet);
        Button goHome = findViewById(R.id.goHome);
        Button goCareCen = findViewById(R.id.goCareCen);
        searchView = findViewById(R.id.search_location);

        //Sets the API Key to use for authorizing requests
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }

        //database
        //Database must be initialized before it can be used. This will ensure
        //that the database exists and is the current version.
        myDb = new DatabaseHelper(this);
        //initial the map UI design
        Cursor res =myDb.findExistedLocation("1");
        if(res.getCount()==0) {
            //save initial Home location as 0,0
            myDb.insertLocation("Home", "0", "0");
            //save initial hospital location as 0,0
            myDb.insertLocation("CareCenter", "0", "0");
        }
        // initialization of location service
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new mylocationlistener();
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            //A toast provides simple feedback about an operation in a small popup
            Toast.makeText(this,"Open GPS", Toast.LENGTH_LONG).show();
        }
        //Returns the name of the provider that best meets the given criteria
        bestProvider =locationManager.getBestProvider(getcriteria(),true);
        //Register for location updates from the given provider with the given arguments, which also save the power
        locationManager.requestLocationUpdates(bestProvider,3000,1,locationListener);
        //locationManager.requestSingleUpdate(bestProvider,locationListener,null);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //the block is designed for the searching view block of the program
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //transfer the url to string
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                //if there is input in the address of the search view block
                if(location!=null || !location.equals(""))
                {
                    //Geocoding is the process of transforming a street address or other description of a location into a (latitude, longitude) coordinate
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try{
                        //Returns an array of Addresses that are known to describe the named location
                        addressList = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {//if the network is unavailable or any other I/O problem occurs
                        e.printStackTrace();
                    }
                    //read the address and transfer to latitude and longitude
                    Address address = addressList.get(0);
                    CurrLat=address.getLatitude();
                    CurrLnt=address.getLongitude();
                    LatLng latLngInput = new LatLng(address.getLatitude(),address.getLongitude());
                    //then generate a maker according to the location
                    mMap.addMarker(new MarkerOptions().position(latLngInput).title(location));
                    //allowing the camera to fit with the new location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngInput,10));
                }
                return false;
            }

            //if there is a change in the text, the program will come back to above block
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //This class automatically initializes the maps system and the view.
        mapFragment.getMapAsync(this);

        //when click on the set as home button, the screen will show the latitude and longtitude of home on the screen, by using the toast funciton
        //this will also show whether the data is successfully saved in database
        homeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeLat=CurrLat;
                homeLnt=CurrLnt;
                Toast.makeText(getApplicationContext(),"Home:\nLat"+homeLat+"\n"+"Lnt"+homeLnt,Toast.LENGTH_LONG).show();
                locationHome = new MarkerOptions().position(new LatLng(homeLat, homeLnt)).title("My Home");
                updateMap(homeLat,homeLnt,"Home");
                boolean isUpdated =myDb.updateData("1", "Home",Double.toString(homeLat),Double.toString(homeLnt));
                if(isUpdated = true)
                    Toast.makeText(MapsActivity.this,"Successfully saved in database",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MapsActivity.this,"Failed to save in database",Toast.LENGTH_LONG).show();
            }
        });
        //when click on the set as hospital location, the screen will show the latitude and longtitude of hospital  on the screen,
        //this will also show whether the data is successfully saved in database
        careSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                careLat=CurrLat;
                careLnt=CurrLnt;
                Toast.makeText(getApplicationContext(),"CareCenter:\nLat"+careLat+"\n"+"Lnt"+careLnt,Toast.LENGTH_LONG).show();
                locationCareCenter= new MarkerOptions().position(new LatLng(careLat, careLnt)).title("My Care Center");
                updateMap(careLat,careLnt,"CareCenter");
                boolean isUpdated =myDb.updateData("2", "CareCenter",Double.toString(careLat),Double.toString(careLnt));
                if(isUpdated = true)
                    Toast.makeText(MapsActivity.this,"Successfully saved in database",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MapsActivity.this,"Failed to save in database",Toast.LENGTH_LONG).show();

            }
        });
        //when clicking on Go home button, the following block is aimed to find the home address saved in database, and fetch url on the map
        // if you put empty address, the screen will show Please set up your home address
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myLocation!=null)
                {
                    Cursor res =myDb.findExistedLocation("1");
                    res.moveToNext();
                    LatLng latLngHome = new LatLng(res.getDouble(2), res.getDouble(3));
                    new FetchURL(MapsActivity.this).execute(getUrl(myLocation.getPosition(), latLngHome, "driving"), "driving");
                }
                else
                    Toast.makeText(getApplicationContext(),"Please set up your home",Toast.LENGTH_LONG).show();

            }
        });
        //when clicking on Go Hospital button, the following block is aimed to find the hospital address saved in database, and fetch url on the map
        // if you put empty address, the screen will show Please set up your hospital
        goCareCen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myLocation!=null)
                {
                    Cursor res =myDb.findExistedLocation("2");
                    res.moveToNext();
                    LatLng latLngCare = new LatLng(res.getDouble(2), res.getDouble(3));
                    new FetchURL(MapsActivity.this).execute(getUrl(myLocation.getPosition(),latLngCare, "driving"), "driving");
                }
                else
                    Toast.makeText(getApplicationContext(),"Please set up your Care Center",Toast.LENGTH_LONG).show();

            }
        });


    }
    //unregistered the locationmanager
    @Override
    protected void onPause(){
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
    @SuppressLint("MissingPermission")
    @Override
    //initialization method
    protected void onResume(){
        super.onResume();
        setUpMapIfNeeded();
        bestProvider =locationManager.getBestProvider(getcriteria(),true);
    }
    //A class indicating the application criteria for selecting a location provider.
    // Providers may be ordered according to accuracy,
    // power usage, ability to report altitude, speed, bearing, and monetary cost
        private Criteria getcriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return  criteria;
    }
    //A polyline is a list of points, where line segments are drawn between consecutive points.

    @Override
    public void onTaskDone(Object... values) {
        //The code below helps from the app from not crashing
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
    //This class provides access to the system location services.
    // These services allow applications to obtain periodic updates of the device's geographical location,
    // or to be notified when the device enters the proximity of a given geographical location.
    class mylocationlistener implements  LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            if(location!=null)
            {
                personalLat = location.getLatitude();
                personalLnt = location.getLongitude();
                myLocation = new MarkerOptions().position(new LatLng(personalLat, personalLnt)).title("My Location");
                updateMap(personalLat,personalLnt,"My Location");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    // this function will implement when there is a new location is added into the program
    private void updateMap(double tlat,double tlong,String location) {
            LatLng latLng = new LatLng(tlat, tlong);
            mMap.addMarker(new MarkerOptions().position(latLng).title(location));

    }

    //link the program to the map ui design
    public void  setUpMapIfNeeded()
    {
        if(mMap == null)
        {
            SupportMapFragment mapFragment =(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }


    // this function is used to get the url from the input by the user
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }
    //Creates a builder for an alert dialog that uses the default alert dialog theme
    public void showMessage(String title,String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Can cancel it after using it.
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
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
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // add a marker in Sydney and move the camera
        LatLng Edinburgh = new LatLng(55.953251, -3.188267);
        mMap.addMarker(new MarkerOptions().position(Edinburgh).title("Marker in Edinburgh"));
        // update the camera zoom at edinburgh
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Edinburgh));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Edinburgh,15));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //enabling the settings in the map
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }
    // user can adjust the map as the type they want
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.seeAllLocation:
                //get the location from database
                Cursor res =myDb.getAllLocation();
                    if(res.getCount()==0)
                    {
                        Toast.makeText(MapsActivity.this,"Nothing found in database",Toast.LENGTH_LONG).show();
                        return true;
                    }
                    else {// showing all the location that user had chosen
                        StringBuffer locationBuffer =new StringBuffer();
                        while (res.moveToNext())
                        {
                            locationBuffer.append("Id:"+res.getString(0)+"\n");
                            locationBuffer.append("Location Name:"+res.getString(1)+"\n");
                            locationBuffer.append("Latitude:"+res.getString(2)+"\n");
                            locationBuffer.append("Longitude:"+res.getString(3)+"\n\n");
                        }
                        showMessage("ALL Location",locationBuffer.toString());
                        return true;
                    }
                    //delete all location
            case R.id.deleteAllLocation:
                myDb.deleteALLData();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
