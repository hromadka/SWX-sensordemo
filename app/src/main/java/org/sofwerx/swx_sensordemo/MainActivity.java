package org.sofwerx.swx_sensordemo;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static Context context;

    private TextView readoutX, readoutY, readoutZ, tv;
    private GraphView gv;

    public File logfile;
    public FileHelper fileHelper;

    // Sensors and Manager variables
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    //private Sensor gyroscopeSensor;
    private Sensor magneticSensor;
    private Sensor audioSensor;
    //private TriggerEventListener mTriggerEventListener;

    private float[] gravity = new float[3];
    private float[] magneticForce = new float[3];
    private float[] linear_acceleration = new float[3];

    private boolean sensorsEnabled = false;
    private boolean sensorsListening = false;

    private long tick = 0;
    private boolean logging = false;

    private static final float RAD_TO_DEGREES = (float) (180.0f / Math.PI);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        fileHelper = new FileHelper(context);

        initUIControls();
        initSensors();
        initGraph();
    }

    public void onResume() {
        super.onResume();
        if (sensorsEnabled) {
            startSensorEvents();
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        stopSensorEvents();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    public void onSensorChanged(SensorEvent event){
        // may need to move this off the UI thread

        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;
        tick++;

        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];


                Float f = linear_acceleration[0];
                readoutX.setText(f.toString());
                f = linear_acceleration[1];
                readoutY.setText(f.toString());
                f = linear_acceleration[2];
                readoutZ.setText(f.toString());

                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                // Isolate the force of gravity with the low-pass filter.
                magneticForce[0] = alpha * magneticForce[0] + (1 - alpha) * event.values[0];
                magneticForce[1] = alpha * magneticForce[1] + (1 - alpha) * event.values[1];
                magneticForce[2] = alpha * magneticForce[2] + (1 - alpha) * event.values[2];

                break;
        }

        if (logging) {
            logData(String.valueOf(tick)
                    + "," + String.valueOf(linear_acceleration[0])
                    + "," + String.valueOf(linear_acceleration[1])
                    + "," + String.valueOf(linear_acceleration[2])
                    + "," + String.valueOf(magneticForce[0])
                    + "," + String.valueOf(magneticForce[1])
                    + "," + String.valueOf(magneticForce[2])
                    + System.lineSeparator());
            tv.setText("LOGGED " + String.valueOf(tick));
        }


    }

    /////////////////////////////////////////////////////////////////////////////////
    // LPF based on https://github.com/raweng/augmented-reality-view
    //
    // example usage:
    // onSensorChanged(SensorEvent evt) { accelVals = lowPass(evt.values.clone(), accelVals); }
    //
    static final float ALPHA = 0.25f;
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    protected float[] highPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i=0; i<input.length; i++) {
            output[i] = output[i] + (1f/ALPHA) * (input[i] - output[i]);
        }
        return output;
    }



    public void initSensors() {
        // initialize all sensors and the sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        //gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

    }

    private void initUIControls() {
        readoutX = (TextView)findViewById(R.id.readoutTxtX);
        readoutY = (TextView)findViewById(R.id.readoutTxtY);
        readoutZ = (TextView)findViewById(R.id.readoutTxtZ);

        tv = (TextView)findViewById(R.id.loggingStatus);

        ToggleButton toggleLogging = (ToggleButton)findViewById(R.id.toggleLogging);
        if (fileHelper.isExternalStorageWritable()) {
            toggleLogging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        startLogging();
                    } else {
                        stopLogging();
                    }
                }
            });
        } else {
            tv.setText("LOG: NO SDCARD?");
            toggleLogging.setEnabled(false);
        }

        ToggleButton toggleSensors = (ToggleButton)findViewById(R.id.toggleSensors);
        toggleSensors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggleSensors is enabled
                    startSensorEvents();
                } else {
                    // The toggleSensors is disabled
                    stopSensorEvents();
                    sensorsEnabled = false;
                    readoutX.setText("ACCEL_X OFF");
                    readoutY.setText("ACCEL_Y OFF");
                    readoutZ.setText("ACCEL_Z OFF");
                }
            }
        });
    }

    private void initGraph() {
        // accelerometer
        mSeriesX = new LineGraphSeries<>();
        mSeriesX.setColor(Color.RED);
        mSeriesY = new LineGraphSeries<>();
        mSeriesY.setColor(Color.GREEN);
        mSeriesZ = new LineGraphSeries<>();
        mSeriesZ.setColor(Color.BLUE);

        mSeriesMag3D = new LineGraphSeries<>();
        mSeriesMag3D.setColor(Color.BLACK);

        gv = (GraphView) findViewById(R.id.graph);
        gv.addSeries(mSeriesX);
        gv.addSeries(mSeriesY);
        gv.addSeries(mSeriesZ);
        gv.addSeries(mSeriesMag3D);
        gv.getViewport().setXAxisBoundsManual(true);
        gv.getViewport().setMinX(0);
        gv.getViewport().setMaxX(20);
        gv.getViewport().setYAxisBoundsManual(true);
        gv.getViewport().setMaxY(0.5);
        gv.getViewport().setMinY(-0.5);
    }

    // Register all needed sensor listeners
    private void registerListeners() {
        if (!sensorsListening) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            //sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorsListening = true;
        }
    }

    // Unregister any listeners that are currently in use
    private void unregisterListeners() {
        if (sensorsListening) {
            sensorManager.unregisterListener(this);
            sensorsListening = false;
        }
    }

    public void startSensorEvents() {
        sensorsEnabled = true;
        registerListeners();
        enableGraph();
    }

    public void stopSensorEvents() {
        sensorsEnabled = false;
        unregisterListeners();
        disableGraph();
    }

    private void startLogging() {
        logfile = fileHelper.openPublicFile("log" + getTimestamp() + ".csv");
        fileHelper.writeToFile(logfile, getTimestamp());
        tick = 0;
        logging = true;
        tv.setText("LOGGING ON");
    }

    private void stopLogging() {
        logging = false;
        tv.setText("LOGGING OFF");
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    }

    public void logData(final String fileContents) {
        fileHelper.writeToFile(logfile, fileContents);
    }



    /////////////////////////////////////////////////////////////////////////////////////////
    // this graph code based on http://www.android-graphview.org/realtime-chart/
    //
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeriesX, mSeriesY, mSeriesZ, mSeriesMag3D;
    //private Runnable mTimer2;
    //private LineGraphSeries<> mSeries2;

    private double graph2LastXValue = 5d;
    // in hindsight, this var is poorly named.  The X refers to the X-axis of the graph,
    // not the X-axis of the accelerometer.

    double mLastRandom = 2;
    Random mRand = new Random();

    private DataPoint[] generateData() {
        int count = 20;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }

    private void disableGraph() {
        mHandler.removeCallbacks(mTimer1);
        mTimer1 = null;  // hard stop
    }

    private void enableGraph() {
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                mSeriesX.appendData(new DataPoint(graph2LastXValue, linear_acceleration[0]), true, 20);
                mSeriesY.appendData(new DataPoint(graph2LastXValue, linear_acceleration[1]), true, 20);
                mSeriesZ.appendData(new DataPoint(graph2LastXValue, linear_acceleration[2]), true, 20);

                Float f = (magneticForce[0]*magneticForce[0] +
                        magneticForce[1]*magneticForce[1] +
                        magneticForce[2]*magneticForce[2]);
                Double d2 = (double) f;
                Double d = Math.sqrt(d2);
                mSeriesMag3D.appendData(new DataPoint(graph2LastXValue, d), true, 20);

                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer1, 200);
    }

}

