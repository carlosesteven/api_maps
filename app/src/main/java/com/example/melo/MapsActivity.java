package com.example.melo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button button3 = findViewById( R.id.button3 );

        button3.setOnClickListener(view -> {
            if ( mMap != null )
            {
                double lat = mMap.getCameraPosition().target.latitude;
                double lon = mMap.getCameraPosition().target.longitude;

                guardarDatosEnBd(
                        lon,
                        lat,
                        1
                );
                /*
                Intent v = new Intent(getBaseContext(), ColorActivity.class);
                v.putExtra("latitude", lat);
                v.putExtra("longitude", lon);
                startActivity( v );
                */
            }

        });

    }

    private void guardarDatosEnBd(final double lon, final double lat, int color)
    {
        String url_preticion = "http://172.16.160.110/anime/proyecto/set_datos.php";
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
                params.put("longitude", String.valueOf( lon ) );
                params.put("latitude", String.valueOf( lat ) );
                params.put("color", String.valueOf( color ) );
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
                mMap.clear();
                for ( int i = 0; i < datos.length(); i++ )
                {
                    JSONObject actual = datos.getJSONObject( i );
                    imprimirEnMapa(
                            actual.getInt("id"),
                            actual.getLong("latitude"),
                            actual.getLong("longitude"),
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

    public void imprimirEnMapa( int id , double longitude , double latitude , int color) {
        LatLng ubicacion = new LatLng( latitude , longitude);
        Log.e("melo_consola", "IMPRIMIO PUNTO # " + id );
        switch (color){
            case 1:
                mMap.addMarker(new MarkerOptions().position(ubicacion).title("Accidente #" + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                Log.e("melo_consola", "IMPRIMIO COLOR 1" );
                break;
            case 2 :
                mMap.addMarker(new MarkerOptions().position(ubicacion).title("Accidente #" + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                Log.e("melo_consola", "IMPRIMIO COLOR 2" );
                break;
            case 3 :
                mMap.addMarker(new MarkerOptions().position(ubicacion).title("Accidente #" + id ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                Log.e("melo_consola", "IMPRIMIO COLOR 3" );
                break;
        }
        Log.e("melo_consola", " " );
    }

    /**
     * EVENTO DE ANDROID EN EL CICLO DE VIDA
     * - SE EJECUTA CADA VEZ QUE EL USUARIO ENTRA AL ACTIVITY O VUELVE DE OTRA
     */
    @Override
    protected void onResume() {
        super.onResume();
        getDatos();
    }

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


}
