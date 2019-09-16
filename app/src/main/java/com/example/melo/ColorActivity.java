package com.example.melo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.melo.VOLLEY.MySingleton;

import java.util.HashMap;
import java.util.Map;

public class ColorActivity extends AppCompatActivity {

    private CheckBox rojo, azul , amarillo ;
    private double longitud, latitud;

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
