package com.example.melo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button button3 = findViewById( R.id.button3 );

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mMap != null ) {
                    /*
                    Toast
                            .makeText(getBaseContext(), "Latitude: "
                                    + mMap.getCameraPosition().target.latitude
                                    + "Longitude: "
                                    + mMap.getCameraPosition().target.longitude,
                                    Toast.LENGTH_LONG
                            ).show();
                            */
                    double lat = mMap.getCameraPosition().target.latitude;
                    double lon = mMap.getCameraPosition().target.longitude;
                    guardarDatosEnBd(lat, lon);
                    //Aunt();
                    Log.d("melo_consola", "latitude: " + mMap.getCameraPosition().target.latitude );
                    Log.d("melo_consola", "longitude: " + mMap.getCameraPosition().target.longitude );
                }

            }
        });

        getDatos();

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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);



    }
int i = 0;
//metodo para generar puntos
    public void Aunt (){

        i++;
        LatLng punto_accidente = new LatLng(mMap.getCameraPosition().target.latitude , mMap.getCameraPosition().target.longitude);
        mMap.addMarker(new MarkerOptions().position(punto_accidente).title("Accidente #"+i).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));



    }

    private void guardarDatosEnBd(final double lon, final double lat)
    {
        String url_preticion = "http://172.16.160.110/anime/proyecto/set_datos.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url_preticion, response ->
        {
            Log.e("melo_consola", "peticion ejecutada correctamente" );
            getDatos();
        }, error -> {
            Log.e("melo_consola", error.getMessage() );
            Log.e("melo_consola", "error en volley" );
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("longitude", String.valueOf( lon ) );
                params.put("latitude", String.valueOf( lat ) );

                return params;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(strReq);
    }

    private void getDatos()
    {
        String url_preticion = "http://172.16.160.110/anime/proyecto/consulta.php";
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
                            Double.parseDouble(actual.getString("latitude")),
                            Double.parseDouble(actual.getString("longitude"))
                    );
                    Log.e("melo_consola", actual.toString() );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, error -> {
            Log.e("melo_consola", error.getMessage() );
            Log.e("melo_consola", "error en volley" );
        });
        MySingleton.getInstance(this).addToRequestQueue(strReq);
    }

    private void imprimirEnMapa( int id,  double lon, double lat )
    {
        LatLng punto_accidente = new LatLng( lat , lon);
        mMap.addMarker(new MarkerOptions().position(punto_accidente).title("Accidente # " + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

}
