package com.example.hellowatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.Matrix;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
    private int excelRowCounter;
    private Button excelButton;
    private WritableWorkbook workbook;
    private WritableSheet worksheet;
    private EditText letter;
    private ArrayList<Character> xSequence;
    private ArrayList<Character> ySequence;
    private ArrayList<Character> zSequence;
    private TextView guessView;
    private Signals SIGNALS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        excelButton = (Button) findViewById(R.id.excel_button);
        guessView = (TextView) findViewById(R.id.guess_view);

        letter = (EditText)findViewById(R.id.letter);
        resetDrawing();

        SIGNALS = new Signals();

        //try {
//            Field field = Signals.class.getField("ax");
//            field.set(SIGNALS, d);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }



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
        xSequence = new ArrayList<Character>();
        ySequence = new ArrayList<Character>();
        zSequence = new ArrayList<Character>();
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

        if(run == true){

            float[] accRotated = new float[4];
            float[] accNotRotated = new float[4];
            float[] acc = dataMap.getFloatArray("acc");
            float[] orgAcc = dataMap.getFloatArray("orgAcc");
            double angle= dataMap.getDouble("angle");
            double angle1= dataMap.getDouble("angle1");
            double angle2= dataMap.getDouble("angle2");
            boolean session= dataMap.getBoolean("session");
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

            if(fileOpen == true) {//Building the excel file for log peruses.
                excelRowCounter++;
                for (int i = 0; i < 3; i++) {

                    Number t = new Number(0, excelRowCounter, time);
                    Number b = new Number(i + 2, excelRowCounter, acc[i]);
                    Number c = new Number(i + 6, excelRowCounter, accRotated[i]);
                    Label l1 = new Label(10, excelRowCounter, letter.getText().toString());
                    Label l2 = new Label(12, excelRowCounter, session + "");

                    try {

                        worksheet.addCell(t);
                        worksheet.addCell(b);
                        worksheet.addCell(c);
                        worksheet.addCell(l1);
                        worksheet.addCell(l2);

                    } catch (WriteException e) {
                        e.printStackTrace();
                    }

                }
            }
            //Updating the view.
            updateCanvas(acc, 1);
            updateCanvas(accRotated, 4);
            drawingImageView.setImageBitmap(bitmap);

            //Building the sequence.
            xSequence.add(getCorrespondingChar(accRotated[0]));
            ySequence.add(getCorrespondingChar(accRotated[1]));
            zSequence.add(getCorrespondingChar(accRotated[2]));


        }else{//End of the session

            if(fileOpen == true) {//Building the excel file for log peruses.
                excelRowCounter++;
            }

            drawVerticalLine();//To separate session in the view.

            String guess = getClosestLetterWithHM();
            //String guess = getClosestLetterWithDTW();
            guessView.append(guess);

            //reset sequences for next letter
            xSequence = new ArrayList<Character>();
            ySequence = new ArrayList<Character>();
            zSequence = new ArrayList<Character>();
        }
    }

    private String readHmmFile(String fileName) {
        //Find the directory for the SD Card using the API
        File sdcard = Environment.getExternalStorageDirectory();

        //Get the text file
        File file = new File(sdcard, "hmm/"+fileName+".hmm");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Log.e("readHmmFile() Error",e.toString());
        }

        return text.toString();
    }

    private String ArrayListToString(ArrayList<Character> al) {
        String s = "";
        for(char i: al){
            s+=i;
        }
        return s;
    }

    private Character getCorrespondingChar(float v) {
        if(v>6){
            return 'a';
        }else
        if(v>5.5){
            return 'b';
        }else
        if(v>5){
            return 'c';
        }else
        if(v>4.5){
            return 'd';
        }else
        if(v>4){
            return 'e';
        }else
        if(v>3.5){
            return 'f';
        }else
        if(v>3){
            return 'g';
        }else
        if(v>2.5){
            return 'h';
        }else
        if(v>2){
            return 'i';
        }else
        if(v>1.5){
            return 'j';
        }else
        if(v>1){
            return 'k';
        }else
        if(v>0.5){
            return 'l';
        }else
        if(v>0){
            return 'm';
        }else
        if(v>-0.5){
            return 'n';
        }else
        if(v>-1){
            return 'o';
        }else
        if(v>-1.5){
            return 'p';
        }else
        if(v>-2){
            return 'q';
        }else
        if(v>-2.5){
            return 'r';
        }else
        if(v>-3){
            return 's';
        }else
        if(v>-3.5){
            return 't';
        }else
        if(v>-4){
            return 'u';
        }else
        if(v>-4.5){
            return 'v';
        }else
        if(v>-5){
            return 'w';
        }else
        if(v>-5.5){
            return 'x';
        }else
        if(v>-6){
            return 'y';
        }else{
            return 'z';
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
        canvas.drawLine(c, 0, c, 2000, paint4);
        c+=3;
    }
    public void setView(View v){
        guessView.setText("");
        resetDrawing();
    }

    public void excelManager(View view) {
        Date date = new Date();
        if (fileOpen == true) {
            try {
                workbook.write();
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
            excelButton.setText("Open Excel");
        }else{
            try {
                workbook = Workbook.createWorkbook(new File(this.getBaseContext().getFilesDir(), Long.toString(date.getTime())+".xls"));
                worksheet = workbook.createSheet("First Sheet", 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            excelRowCounter = 0;
            excelButton.setText("Save Excel");
        }
        fileOpen = !fileOpen;
    }

    public String getClosestLetterWithHM() {

        char[] alphabet = new char[]{'a','j','w','z','b'};//,'f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        String xString = ArrayListToString(xSequence);
        String yString = ArrayListToString(ySequence);
        String zString = ArrayListToString(zSequence);
        char closestLetter = '!';
        double min = Double.MAX_VALUE;

        for(char ch : alphabet){
            ForwardAlgo xFoAl = new ForwardAlgo(new HMM(readHmmFile(ch+"x"), false), xString);
            ForwardAlgo yFoAl = new ForwardAlgo(new HMM(readHmmFile(ch+"y"), false), yString);
            ForwardAlgo zFoAl = new ForwardAlgo(new HMM(readHmmFile(ch+"z"), false), zString);
            double res = Math.abs(xFoAl.getLogScore(0)+yFoAl.getLogScore(0) + zFoAl.getLogScore(0));
            if (res<min) {
                min = res;
                closestLetter = ch;
            }
        }

        return Character.toString(closestLetter);
    }

    public String getClosestLetterWithDTW() {

        char[] alphabet = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        String xString = ArrayListToString(xSequence);
        String yString = ArrayListToString(ySequence);
        String zString = ArrayListToString(zSequence);
        char closestLetter = '!';
        double min = Double.MAX_VALUE;

        Log.v("x", xString);
        Log.v("y", yString);
        Log.v("z", zString);

        for(char ch : alphabet){
            ForwardAlgo xFoAl = new ForwardAlgo(new HMM(readHmmFile(ch+"x"), false), xString);
            ForwardAlgo yFoAl = new ForwardAlgo(new HMM(readHmmFile(ch+"y"), false), yString);
            ForwardAlgo zFoAl = new ForwardAlgo(new HMM(readHmmFile(ch+"z"), false), zString);
            double res = yFoAl.getLogScore(0) + zFoAl.getLogScore(0);
            if (res < min) {
                min = res;
                closestLetter = ch;
            }
        }

        return Character.toString(closestLetter);
    }
}