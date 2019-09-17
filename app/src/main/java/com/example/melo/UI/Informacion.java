package com.example.melo.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.melo.R;

public class Informacion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);

        WebView web = findViewById( R.id.web );
        web.loadUrl("http://172.16.160.72:8080/paso_a_seguir.html");


    }




}
