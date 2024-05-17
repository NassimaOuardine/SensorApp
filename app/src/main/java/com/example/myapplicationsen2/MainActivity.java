package com.example.myapplicationsen2;

        import android.Manifest;
        import android.content.pm.PackageManager;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private TextView textView;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;
    private Sensor temperatureSensor;
    private Sensor humiditySensor;

    private float[] gyroscopeValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private float temperatureValue = Float.NaN;
    private float humidityValue = Float.NaN;

    private LocationManager locationManager;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        // Initialisation des capteurs
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        if (gyroscopeSensor == null) {
            textView.append("Le capteur de gyroscope n'est pas disponible sur ce périphérique.\n");
        }

        if (accelerometerSensor == null) {
            textView.append("Le capteur d'accéléromètre n'est pas disponible sur ce périphérique.\n");
        }

        if (temperatureSensor == null) {
            textView.append("Le capteur de température n'est pas disponible sur ce périphérique.\n");
        }

        if (humiditySensor == null) {
            textView.append("Le capteur d'humidité n'est pas disponible sur ce périphérique.\n");
        }

        // Initialisation du gestionnaire de localisation
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Vérification des permissions pour les capteurs corporels et les services de localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Autorisation accordée pour utiliser les capteurs corporels et GPS.", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Autorisation refusée pour utiliser les capteurs corporels et GPS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enregistrer le listener pour les capteurs
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (humiditySensor != null) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Reprendre les mises à jour de la localisation
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Désenregistrer le listener pour économiser la batterie
        sensorManager.unregisterListener(this);
        // Arrêter les mises à jour de la localisation
        locationManager.removeUpdates(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            temperatureValue = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            humidityValue = event.values[0];
        }

        displaySensorValues();
    }

    private void displaySensorValues() {
        StringBuilder sensorData = new StringBuilder();
        sensorData.append(String.format("Gyroscope\nx: %.2f\ny: %.2f\nz: %.2f\n\n",
                gyroscopeValues[0], gyroscopeValues[1], gyroscopeValues[2]));
        sensorData.append(String.format("Accéléromètre\nx: %.2f\ny: %.2f\nz: %.2f\n\n",
                accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]));

        if (!Float.isNaN(temperatureValue)) {
            sensorData.append(String.format("Température ambiante: %.2f °C\n", temperatureValue));
        } else {
            sensorData.append("Température ambiante: Capteur non disponible\n");
        }

        if (!Float.isNaN(humidityValue)) {
            sensorData.append(String.format("Humidité relative: %.2f %%\n", humidityValue));
        } else {
            sensorData.append("Humidité relative: Capteur non disponible\n");
        }

        sensorData.append(String.format("Latitude: %.5f\nLongitude: %.5f\n", latitude, longitude));

        textView.setText(sensorData.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Vous pouvez gérer les changements de précision ici si nécessaire
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        displaySensorValues();
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Gérer le cas où le fournisseur est désactivé
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Gérer le cas où le fournisseur est activé
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Gérer le changement de statut du fournisseur
    }
}
