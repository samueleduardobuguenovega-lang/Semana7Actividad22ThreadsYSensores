package com.example.semana7actividad22threadsysensores;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AResultado extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        TextView TVNombreRecibido = (TextView) findViewById(R.id.TVNombreRecibido);

        // Recibe el dato enviado desde MainActivity mediante el Intent explicito.
        String StNombre = getIntent().getStringExtra("STNombre");
        if (StNombre == null || StNombre.trim().isEmpty()) {
            TVNombreRecibido.setText(R.string.resultado_sin_nombre);
        } else {
            TVNombreRecibido.setText(getString(R.string.resultado_prefijo, StNombre));
        }

        // Intent implicito: comparte el nombre recibido con otras aplicaciones.
        Button BTCompartir = (Button) findViewById(R.id.BTCompartir);
        BTCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String StValor = TVNombreRecibido.getText().toString();
                Intent sIntentEnviar = new Intent(Intent.ACTION_SEND);
                sIntentEnviar.setType("text/plain");
                sIntentEnviar.putExtra(Intent.EXTRA_TEXT, StValor);
                Intent sChooser = Intent.createChooser(sIntentEnviar, getString(R.string.titulo_compartir));
                try {
                    startActivity(sChooser);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(AResultado.this, R.string.msg_sin_app_compartir, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Cierra la Activity y vuelve a la anterior.
        Button BTVolver = (Button) findViewById(R.id.BTVolver);
        BTVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
