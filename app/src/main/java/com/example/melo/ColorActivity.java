package com.example.melo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.melo.VOLLEY.MySingleton;
import com.example.melo.VOLLEY.VolleyMultipartRequest;
import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ColorActivity extends AppCompatActivity {

    private CheckBox rojo, azul , amarillo ;
    private double longitud, latitud;


    private MagicalPermissions magicalPermissions;
    private MagicalCamera magicalcamera ;
    private final static  int  RESIZE_PHOTO_PIXELS_PERCENTAGE = 50;
    ImageView img;

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
        continuar.setOnClickListener(view -> validar());

        Button fotin = findViewById(R.id.fotin);

        img = findViewById(R.id.imageView);

        //permisos par de la camara
        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };


// aceptamos los permisos
        magicalPermissions = new MagicalPermissions(this  , permissions);

        //instanciamos la camara

         magicalcamera = new MagicalCamera(this , RESIZE_PHOTO_PIXELS_PERCENTAGE , magicalPermissions);



         fotin.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 magicalcamera.takePhoto();
             }
         });




    }
    @Override
    public  void  onActivityResult(int requestCode , int resultCode , Intent data){
        super.onActivityResult(requestCode , resultCode , data);
        if(resultCode==RESULT_OK){
            magicalcamera.resultPhoto(requestCode , resultCode , data);
            img.setImageBitmap(magicalcamera.getPhoto());

            magicalcamera.savePhotoInMemoryDevice(magicalcamera.getPhoto(), "myPhotoname","mydirectorname" , MagicalCamera.JPEG , true);

            uploadImage(magicalcamera.getPhoto());

        }

    }

    private RequestQueue rQueue;




    private void uploadImage(final Bitmap bitmap){

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, "http://172.16.160.110/app/accidentes/camera/upload.php",
                response -> {
                    Log.d("asdfghj",response.toString());
                },
                error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show()) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */

            /*
             *pass files using below method
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("name", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };


        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue = Volley.newRequestQueue(ColorActivity.this);
        rQueue.add(volleyMultipartRequest);
    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    public void validar (){
        int color = 0;
        if (rojo.isChecked()){
            color = 1;
        }else if (amarillo.isChecked()){
            color = 2;
        }else if (azul.isChecked()){
            color = 3;
        }
        if ( color > 0 ) {
            guardarDatosEnBd(
                    color
            );
        }else{
            Toast.makeText(
                    this,
                    "No se ha seleccionado color",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void guardarDatosEnBd(int color)
    {
        String url_preticion = getString(R.string.url_guardar);
        StringRequest strReq = new StringRequest(Request.Method.POST, url_preticion,
                response -> {
                    finish();
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
                params.put("longitud", String.valueOf( longitud ) );
                params.put("latitud", String.valueOf( latitud ) );
                params.put("color", String.valueOf( color ) );
                return params;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(strReq);
    }

}
