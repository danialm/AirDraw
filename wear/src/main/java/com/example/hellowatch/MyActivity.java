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

import java.util.Date;

public class MyActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;
    private TextView mTextView;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private float[] DataA;
    private Button button;
    private boolean flag = false;
    private PutDataMapRequest dataMap;
    private GoogleApiClient googleClient;

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
                    case Sensor.TYPE_ACCELEROMETER:
                        System.arraycopy(event.values,0, DataA,0, 3);
                        break;
                    case Sensor.TYPE_GYROSCOPE:

                        dataMap.getDataMap().putFloatArray("gyro", event.values);
                        dataMap.getDataMap().putFloatArray("acc", DataA);
                        dataMap.getDataMap().putLong("time", date.getTime());
                        dataMap.getDataMap().putBoolean("run", true);

                        PutDataRequest request = dataMap.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleClient, request);

                        break;

                }
            }

        };

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

                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SENSOR_DELAY);
                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY);
                }else {
                    mTextView.setText("Not sending data!");
                    button.setText("Start!");

                    mSensorManager.unregisterListener(mSensorListener);

                    dataMap.getDataMap().putBoolean("run", false);
                    PutDataRequest request = dataMap.asPutDataRequest();
                    Wearable.DataApi.putDataItem(googleClient, request);
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
