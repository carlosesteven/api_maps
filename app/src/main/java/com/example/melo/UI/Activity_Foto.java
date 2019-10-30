package com.example.melo.UI;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.melo.R;
import com.squareup.picasso.Picasso;

public class Activity_Foto extends AppCompatActivity {
ImageView imagen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__foto);

        String url = getIntent().getStringExtra("url_foto");
        imagen = findViewById(R.id.image);


        Picasso.get()
                .load( "http://csc-lab.xyz/accidentes/archivos/" + url)
                .into( imagen );

    }
}
