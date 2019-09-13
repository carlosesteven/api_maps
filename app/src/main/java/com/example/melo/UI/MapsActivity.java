package com.example.melo.UI;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.melo.R;
import com.example.melo.VOLLEY.MySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation = new LatLng(3.4372201,-76.5224991);
    private CameraPosition mCameraPosition;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                //.addApi(Places.GEO_DATA_API)
                //.addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        Button obtenerUbicacion = findViewById( R.id.button3 );
        obtenerUbicacion.setOnClickListener(view -> {
            getDeviceLocation();
            if ( mLastKnownLocation != null )
            {
                guardarDatosEnBd(
                        1
                );
            }
        });

    }

    private void guardarDatosEnBd(int color)
    {
        String url_preticion = getString(R.string.url_guardar);
        StringRequest strReq = new StringRequest(Request.Method.POST, url_preticion,
                response -> {
                    Log.e("melo_consola", "peticion ejecutada correctamente" );
                    getDatos();
                },
                error ->
                {
                    if ( error != null && error.getMessage() != null ) {
                        Log.e("melo_consola", error.getMessage());
                    }
                    Log.e("melo_consola", "error en volley" );
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("longitud", String.valueOf( mLastKnownLocation.getLongitude() ) );
                params.put("latitud", String.valueOf( mLastKnownLocation.getLatitude() ) );
                params.put("color", String.valueOf( color ) );
                return params;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(strReq);
    }

    private void getDatos()
    {
        String url_preticion = getString(R.string.url_consultar);
        StringRequest strReq = new StringRequest(Request.Method.POST, url_preticion, response ->
        {
            Log.e("melo_consola", response );
            try {
                JSONArray datos = new JSONArray( response );
                for ( int i = 0; i < datos.length(); i++ )
                {
                    JSONObject actual = datos.getJSONObject( i );
                    imprimirEnMapa(
                            actual.getInt("id"),
                            actual.getLong("latitud"),
                            actual.getLong("longitud"),
                            actual.getInt("color")
                    );
                    //Log.e("melo_consola", actual.toString() );
                }
            } catch (JSONException e) {
                if (e.getMessage() != null) {
                    Log.e("melo_consola", e.getMessage());
                }
                e.printStackTrace();
            }
        }, error -> {
            if ( error != null && error.getMessage() != null ) {
                Log.e("melo_consola", error.getMessage());
            }
            Log.e("melo_consola", "error en volley" );
        });
        MySingleton.getInstance(this).addToRequestQueue(strReq);
    }

    public void imprimirEnMapa( int id , double latitude, double longitude , int color) {
        LatLng ubicacion = new LatLng( latitude, longitude );
        switch (color){
            case 1:
                mMap.addMarker(new MarkerOptions().position(ubicacion).title("Accidente #" + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                break;
            case 2 :
                mMap.addMarker(new MarkerOptions().position(ubicacion).title("Accidente #" + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                break;
            case 3 :
                mMap.addMarker(new MarkerOptions().position(ubicacion).title("Accidente #" + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                break;
        }
    }

    /**
     * EVENTO DE ANDROID EN EL CICLO DE VIDA
     * - SE EJECUTA CADA VEZ QUE EL USUARIO ENTRA AL ACTIVITY O VUELVE DE OTRA
     */
    @Override
    protected void onResume() {
        super.onResume();
        //getDatos();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        getDatos();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SupportMapFragment mapFragment = (SupportMapFragment)  getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    @SuppressWarnings("deprecation")
    private void getDeviceLocation() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }

        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        float DEFAULT_ZOOM = 17;
        /*
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else
            */
         if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
             mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),
                     mLastKnownLocation.getLongitude())).title("TU" ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            Log.d("melo_consola", "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 9));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

}
