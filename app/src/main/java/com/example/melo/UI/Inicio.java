package com.example.melo.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.melo.R;

public class Inicio extends AppCompatActivity {
    ImageView peaton , funcionario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        peaton = findViewById(R.id.peaton);
        funcionario = findViewById(R.id.funcionario);

        peaton.setOnClickListener(
                view -> startActivity(new Intent(getBaseContext(),MapsActivity.class))
        );
        funcionario.setOnClickListener(
                view -> startActivity(new Intent(getBaseContext(),MapsActivity2.class))
        );

    }
}
