package com.example.melo.UI;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.melo.R;

public class Informacion extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);

        WebView web = findViewById( R.id.web );
        web.loadUrl( getString( R.string.url_html ) );


    }




}
