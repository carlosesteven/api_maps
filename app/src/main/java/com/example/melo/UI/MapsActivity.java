package com.example.melo.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.melo.ColorActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.melo.Util.ConsolaDebug;
import static com.example.melo.Util.ConsolaDebugError;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    // CORDENADAS DE CALI
    private LatLng mDefaultLocation = new LatLng(3.4372201,-76.5224991);
    private boolean mLocationPermissionGranted;

    private boolean lanzoMapa = false;

    private final int CODIGO_GPS = 100;

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

                Intent color = new Intent( getBaseContext(), ColorActivity.class);
                color.putExtra( "longitud", mLastKnownLocation.getLongitude() );
                color.putExtra( "latitud", mLastKnownLocation.getLatitude() );
                startActivity( color );
                /*
                guardarDatosEnBd(
                        1
                );
                */
            }
        });

        Button informacion = findViewById(R.id.info);

        informacion.setOnClickListener(
                view -> startActivity(new Intent(getBaseContext(),Informacion.class))
        );

    }

    private void getDatos()
    {
        String url_preticion = getString(R.string.url_consultar);
        StringRequest strReq = new StringRequest(Request.Method.POST, url_preticion, response ->
        {
            try {
                JSONArray datos = new JSONArray( response );
                for ( int i = 0; i < datos.length(); i++ )
                {
                    JSONObject actual = datos.getJSONObject( i );
                    imprimirEnMapa(
                            actual.getInt("id"),
                            Double.parseDouble( actual.getString("latitud") ),
                            Double.parseDouble( actual.getString("longitud") ),
                            actual.getInt("color")
                    );
                }
                ConsolaDebug("resultados ciclo", datos.toString() );
            } catch (JSONException e) {
                if (e.getMessage() != null) {
                    ConsolaDebugError("melo_consola", e.getMessage());
                }
                e.printStackTrace();
            }
        }, error -> {
            if ( error != null && error.getMessage() != null ) {
                ConsolaDebugError("melo_consola", error.getMessage());
            }
            ConsolaDebugError("melo_consola", "error en volley" );
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Seteamos el tipo de mapa
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        getDatos();

        lanzoMapa = true;
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
        }else{
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_GPS
            );
        }

        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        /*
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else
            */
         if (mLastKnownLocation != null) {
             // NIVEL DE ZOOM POR DEFECTO
             float DEFAULT_ZOOM = 18;
             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            //mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),
            //         mLastKnownLocation.getLongitude())).title("TU" ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
             ConsolaDebug("melo_consola", "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 9));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( lanzoMapa )
            getDatos();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == CODIGO_GPS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        CODIGO_GPS
                );
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
