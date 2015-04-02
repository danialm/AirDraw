package com.example.hellowatch;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.nio.channels.DatagramChannel;
import java.util.Date;

public class MyActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;

    private TextView mTextView;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private float[] DataA;
    private float[] DataG;
    private Button button;
    private boolean flag = false;
    private PutDataMapRequest dataMap;

    private GoogleApiClient googleClient;
    private long startTime;
    private double[] angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        DataA = new float[3];
        DataG = new float[3];
        angle = new double[3];
        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        dataMap = PutDataMapRequest.create("/data");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Date date = new Date();
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_GRAVITY:
                        DataG = lowPass(event.values, DataG);
                        angle = updateAngle(DataG);
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        DataA = lowPass(event.values, DataA);
                        if (inSession(DataA)) {
                            dataMap.getDataMap().putFloatArray("orgAcc", event.values);
                            dataMap.getDataMap().putFloatArray("acc", DataA);
                            dataMap.getDataMap().putLong("time", date.getTime());
                            dataMap.getDataMap().putDouble("angle", angle[0]);
                            dataMap.getDataMap().putDouble("angle1", angle[1]);
                            dataMap.getDataMap().putDouble("angle2", angle[2]);
                            dataMap.getDataMap().putBoolean("session", inSession(DataA));
                            dataMap.getDataMap().putBoolean("run", true);

                            PutDataRequest request = dataMap.asPutDataRequest();
                            Wearable.DataApi.putDataItem(googleClient, request);
                        } else {
                            DataA = new float[3];
                            DataG = new float[3];
                            if (dataMap.getDataMap().getBoolean("run")) {
                                dataMap.getDataMap().putBoolean("run", false);
                                PutDataRequest request = dataMap.asPutDataRequest();
                                Wearable.DataApi.putDataItem(googleClient, request);
                            }
                        }
                        break;

                }
            }

        };

    }

    private double[] updateAngle(float[] values) {
        if(values == null) return null;
        double[] g = new double[3];
        double norm_Of_g = Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);

        // Normalize the accelerometer vector
        g[0] = values[0] / norm_Of_g;
        g[1] = values[1] / norm_Of_g;
        g[2] = values[2] / norm_Of_g;

        return new double[]{Math.atan2(g[0], g[1]), Math.atan2(g[1], g[0]), Math.atan2(g[2], g[1])};
    }

    private boolean inSession(float[] acc) {
        if(sessionStart(acc)) {
            startTime = new Date().getTime();
        }
        if(startTime == 0)
            return false;
        if(new Date().getTime() - startTime < 400)
            return true;
        return false;
    }

    private Boolean sessionStart(float[] acc) {
        if(Math.sqrt(acc[0]*acc[0]+acc[1]*acc[1]+acc[2]*acc[2])>1) return true;
        else return false;
    }

    public float[] lowPass( float[] input, float[] output ) {
        final float alpha = 0.1f;
        if ( output == null ){
            return input;
        }

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mTextView.setText("Connected to Google Play!");
        button = (Button) findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                flag = !flag;
                if(flag) {
                    mTextView.setText("Sending data...");
                    button.setText("Pause!");

                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SENSOR_DELAY);
                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SENSOR_DELAY);
                }else {
                    mTextView.setText("Not sending data!");
                    button.setText("Start!");

                    mSensorManager.unregisterListener(mSensorListener);
                    if(dataMap.getDataMap().getBoolean("run")) {
                        dataMap.getDataMap().putBoolean("run", false);
                        PutDataRequest request = dataMap.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleClient, request);
                    }
                }

            }
        });
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            //googleClient.disconnect();
        }
        super.onStop();
        Log.v("Finish", "fin");
        finish();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
        mTextView.setText("Google Play connection is suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mTextView.setText("Google Play connection failed!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(mSensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //googleClient.connect();
    }
}
