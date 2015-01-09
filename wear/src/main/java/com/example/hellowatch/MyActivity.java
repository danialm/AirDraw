package com.example.hellowatch;

import android.app.Activity;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class MyActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextView;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private float[] DataA1 = new float[3];
    private float[] DataA2 = new float[3];
    private float[] DataG1 = new float[3];
    private float[] DataG2 = new float[3];
  	private float[] DataM = new float[3];
    private float[] DataM1 = new float[3];
    private float[] DataM2 = new float[3];
    private float[] DataG = new float[3];
    private float[] Rot = new float[16];
    private float[] Inc = new float[16];
    private float[] A = new float[3];
    private float[] B = new float[3];
    private int cb = 0;
    private int ca = 0;
    Button button;
    boolean flag = false;
    PutDataMapRequest dataMap;

    GoogleApiClient googleClient;

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

        WifiManager wm = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wm.getSc
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

                switch( event.sensor.getType() ) {
                    case Sensor.TYPE_GRAVITY:
                        if(DataG1[0] == 0.0){
                            DataG1[0] = event.values[0];
                            DataG1[1] = event.values[1];
                            DataG1[2] = event.values[2];
                        }else if(DataG2[0] == 0.0){
                            DataG2[0] = event.values[0];
                            DataG2[1] = event.values[1];
                            DataG2[2] = event.values[2];
                        }else {
                            DataG[0] = (DataG1[0]+DataG2[0]+event.values[0])/3;
                            DataG[1] = (DataG1[1]+DataG2[1]+event.values[1])/3;
                            DataG[2] = (DataG1[2]+DataG2[2]+event.values[2])/3;
                        }
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        if(DataM1[0] == 0.0){
                            DataM1[0] = event.values[0];
                            DataM1[1] = event.values[1];
                            DataM1[2] = event.values[2];
                        }else if(DataM2[0] == 0.0){
                            DataM2[0] = event.values[0];
                            DataM2[1] = event.values[1];
                            DataM2[2] = event.values[2];
                        }else {
                            DataM[0] = (DataM1[0]+DataM2[0]+event.values[0])/3;
                            DataM[1] = (DataM1[1]+DataM2[1]+event.values[1])/3;
                            DataM[2] = (DataM1[2]+DataM2[2]+event.values[2])/3;
                        }
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
//                        A[0] += event.values[0];
//                        A[1] += event.values[1];
//                        A[2] += event.values[2];
//                        ca++;
//                        if(ca == 10000){
//                            Log.v("Linear Ave", A[0] / 10000f + " " + A[1] / 10000f + " " + A[2] / 10000f);
//                            ca = 0;
//                            A = new float [3];
//                        }
                        if(DataA1[0] == 0.0){
                            DataA1[0] = event.values[0];
                            DataA1[1] = event.values[1];
                            DataA1[2] = event.values[2];
                        }else if(DataA2[0] == 0.0){
                            DataA2[0] = event.values[0];
                            DataA2[1] = event.values[1];
                            DataA2[2] = event.values[2];
                        }else {
                            float[] dataA = new float[3];
                            dataA[0] = (event.values[0] + DataA1[0] + DataA2[0])/3 ;
                            dataA[1] = (event.values[1] + DataA1[1] + DataA2[1])/3 ;
                            dataA[2] = (event.values[2] + DataA1[2] + DataA2[2])/3 ;
                            if (mSensorManager.getRotationMatrix(Rot, Inc, DataG, DataM)) {

                                dataMap.getDataMap().putFloatArray("acc", dataA);
                                dataMap.getDataMap().putLong("time", date.getTime());
                                dataMap.getDataMap().putFloatArray("rot", Rot);
//                                dataMap.getDataMap().putString("test: ", DataA1[0]+", "+ DataA2[0]+", "+ event.values[0]);
                                PutDataRequest request = dataMap.asPutDataRequest();
                                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                        .putDataItem(googleClient, request);
                            }

                            DataA1 = new float[3];
                            DataA2 = new float[3];
                            DataG1 = new float[3];
                            DataG2 = new float[3];
                        }
					break;
	              default:
                    return;
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

                    dataMap.getDataMap().putBoolean("run", true);
                    PutDataRequest request = dataMap.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                            .putDataItem(googleClient, request);

                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
                    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

                }else {
                    mTextView.setText("Not sending data!");
                    button.setText("Start!");
                    mSensorManager.unregisterListener(mSensorListener);

                    dataMap.getDataMap().putBoolean("run", false);
                    PutDataRequest request = dataMap.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                            .putDataItem(googleClient, request);
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
