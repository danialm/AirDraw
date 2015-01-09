package com.example.hellowatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.Matrix;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.reflect.Array;
import java.util.Date;


public class MyActivity extends ActionBarActivity implements
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    DataMap dataMap;
    int testId = R.id.test;
    boolean run = false;
    ImageView drawingImageView;
    int[] currentPoint;
    long currentTime;
    float[] currentV;
    float timeDiff;
    Canvas canvas;
    Bitmap bitmap;
    Paint paint1;
    Paint paint2;
    Paint paint3;
    Paint paint;
    int c;
    private float[] ab;
    private boolean[] pos;
    private boolean[] neg;
    private int[] d;
    private long[] cd;
    float distance[];
    float init_vel[];
    float current_acc[];
    long [] startTime;
    private final long wait = 1000;
    private boolean accFilFlag;
    private boolean velFilFlag;
    private boolean absAcc1Flag;
    private boolean linAcc1Flag;
    private boolean vel1Flag;
    private boolean absAcc2Flag;
    private boolean linAcc2Flag;
    private boolean vel2Flag;
    private boolean lineFlag;
    private boolean onBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        resetDrawing();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v("myTag", "Mobile: Connected to Google Api Service");
        show("Connected to Google", testId);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        show("Connection is Suspended", testId);
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v("myTag", "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                //Log.v("myTag", "DataItem changed: " + dataMap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log(dataMap);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        show("Connection failed!", testId);
    }

    public void show(String s, int id){
        TextView t = (TextView) findViewById(id);
        t.setText(s);
    }
    public void log(DataMap dataMap){

        //Log.v("myTag", "Run: "+run+", "+dataMap.getBoolean("run")+" ct: "+currentTime+" td: "+timeDiff);

        long time = dataMap.getLong("time");

        if(run == false && dataMap.getBoolean("run") == true){
            resetDrawing();
        }

        run = dataMap.getBoolean("run");

        show("Run: "+run, R.id.act);

        if(run == true){

            timeDiff = (time - currentTime)/1000f ;

            Log.v("count","---------------------------");

            if(timeDiff < 0.2 && timeDiff > 0){ //Reliable time diff

                int[] nextPoint = new int[3];
                float[] accV = dataMap.getFloatArray("acc");
                float[] acc = new float[4];
                float[] rotV = dataMap.getFloatArray("rot");
                float[] accAbs = new float[4];
                float[] rot = new float[16];
                int[] dir;
                long [] chDir;
                long [] filChDir;

                acc[0] = accV[0];
                acc[1] = accV[1];
                acc[2] = accV[2];
                acc[3] = 0;

                Matrix.invertM(rot, 0, rotV, 0);
                Matrix.multiplyMV(accAbs, 0, rot, 0, acc, 0);



                for(int i=0;i<3;i++){
                    //Log.v("time", i+ "=> "+"time: "+time+" startTime: "+startTime[i]);
                    ab[i] = accAbs[i];
                    if(accFilFlag) {
                        if (time - startTime[i] > wait) {//pass the limited time!!!!
                            if (Math.abs(accAbs[i]) > 1) {
                                startTime[i] = time;
                                if (accAbs[i] > 0) {
                                    d[i] = 1;
                                } else {
                                    d[i] = -1;
                                }
                            }
                        } else {//in the limited time!!!!
                            if ((d[i] == 1 && accAbs[i] <= 0) || (d[i] == -1 && accAbs[i] >= 0)) {
                                ab[i] = 0;
                            }
                        }
                    }
                    if(velFilFlag) {
                        if (c % 10 == 0) {
                            init_vel[i] = 0;
                        }
                    }
                    float vel = init_vel[i] + (ab[i] * timeDiff);
                    init_vel[i] = vel;
                    nextPoint[i] = currentPoint[i] + (int) Math.round((vel * timeDiff + 0.5 * ab[i] * timeDiff * timeDiff) * 1000f);
                }
                Log.v("z", "Current: "+currentPoint[2]+" Next: "+nextPoint[2]);
                if(d[2] == 1){
                    onBoard = false;
                }else if(d[2] == -1){
                    onBoard = true;
                }
               show("On Board: "+onBoard, R.id.onBoard);
                if(absAcc1Flag)
                    updateCanvas(ab, 1);
                if(linAcc1Flag)
                    updateCanvas(acc, 1);
                if(vel1Flag)
                    updateCanvas(init_vel, 1);
                if(absAcc2Flag)
                    updateCanvas(ab, 4);
                if(linAcc2Flag)
                    updateCanvas(acc, 4);
                if(vel2Flag)
                    updateCanvas(init_vel, 4);
                if(lineFlag)
                    canvas.drawLine(currentPoint[0], currentPoint[1], nextPoint[0], nextPoint[1], paint);

                drawingImageView.setImageBitmap(bitmap);

                for(int i=0;i<3;i++){
                    ab[i] = accAbs[i];
                    currentPoint[i] = nextPoint[i];
                }

            }
            currentTime = time;

        }
    }

    public void resetDrawing(){
        Log.v("myTag", "Reset");
        currentPoint = new int[3];
        currentPoint[0] = 1000;
        currentPoint[1] = 1000;
        currentPoint[2] = 0;
        currentTime = 0;
        current_acc = new float[3];
        init_vel = new float[3];
        timeDiff = 0;
        c = 0;
        pos = new boolean[3];
        neg = new boolean[3];
        ab = new float[3];
        d = new int[3];
        cd = new long[3];
        startTime = new long[3];

        drawingImageView = (ImageView) this.findViewById(R.id.draw);
        bitmap = Bitmap.createBitmap(2000,2000,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        drawingImageView.setImageBitmap(bitmap);

        // Line
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6);

        // Line
        paint1 = new Paint();
        paint1.setColor(Color.RED);
        paint1.setStrokeWidth(6);

        // Line
        paint2 = new Paint();
        paint2.setColor(Color.argb(255,0,170,0));
        paint2.setStrokeWidth(6);

        // Line
        paint3 = new Paint();
        paint3.setColor(Color.BLUE);
        paint3.setStrokeWidth(6);
    }
    public void updateCanvas(float[] first,int num){
        c++;
        c++;
        if(c > 2000)
            resetDrawing();
        canvas.drawPoint(c, (int) Math.round(first[0] * 10) + (num*300), paint1);
        canvas.drawPoint(c, (int) Math.round(first[1] * 10) + (num*300+300), paint2);
        canvas.drawPoint(c, (int) Math.round(first[2] * 10) + (num*300+600), paint3);
    }
    public void setView(View v){
        accFilFlag = false;
        velFilFlag = false;
        lineFlag = false;
        absAcc1Flag = false;
        linAcc1Flag = false;
        vel1Flag = false;
        absAcc2Flag = false;
        linAcc2Flag = false;
        vel2Flag = false;
        CheckBox accFil = (CheckBox) findViewById(R.id.accFil);
        CheckBox velFil = (CheckBox) findViewById(R.id.velFil);
        CheckBox line = (CheckBox) findViewById(R.id.line);
        RadioGroup g1 = (RadioGroup) findViewById(R.id.g1);
        RadioGroup g2 = (RadioGroup) findViewById(R.id.g2);
        int g1Checked = g1.getCheckedRadioButtonId();
        int g2Checked = g2.getCheckedRadioButtonId();
        switch(g1Checked){
            case R.id.absAcc1:
                absAcc1Flag = true;
                break;
            case R.id.linAcc1:
                linAcc1Flag = true;
                break;
            case R.id.velocity1:
                vel1Flag = true;
                break;
        }
        switch(g2Checked){
            case R.id.absAcc2:
                absAcc2Flag = true;
                break;
            case R.id.linAcc2:
                linAcc2Flag = true;
                break;
            case R.id.velocity2:
                vel2Flag = true;
                break;
        }
        if(accFil.isChecked())
            accFilFlag = true;
        if(velFil.isChecked())
            velFilFlag = true;
        if(line.isChecked())
            lineFlag = true;
        resetDrawing();
    }
}