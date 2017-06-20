package org.sofwerx.swx_sensordemo;

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

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView readoutX, readoutY, readoutZ;
    GraphView gv;

    // Sensors and Manager variables
    private  SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;
    private Sensor magneticSensor;

    //private SensorManager mSensorManager;
    //private Sensor mSensor;
    //private TriggerEventListener mTriggerEventListener;

    private float[] gravity = new float[3];
    private float[] magneticForce = new float[3];
    private float[] linear_acceleration = new float[3];

    // flags to indicate when the device should have sensors enabled
    // and if they are currently listening for events
    private boolean sensorsEnabled = false;
    private boolean sensorsListening = false;

    private static final float RAD_TO_DEGREES = (float) (180.0f / Math.PI);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readoutX = (TextView)findViewById(R.id.readoutTxtX);
        readoutY = (TextView)findViewById(R.id.readoutTxtY);
        readoutZ = (TextView)findViewById(R.id.readoutTxtZ);


        //   mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //   mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        /*
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

        mTriggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                // Do work
            }
        };

        mSensorManager.requestTriggerSensor(mTriggerEventListener, mSensor);
        */

        ToggleButton toggle = (ToggleButton)findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    startEvents();
                    readoutX.setText("ON");
                    readoutY.setText("ON");
                    readoutZ.setText("ON");
                } else {
                    // The toggle is disabled
                    stopEvents();
                    sensorsEnabled = false;
                    readoutX.setText("OFF");
                    readoutY.setText("OFF");
                    readoutZ.setText("OFF");
                }
            }
        });

        initSensors();



        //mSeries1 = new LineGraphSeries<>(generateData());
        mSeriesX = new LineGraphSeries<>();
        mSeriesY = new LineGraphSeries<>();
        mSeriesZ = new LineGraphSeries<>();
        gv = (GraphView) findViewById(R.id.graph);
        gv.addSeries(mSeriesX);
        gv.addSeries(mSeriesY);
        gv.addSeries(mSeriesZ);
        gv.getViewport().setXAxisBoundsManual(true);
        gv.getViewport().setMinX(0);
        gv.getViewport().setMaxX(20);
        gv.getViewport().setYAxisBoundsManual(true);
        gv.getViewport().setMaxY(0.5);
        gv.getViewport().setMinY(-0.5);
    }

    public void onResume() {
        super.onResume();
        if (sensorsEnabled) {
            startEvents();
            //registerListeners();
            //enableGraph();
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        stopEvents();
        //unregisterListeners();
        //disableGraph();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event){
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;


        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];


        Float f = linear_acceleration[0];
        readoutX.setText( f.toString() );
        f = linear_acceleration[1];
        readoutY.setText( f.toString() );
        f = linear_acceleration[2];
        readoutZ.setText( f.toString() );
    }


    public void initSensors() {
        // initialize all sensors and the sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        //gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    // Register all needed sensor listeners
    private void registerListeners() {
        if (!sensorsListening) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            //sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            //sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorsListening = true;
        }
        //Toast.makeText(this, "registerListeners = " + sensorsListening, Toast.LENGTH_SHORT).show();
    }

    // Unregister any listeners that are currently in use
    private void unregisterListeners() {
        if (sensorsListening) {
            sensorManager.unregisterListener(this);
            sensorsListening = false;
        }
        //Toast.makeText(this, "unregisterListeners = " + sensorsListening, Toast.LENGTH_SHORT).show();
    }

    public void startEvents() {
        sensorsEnabled = true;
        registerListeners();
        enableGraph();
    }

    public void stopEvents() {
        sensorsEnabled = false;
        unregisterListeners();
        disableGraph();
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // this graph code based on http://www.android-graphview.org/realtime-chart/

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    //private LineGraphSeries<DataPoint> mSeriesX;
    private LineGraphSeries<DataPoint> mSeriesX, mSeriesY, mSeriesZ;
    //private Runnable mTimer2;
    //private LineGraphSeries<> mSeries2;

    private double graph2LastXValue = 5d;
    // in hindsight, this var is poorly named.  The X refers to the X-axis of the graph,
    // not the X-axis of the accelerometer

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
        mTimer1 = null;
    }

    private void enableGraph() {
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                //mSeriesX.resetData(generateData());

                graph2LastXValue += 1d;
                //mSeriesX.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 20);
                mSeriesX.appendData(new DataPoint(graph2LastXValue, linear_acceleration[0]), true, 20);
                mSeriesY.appendData(new DataPoint(graph2LastXValue, linear_acceleration[1]), true, 20);
                mSeriesZ.appendData(new DataPoint(graph2LastXValue, linear_acceleration[2]), true, 20);

                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer1, 300);
    }

}

