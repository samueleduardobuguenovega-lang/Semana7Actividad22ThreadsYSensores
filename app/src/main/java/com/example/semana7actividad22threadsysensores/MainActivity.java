package com.example.semana7actividad22threadsysensores;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * Actividad 2.2 - Threads y Sensores.
 *
 * Mantiene lo de la actividad anterior (intents explicitos e implicitos) y agrega lo pedido en la
 * pauta:
 *   1) un Thread de segundo plano que descarga una imagen y actualiza la interfaz con post();
 *   2) un AsyncTask que procesa la imagen (escala de grises) informando el progreso;
 *   3) un intent explicito hacia la pantalla de sensores (vector de rotacion y acelerometro).
 */
public class MainActivity extends AppCompatActivity {

    private static final String ST_TAG = "Actividad22";

    /** Imagen que descarga el hilo de segundo plano (ejemplo 1 de la pauta). */
    private static final String ST_URL_IMAGEN = "https://picsum.photos/seed/threads/600/400";

    private ImageView sIVFoto;
    private Button sBTDescargar;
    private Button sBTProcesar;
    private Button sBTCompartirImagen;
    private TextView sTVEstadoHilo;
    private TextView sTVEstadoProceso;
    private ProgressBar sPBProceso;

    /** Imagen que se muestra ahora mismo (foto, descarga o respaldo). */
    private Bitmap sBitmapActual;
    /** Resultado del AsyncTask, listo para compartir. */
    private Bitmap sBitmapProcesado;

    /** Lanzador para el Intent implicito de la camara (MediaStore.ACTION_IMAGE_CAPTURE). */
    private final ActivityResultLauncher<Intent> sLauncherFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                                Toast.makeText(MainActivity.this, R.string.msg_foto_cancelada,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            Bundle sExtras = result.getData().getExtras();
                            Object sDato = (sExtras != null) ? sExtras.get("data") : null;
                            if (!(sDato instanceof Bitmap)) {
                                Toast.makeText(MainActivity.this, R.string.msg_foto_sin_imagen,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            sBitmapActual = (Bitmap) sDato;
                            sIVFoto.setImageBitmap(sBitmapActual);
                            sBTProcesar.setEnabled(true);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                new androidx.core.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                        return insets;
                    }
                });

        sIVFoto = (ImageView) findViewById(R.id.IVFoto);
        sBTDescargar = (Button) findViewById(R.id.BTDescargar);
        sBTProcesar = (Button) findViewById(R.id.BTProcesar);
        sBTCompartirImagen = (Button) findViewById(R.id.BTCompartirImagen);
        sTVEstadoHilo = (TextView) findViewById(R.id.TVEstadoHilo);
        sTVEstadoProceso = (TextView) findViewById(R.id.TVEstadoProceso);
        sPBProceso = (ProgressBar) findViewById(R.id.PBProceso);

        // ---------- Lo de la actividad anterior: intents ----------

