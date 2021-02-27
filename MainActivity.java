package es.upm.dit.adsw.compass;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Ejercicio4 ADSW
 *
 * @version 11/05/2019
 * @author Mateo Sarria Franco de Sarabia
 * @author Daniel Gómez Campo
 * @author Andrés Alfaro Fernández
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;
    private static Sensor mMagneticSensor;
    private boolean mLastMagnetometerSet;
    private boolean mLastAccelerometerSet;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private static int grados;
    private static String cardinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if(mAccelerometer!= null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        if(mMagneticSensor!= null) {
            mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     *  Retirar el registro cuando la aplicaciónno no está visible
     *  Evita el consumo innecesario de bateria y otros recursos
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Retirar el registro cuando la aplicaciónno no está visible
        if (mAccelerometer != null)
            mSensorManager.unregisterListener(this,mMagneticSensor);
        if (mMagneticSensor != null)
            mSensorManager.unregisterListener(this, mMagneticSensor);
        //Evita el consumo innecesario de bateria
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mMagneticSensor) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
            mLastMagnetometerSet = true;
        } else if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
            mLastAccelerometerSet = true;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            double radianes = orientationAngles[0] + Math.PI;
            grados = ((int) Math.toDegrees(radianes));
            //String gradosString = Integer.toString(grados);

            TextView textoGrados = (TextView) findViewById(R.id.grados);
            textoGrados.setText( grados + "º");
        }

        TextView textoGrados = (TextView) findViewById(R.id.grados);
        TextView puntoCardinal = (TextView) findViewById(R.id.pCardinal);

        if (textoGrados != null) {
            if (grados > 23 && grados < 68) {
                //Suroeste
                cardinal = "Suroeste";
            } else if (grados >= 68 && grados <= 113) {
                //OESTE
                cardinal = "OESTE";
            } else if (grados > 113 && grados < 158) {
                //Noroeste
                cardinal = "Noroeste";
            } else if (grados >= 158 && grados <= 203) {
                //NORTE
                cardinal = "NORTE";
            } else if (grados > 203 && grados < 248) {
                //Noreste
                cardinal = "Noreste";
            } else if (grados >= 248 && grados <= 293) {
                //ESTE
                cardinal = "ESTE";
            } else if (grados > 293 && grados < 337) {
                //Sureste
                cardinal = "Sureste";
            } else if ((grados <= 23 && grados >= 0) || (grados >=337 && grados <= 360)){
                //SUR
                cardinal = "SUR";
            }

            puntoCardinal.setText(cardinal);
        }
    }

    /**
     * Imprime el texto escrito por el usuario: "Estoy en" + ubicacion
     * @param view
     */
    public void imprimirTexto(View view) {

        EditText et = (EditText) findViewById(R.id.wText);
        String ubicacion = et.getText().toString();

        TextView tw = (TextView) findViewById(R.id.grados);
        String grados = tw.getText().toString();

        TextView texto = (TextView) findViewById(R.id.rText);

        if (ubicacion.isEmpty()) {
            String error1 = "No hay ubicacion";
            Toast.makeText(this, error1, Toast.LENGTH_SHORT).show();    //mensaje de "error"
            texto.setText(error1);
        } else {
            texto.setText("Estoy en " + ubicacion + " con una orientacion de " + grados + " grados");
        }
    }


    /**
     * Comparte la ubicacion con apps del dispositivo
     * Si no hay ubicacion devuelve un toast
     * @param view
     */
    public void compartirUbicacion(View view) {

        Intent intent = new Intent(Intent.ACTION_SEND);

        TextView tv = (TextView) findViewById(R.id.rText);
        String ubicacion = tv.getText().toString();

        if (ubicacion.isEmpty()) {
            String error1 = "No hay ubicacion";
            Toast.makeText(this, error1, Toast.LENGTH_SHORT).show();    //mensaje de "error"
            return;
        }else {
            intent.putExtra(Intent.EXTRA_TEXT, ubicacion);
            intent.setType("test/plain");
        }
            startActivity(intent);
    }

    /**
     * Abre la app de Maps con la ubicacion escrita
     * @param view Si no hay ninguna ubicacion da un Toast
     *             Si no encuentra la app de Maps en el dispositivo, lanza un Toast
     */
    public void maps(View view) {

        EditText editText = (EditText) findViewById(R.id.wText);
        String ubicacion = editText.getText().toString();

        if (ubicacion.isEmpty()) {
            String error1 = "No hay ubicacion";
            Toast.makeText(this, error1, Toast.LENGTH_SHORT).show();        //mensaje de "error"
            return;
        } else {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + ubicacion);
            Intent mapaIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapaIntent.setPackage("com.google.android.apps.maps");

            if (mapaIntent.resolveActivity(getPackageManager()) == null) {
                String error2 = "No se encuentra app";
                Toast.makeText(this, error2, Toast.LENGTH_SHORT).show();    //mensaje de "error"
                return;
            } else {
                startActivity(mapaIntent);
            }

        }
    }
}
