package com.example.semana7actividad22threadsysensores;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Captura de datos de entrada con sensores (parte 2 de la pauta).
 *
 * Se usa el vector de rotacion (TYPE_ROTATION_VECTOR) como en el ejemplo 2 de la pauta y, ademas,
 * el acelerometro, para mostrar que se pueden combinar varias entradas de datos.
 *
 * Los listeners se registran en onResume y se desregistran en onPause: si no, el sensor seguiria
 * consumiendo bateria con la pantalla apagada.
 */
public class ASensores extends AppCompatActivity implements SensorEventListener {

    private SensorManager sSensorManager;
    private Sensor sSensorRotacion;
    private Sensor sSensorAceleracion;

    private TextView sTVVector;
    private TextView sTVAngulos;
    private TextView sTVAceleracion;
    private TextView sTVMuestras;
    private TextView sTVEstado;
    private TextView sTVListado;

    private final float[] sMatrizRotacion = new float[9];
    private final float[] sVectorRotacion = new float[5];
    private int sMuestrasRotacion;
    private int sMuestrasAceleracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensores);

        sTVVector = (TextView) findViewById(R.id.TVVectorRotacion);
        sTVAngulos = (TextView) findViewById(R.id.TVAngulos);
        sTVAceleracion = (TextView) findViewById(R.id.TVAceleracion);
        sTVMuestras = (TextView) findViewById(R.id.TVMuestras);
        sTVEstado = (TextView) findViewById(R.id.TVEstadoSensores);
        sTVListado = (TextView) findViewById(R.id.TVListadoSensores);

        sSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sSensorRotacion = sSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sSensorAceleracion = sSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sTVListado.setText(listarSensores());

        Button BTVolverSensores = (Button) findViewById(R.id.BTVolverSensores);
        BTVolverSensores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean sRotacion = false;
        boolean sAceleracion = false;
        if (sSensorRotacion != null) {
            sRotacion = sSensorManager.registerListener(this, sSensorRotacion, SensorManager.SENSOR_DELAY_UI);
        }
        if (sSensorAceleracion != null) {
            sAceleracion = sSensorManager.registerListener(this, sSensorAceleracion, SensorManager.SENSOR_DELAY_UI);
        }
        sTVEstado.setText(getString(R.string.sensores_estado,
                sRotacion ? getString(R.string.sensores_activo) : getString(R.string.sensores_ausente),
                sAceleracion ? getString(R.string.sensores_activo) : getString(R.string.sensores_ausente)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        sSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sEvento) {
        if (sEvento.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            sMuestrasRotacion++;
            System.arraycopy(sEvento.values, 0, sVectorRotacion, 0,
                    Math.min(sEvento.values.length, sVectorRotacion.length));
            sTVVector.setText(getString(R.string.sensores_vector,
                    sVectorRotacion[0], sVectorRotacion[1], sVectorRotacion[2]));

            // La matriz de rotacion se obtiene del vector y de ahi salen los angulos de orientacion.
            SensorManager.getRotationMatrixFromVector(sMatrizRotacion, sEvento.values);
            float[] sAngulos = Orientacion.calcularAngulos(sMatrizRotacion);
            sTVAngulos.setText(getString(R.string.sensores_angulos, sAngulos[0], sAngulos[1], sAngulos[2]));
        } else if (sEvento.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sMuestrasAceleracion++;
            double sModulo = Orientacion.modulo(sEvento.values[0], sEvento.values[1], sEvento.values[2]);
            sTVAceleracion.setText(getString(R.string.sensores_aceleracion,
                    sEvento.values[0], sEvento.values[1], sEvento.values[2], sModulo));
        }
        sTVMuestras.setText(getString(R.string.sensores_muestras, sMuestrasRotacion, sMuestrasAceleracion));
    }

    @Override
    public void onAccuracyChanged(Sensor sSensor, int sPrecision) {
        // No se usa: la actividad solo muestra los valores entregados por el sistema.
    }

    /** Arma el texto con todos los sensores que trae el equipo (otras entradas de datos posibles). */
    private String listarSensores() {
        List<Sensor> sSensores = sSensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder sTexto = new StringBuilder();
        sTexto.append(getString(R.string.sensores_detectados, sSensores.size()));
        for (Sensor sSensor : sSensores) {
            sTexto.append("\n").append(sSensor.getName());
        }
        return sTexto.toString();
    }
}
