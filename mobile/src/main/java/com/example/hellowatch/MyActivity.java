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
import jxl.*;
import jxl.write.*;
import jxl.write.Number;



public class MyActivity extends ActionBarActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private DataMap dataMap;
    private int testId = R.id.test;
    boolean run = false;
    boolean fileOpen = false;
    private ImageView drawingImageView;
    private Canvas canvas;
    private Bitmap bitmap;
    private Paint paint;
    private Paint paint1;
    private Paint paint2;
    private Paint paint3;
    private Paint paint4;
    private int c;
    private int dataCounter;
    private WritableWorkbook workbook;
    private WritableSheet worksheet;
    private EditText letter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        try {
            workbook = Workbook.createWorkbook(new File(this.getBaseContext().getFilesDir(), "output.xls"));
            fileOpen = true;
            worksheet = workbook.createSheet("First Sheet", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        show("Connection failed!", testId);
    }

    public void show(String s, int id){
        TextView t = (TextView) findViewById(id);
        t.setText(s);
    }

    public void log(DataMap dataMap){
        long time = dataMap.getLong("time");

        run = dataMap.getBoolean("run");

        //show("Run: "+run, R.id.act);
        dataCounter++;

        if(run == true){

            float[] accRotated = new float[4];
            float[] accNotRotated = new float[4];
            float[] acc = dataMap.getFloatArray("acc");
            float[] orgAcc = dataMap.getFloatArray("orgAcc");
            double angle= dataMap.getDouble("angle");
            double angle1= dataMap.getDouble("angle1");
            double angle2= dataMap.getDouble("angle2");
            float[] RotationM = {   (float)Math.cos(angle) , (float)Math.sin(angle), 0, 0,
                                   -(float)Math.sin(angle) , (float)Math.cos(angle), 0, 0,
                                    0                      , 0                     , 1, 0,
                                    0                      , 0                     , 0, 0  };

            System.arraycopy(acc,0,accNotRotated,0,3);
            accNotRotated[3] = 0;

            Matrix.multiplyMV(accRotated,0, RotationM,0, accNotRotated,0);

            show("Angle: "+ (int) Math.round(Math.toDegrees(angle )), testId);
            show("Angle: "+ (int) Math.round(Math.toDegrees(angle1)), R.id.act);
            show("Angle: "+ (int) Math.round(Math.toDegrees(angle2)), R.id.onBoard);

            for(int i=0;i<3;i++){

                Number t = new Number(0, dataCounter, time);
                Number b = new Number(i+2, dataCounter, acc[i]);
                Number c = new Number(i+6, dataCounter, accRotated[i]);
                Label l = new Label(10, dataCounter, letter.getText().toString());

                try {

                    worksheet.addCell(t);
                    worksheet.addCell(b);
                    worksheet.addCell(c);
                    worksheet.addCell(l);

                } catch (WriteException e) {
                    e.printStackTrace();
                }

            }

            updateCanvas(acc, 1);
            updateCanvas(accRotated, 4);

            Log.v("Update canvas","pass");
            drawingImageView.setImageBitmap(bitmap);

            Log.v("draw image","pass");
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
    public void updateCanvas(float[] data, int num){

        c+=2;
        if(c > 2000)
            resetDrawing();

        canvas.drawPoint(c, Math.round(data[0] * 10) + (num*300), paint1);
        canvas.drawPoint(c, Math.round(data[1] * 10) + (num*300+300), paint2);
        canvas.drawPoint(c, Math.round(data[2] * 10) + (num*300+600), paint3);
    }
    public void drawVerticalLine(){
        canvas.drawLine(c,0,c,2000,paint4);
        c+=3;
    }
    public void setView(View v){

        try {
            workbook = Workbook.createWorkbook(new File(this.getBaseContext().getFilesDir(), "output.xls"));
            fileOpen = true;
            worksheet = workbook.createSheet("First Sheet", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        resetDrawing();
    }

    public void saveExcel(View view) {
        dataCounter = 0;
        if (fileOpen == true) {
            try {
                workbook.write();
                workbook.close();
                fileOpen = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
    }

}