        // Intent explicito: envia el nombre de la foto a AResultado.
        final Button BTAceptar = (Button) findViewById(R.id.BTAceptar);
        BTAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ETNombreFoto = (EditText) findViewById(R.id.ETNombreFoto);
                String StNombre = ETNombreFoto.getText().toString();
                if (StNombre.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.msg_falta_nombre, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent sIntent = new Intent(MainActivity.this, AResultado.class);
                sIntent.putExtra("STNombre", StNombre);
                startActivity(sIntent);
            }
        });

        // Intent implicito: abre la aplicacion de camara del sistema.
        Button BTHacerFoto = (Button) findViewById(R.id.BTHacerFoto);
        BTHacerFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sIntentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    sLauncherFoto.launch(sIntentCamara);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, R.string.msg_sin_camara, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Limpia el campo de texto y restaura la imagen inicial.
        Button BTLimpiar = (Button) findViewById(R.id.BTLimpiar);
        BTLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ETNombreFoto = (EditText) findViewById(R.id.ETNombreFoto);
                ETNombreFoto.setText("");
                sBitmapActual = null;
                sBitmapProcesado = null;
                sIVFoto.setImageResource(R.mipmap.ic_launcher);
                sTVEstadoHilo.setText(R.string.estado_hilo_inicial);
                sTVEstadoProceso.setText(R.string.estado_async_inicial);
            }
        });

        // ---------- Nuevo: Thread de descarga ----------

        sBTDescargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                descargarEnHiloPropio();
            }
        });

        // ---------- Nuevo: AsyncTask de procesamiento ----------

        sBTProcesar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap sOrigen = (sBitmapActual != null) ? sBitmapActual : imagenDeRespaldo();
                new TareaGris().execute(sOrigen);
            }
        });

        // ---------- Nuevo: compartir la imagen procesada (intent implicito) ----------

        sBTCompartirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compartirImagenProcesada();
            }
        });

        // ---------- Nuevo: pantalla de sensores ----------

        Button BTSensores = (Button) findViewById(R.id.BTSensores);
        BTSensores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ASensores.class));
            }
        });
    }

    /**
     * Thread de segundo plano: descarga la imagen y recien al terminar publica el resultado en la
     * interfaz con post(), porque las vistas solo se pueden tocar desde el hilo principal.
     */
    private void descargarEnHiloPropio() {
        sBTDescargar.setEnabled(false);
        sBTProcesar.setEnabled(false);
        sTVEstadoHilo.setText(R.string.estado_hilo_descargando);
        final long sInicio = System.currentTimeMillis();

        Thread sHilo = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(ST_TAG, "Descarga iniciada en el hilo " + Thread.currentThread().getName());
                final ResultadoDescarga sResultado = descargarImagen();
                final long sDuracion = System.currentTimeMillis() - sInicio;
                Log.i(ST_TAG, "Descarga terminada en " + sDuracion + " ms desde la red: " + sResultado.desdeRed);

                sIVFoto.post(new Runnable() {
                    @Override
                    public void run() {
                        sBitmapActual = sResultado.bitmap;
                        sIVFoto.setImageBitmap(sBitmapActual);
                        if (sResultado.desdeRed) {
                            sTVEstadoHilo.setText(getString(R.string.estado_hilo_ok_red, sDuracion));
                        } else {
                            sTVEstadoHilo.setText(getString(R.string.estado_hilo_ok_local, sDuracion));
                        }
                        sBTDescargar.setEnabled(true);
                        sBTProcesar.setEnabled(true);
                    }
                });
            }
        }, "HiloDescarga");
        sHilo.start();
    }

    /** Devuelve la imagen descargada o, si falla la red, una imagen de respaldo del dispositivo. */
    private ResultadoDescarga descargarImagen() {
        HttpURLConnection sConexion = null;
        try {
            sConexion = (HttpURLConnection) new URL(ST_URL_IMAGEN).openConnection();
            sConexion.setConnectTimeout(5000);
            sConexion.setReadTimeout(5000);
            sConexion.connect();
            if (sConexion.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream sEntrada = sConexion.getInputStream();
                Bitmap sBitmap = BitmapFactory.decodeStream(sEntrada);
                sEntrada.close();
                if (sBitmap != null) {
                    return new ResultadoDescarga(sBitmap, true);
                }
            }
        } catch (Exception e) {
            Log.w(ST_TAG, "No se pudo descargar la imagen: " + e.getMessage());
        } finally {
            if (sConexion != null) {
                sConexion.disconnect();
            }
        }
        return new ResultadoDescarga(imagenDeRespaldo(), false);
    }

    /**
     * Imagen generada en el propio equipo: permite mostrar el trabajo del Thread aunque el celular
     * no tenga internet en la sala.
     */
    private Bitmap imagenDeRespaldo() {
        int sAncho = 600;
        int sAlto = 400;
        Bitmap sBitmap = Bitmap.createBitmap(sAncho, sAlto, Bitmap.Config.ARGB_8888);
        Canvas sLienzo = new Canvas(sBitmap);
        sLienzo.drawColor(Color.rgb(18, 104, 76));
        Paint sPincel = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random sAzar = new Random(22L);
        for (int i = 0; i < 40; i++) {
            sPincel.setColor(Color.argb(60 + sAzar.nextInt(120),
                    sAzar.nextInt(256), sAzar.nextInt(256), sAzar.nextInt(256)));
            sLienzo.drawCircle(sAzar.nextInt(sAncho), sAzar.nextInt(sAlto), 20 + sAzar.nextInt(90), sPincel);
        }
        sPincel.setColor(Color.WHITE);
        sPincel.setTextSize(34);
        sLienzo.drawText("Imagen de respaldo local", 40, 210, sPincel);
        return sBitmap;
    }

    /**
     * AsyncTask pedida por la pauta: procesa la imagen pixel a pixel informando el avance.
     *
     * AsyncTask esta deprecada desde Android 11 (API 30): hoy se usan ExecutorService, corrutinas o
     * WorkManager. Se mantiene aca porque es el aprendizaje esperado de la actividad.
     */
    @SuppressWarnings("deprecation")
    private class TareaGris extends AsyncTask<Bitmap, Integer, Bitmap> {

        private long sInicio;

        @Override
        protected void onPreExecute() {
            sInicio = System.currentTimeMillis();
            sPBProceso.setVisibility(View.VISIBLE);
            sPBProceso.setProgress(0);
            sTVEstadoProceso.setText(R.string.estado_async_procesando);
        }

        @Override
        protected Bitmap doInBackground(Bitmap... sBitmaps) {
            Bitmap sOrigen = sBitmaps[0];
            if (sOrigen == null) {
                return null;
            }
            Log.i(ST_TAG, "Procesando en " + Thread.currentThread().getName());
            int sAncho = sOrigen.getWidth();
            int sAlto = sOrigen.getHeight();
            int[] sPixeles = new int[sAncho * sAlto];
            sOrigen.getPixels(sPixeles, 0, sAncho, 0, 0, sAncho, sAlto);

            int sPaso = Math.max(1, sPixeles.length / 20);
            for (int i = 0; i < sPixeles.length; i++) {
                sPixeles[i] = EscalaGris.pixelGris(sPixeles[i]);
                if (i % sPaso == 0) {
                    publishProgress(i * 100 / sPixeles.length);
                    try {
                        // Pausa didactica: hace visible el progreso en pantalla.
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            Bitmap sSalida = Bitmap.createBitmap(sAncho, sAlto, Bitmap.Config.ARGB_8888);
            sSalida.setPixels(sPixeles, 0, sAncho, 0, 0, sAncho, sAlto);
            return sSalida;
        }

        @Override
        protected void onProgressUpdate(Integer... sValores) {
            sPBProceso.setProgress(sValores[0]);
        }

        @Override
        protected void onPostExecute(Bitmap sBitmap) {
            sPBProceso.setVisibility(View.GONE);
            if (sBitmap == null) {
                sTVEstadoProceso.setText(R.string.estado_async_sin_imagen);
                return;
            }
            sBitmapProcesado = sBitmap;
            sIVFoto.setImageBitmap(sBitmapProcesado);
            long sDuracion = System.currentTimeMillis() - sInicio;
            sTVEstadoProceso.setText(getString(R.string.estado_async_listo, sDuracion));
        }
    }

    /** Intent implicito: guarda la imagen procesada y la ofrece para compartir. */
    private void compartirImagenProcesada() {
        if (sBitmapProcesado == null) {
            Toast.makeText(this, R.string.msg_sin_imagen_procesada, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ContentValues sValores = new ContentValues();
            sValores.put(MediaStore.Images.Media.DISPLAY_NAME, "actividad22_gris.png");
            sValores.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            ContentResolver sResolver = getContentResolver();
            Uri sUri = sResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sValores);
            if (sUri == null) {
                throw new IllegalStateException("No se pudo crear la imagen para compartir");
            }
            OutputStream sSalida = sResolver.openOutputStream(sUri);
            sBitmapProcesado.compress(Bitmap.CompressFormat.PNG, 100, sSalida);
            sSalida.close();

            Intent sIntentEnviar = new Intent(Intent.ACTION_SEND);
            sIntentEnviar.setType("image/png");
            sIntentEnviar.putExtra(Intent.EXTRA_STREAM, sUri);
            sIntentEnviar.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sIntentEnviar, getString(R.string.titulo_compartir_imagen)));
        } catch (Exception e) {
            Log.e(ST_TAG, "Error al compartir la imagen: " + e.getMessage());
            Toast.makeText(this, getString(R.string.msg_error_compartir, String.valueOf(e.getMessage())),
                    Toast.LENGTH_LONG).show();
        }
    }

    /** Resultado de la descarga en segundo plano: la imagen y si vino de la red o del respaldo. */
    private static class ResultadoDescarga {
        private final Bitmap bitmap;
        private final boolean desdeRed;

        ResultadoDescarga(Bitmap sBitmap, boolean sDesdeRed) {
            this.bitmap = sBitmap;
            this.desdeRed = sDesdeRed;
        }
    }
}
