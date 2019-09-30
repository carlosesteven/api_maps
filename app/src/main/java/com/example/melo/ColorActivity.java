package com.example.melo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.melo.VOLLEY.MySingleton;
import com.example.melo.VOLLEY.VolleyMultipartRequest;
import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.example.melo.Util.ConsolaDebug;
import static com.example.melo.Util.ConsolaDebugError;

public class ColorActivity extends AppCompatActivity {

    private CheckBox rojo, azul , amarillo ;
    private double longitud, latitud;
    private int color = 0;
    private String nombreFoto;


    private MagicalCamera magicalcamera ;
    private final static  int  RESIZE_PHOTO_PIXELS_PERCENTAGE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        latitud = getIntent().getDoubleExtra("latitud", 0);
        longitud = getIntent().getDoubleExtra("longitud", 0);
        rojo = findViewById(R.id.rojo);
        azul = findViewById(R.id.azul);
        amarillo = findViewById(R.id.amarillo2);
        Button continuar = findViewById(R.id.continuar);
        continuar.setOnClickListener(view ->
                validar()
        );

        //permisos par de la camara
        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        // aceptamos los permisos
        MagicalPermissions magicalPermissions = new MagicalPermissions(this, permissions);

        //instanciamos la camara

         magicalcamera = new MagicalCamera(this , RESIZE_PHOTO_PIXELS_PERCENTAGE , magicalPermissions);

    }
    @Override
    public  void  onActivityResult(int requestCode , int resultCode , Intent data){
        super.onActivityResult(requestCode , resultCode , data);
        if(resultCode==RESULT_OK){
            magicalcamera.resultPhoto(requestCode , resultCode , data);

            magicalcamera.savePhotoInMemoryDevice(magicalcamera.getPhoto(), "myPhotoname","mydirectorname" , MagicalCamera.PNG , true);

            uploadImage(magicalcamera.getPhoto());

        }

    }


    private void uploadImage(final Bitmap bitmap){


        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                getString( R.string.url_guardar_foto ),
                response -> {
                    ConsolaDebug("uploadImage", response.toString() );
                    guardarDatosEnBd();
                },
                error -> {
                    ConsolaDebugError("uploadImage", "error en volley" );
                    guardarDatosEnBd();
                })
        {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                nombreFoto = imagename + ".png";
                params.put("archivo", new DataPart(nombreFoto, getFileDataFromDrawable(bitmap)));
                return params;
            }
        };


        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue rQueue = Volley.newRequestQueue(ColorActivity.this);
        rQueue.add(volleyMultipartRequest);

    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    public void validar (){
        if (rojo.isChecked()){
            color = 1;
        }else if (amarillo.isChecked()){
            color = 2;
        }else if (azul.isChecked()){
            color = 3;
        }
        if ( color > 0 ) {
            magicalcamera.takePhoto();
        }else{
            Toast.makeText(
                    this,
                    "No se ha seleccionado color",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void guardarDatosEnBd()
    {

        StringRequest strReq = new StringRequest(Request.Method.POST, getString(R.string.url_guardar_datos),
                response -> {
                    ConsolaDebug("guardarDatosEnBd", response);
                    finish();
                },
                error ->
                {
                    if ( error != null && error.getMessage() != null ) {
                        ConsolaDebugError("guardarDatosEnBd", error.getMessage());
                    }
                    ConsolaDebugError("guardarDatosEnBd", "error en volley" );
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("longitud", String.valueOf( longitud ) );
                params.put("latitud", String.valueOf( latitud ) );
                params.put("color", String.valueOf( color ) );
                params.put("foto", nombreFoto );
                return params;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(strReq);
    }

}
