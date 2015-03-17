package com.example.hellowatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.IOException;
import java.lang.Boolean;
import java.util.Date;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;


public class MyActivity extends ActionBarActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private DataMap dataMap;
    boolean run = false;
    private ImageView drawingImageView;
    private Canvas canvas;
    private Bitmap bitmap;
    private Paint paint1;
    private Paint paint2;
    private Paint paint3;
    private Paint paint;
    private int c;
    private Paint paint4;
    private int dataCounter;
    private WritableWorkbook workbook;
    private WritableSheet worksheet;
    private EditText letter;
    private boolean touchFlag;
    private boolean fileOpen;
    Button excelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        fileOpen = false;
        touchFlag =false;
        excelButton = (Button) findViewById(R.id.excel);
        excelButton.setText("Open Excel");
        letter = (EditText)findViewById(R.id.letter);
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
        show("Connected to Google", R.id.test1);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        show("Connection is Suspended", R.id.test1);
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
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                //Log.v("data", dataMap.getBoolean("run")+"");
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
        show("Connection failed!", R.id.test1);
    }

    public void log(DataMap dataMap){
        long time = dataMap.getLong("time");
        run = dataMap.getBoolean("run");

        show("Run: "+run, R.id.test2);

        dataCounter++;

        if(run == true){

            float[] acc = dataMap.getFloatArray("acc");
            float[] gyro = dataMap.getFloatArray("gyro");

            show("Is touching: "+touchFlag, R.id.test3);

            if(fileOpen) {
                for (int i = 0; i < 3; i++) {
                    Number t = new Number(0, dataCounter, time);
                    Number a = new Number(i + 2, dataCounter, gyro[i]);
                    Number b = new Number(i + 6, dataCounter, acc[i]);
                    Label l1 = new Label(10, dataCounter, Boolean.toString(touchFlag));
                    Label l2 = new Label(12, dataCounter, letter.getText().toString());

                    try {
                        worksheet.addCell(t);
                        worksheet.addCell(a);
                        worksheet.addCell(b);
                        worksheet.addCell(l1);
                        worksheet.addCell(l2);
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }
            }

            updateCanvas(gyro, 1);
            updateCanvas(acc, 4);

            drawingImageView.setImageBitmap(bitmap);

        }else{//End of the session
            drawVerticalLine();
        }
    }

    public void resetDrawing(){
        c = 0;

        drawingImageView = (ImageView) this.findViewById(R.id.draw);
        bitmap = Bitmap.createBitmap(2000,2000,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        drawingImageView.setImageBitmap(bitmap);

        // Line
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);

        // Line
        paint1 = new Paint();
        paint1.setColor(Color.RED);
        paint1.setStrokeWidth(5);

        // Line
        paint2 = new Paint();
        paint2.setColor(Color.argb(255,0,170,0));
        paint2.setStrokeWidth(5);

        // Line
        paint3 = new Paint();
        paint3.setColor(Color.BLUE);
        paint3.setStrokeWidth(5);

        // Line
        paint4 = new Paint();
        paint4.setColor(Color.BLACK);
        paint4.setStrokeWidth(2);


        canvas.drawLine(0,300,2000,300,paint4);
        canvas.drawLine(0,600,2000,600,paint4);
        canvas.drawLine(0,900,2000,900,paint4);
        canvas.drawLine(0,1200,2000,1200,paint4);
        canvas.drawLine(0,1500,2000,1500,paint4);
        canvas.drawLine(0,1800,2000,1800,paint4);
    }
    public void updateCanvas(float[] first,int num){
        c+=2;
        if(c > 2000)
            resetDrawing();
        canvas.drawPoint(c, Math.round(first[0] * 10) + (num*300)    , paint1);
        canvas.drawPoint(c, Math.round(first[1] * 10) + (num*300+300), paint2);
        canvas.drawPoint(c, Math.round(first[2] * 10) + (num*300+600), paint3);
    }
    public void drawVerticalLine(){
        canvas.drawLine(c,0,c,2000,paint4);
        c+=3;
    }
    public void setView(View v){
        resetDrawing();
    }

    public void show(String s, int id){
        TextView t = (TextView) findViewById(id);
        t.setText(s);
    }

    public boolean onTouchEvent(MotionEvent event){
        int action = MotionEventCompat.getActionMasked(event);

        switch(action){
            case (MotionEvent.ACTION_DOWN):
                touchFlag = true;
                break;

            case (MotionEvent.ACTION_UP):
                touchFlag = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    public void saveExcel(View view) {
        dataCounter = 0;
        fileOpen = !fileOpen;
        excelButton.setText(fileOpen?"Save Excel":"Open Excel");
        if(!fileOpen) {
            try {
                workbook.write();
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }else{
            Date date = new Date();
            try {
                workbook = Workbook.createWorkbook(new File(this.getBaseContext().getFilesDir(), date.getTime()+".xls"));
                worksheet = workbook.createSheet("First Sheet", 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}