package com.physicaloid.mcui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.Physicaloid.UploadCallBack;
import com.physicaloid.lib.programmer.avr.UploadErrors;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import static com.physicaloid.mcui.R.id.imageView;

public class MCUIActivity extends Activity {


    private static final String TAG = MCUIActivity.class.getSimpleName();
    /*
     * !!! You need to import PhysicaloidLibrary. !!!
     * If you have errors, check Project -> Properties -> Android -> Library.
     */

    /*
     * In this tutorial, You can learn
     *  - how to use read/upload callbacks
     *  
     *  You might check TODO tags.
     */

    Button btOpen, btClose, btWrite, btUpload;
    EditText etWrite;
    TextView tvRead;
    ImageView imageView;
    FloatPoint bt0, bt1, bt2, bt3;


    private static Context context;

    Physicaloid mPhysicaloid;
    Bitmap scaledBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcui);
        MCUIActivity.context = getApplicationContext();

        btOpen  = (Button) findViewById(R.id.btOpen);
        btClose = (Button) findViewById(R.id.btClose);
       // btWrite = (Button) findViewById(R.id.btWrite);
        btUpload= (Button) findViewById(R.id.btUpload);
     //   etWrite = (EditText) findViewById(R.id.etWrite);
        tvRead  = (TextView) findViewById(R.id.tvRead);
        imageView  = (ImageView) findViewById(R.id.imageView);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float w = displayMetrics.widthPixels;
        float h = w;

        setEnabledUi(false);
        mPhysicaloid = new Physicaloid(this);



        // paint gesturepas on screen
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bitmapOrg = Bitmap.createBitmap((int)w,(int) h, conf); // this creates a MUTABLE bitmap

        Canvas canvas = new Canvas(bitmapOrg);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvas.setBitmap(bitmapOrg);

        bt0 = new FloatPoint(w*.25f, h*.25f);
        bt1 = new FloatPoint(w*.75f, h*.25f);
        bt2 = new FloatPoint(w*.75f, h*.75f);
        bt3 = new FloatPoint(w*.25f, h*.75f);


        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.BLUE);
        rectPaint.setStrokeWidth(10);
        rectPaint.setStyle(Paint.Style.STROKE);

        // bt0
        canvas.drawRect(w*.0625f, w*.0625f, w * .4375f , w * .4375f, rectPaint);
        // bt1
        canvas.drawRect(w*.5625f, w*.0625f, w * .9375f , w * .4375f, rectPaint);
        // bt2
        canvas.drawRect(w*.5625f, w*.5625f, w * .9375f , w * .9375f, rectPaint);
        // bt
        canvas.drawRect(w*.0625f, w*.5625f, w * .4375f , w * .9375f, rectPaint);


        scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg,(int)w,(int)h,true);
        Bitmap rotatedBitmap = rotateImg(scaledBitmap , 45 );
        imageView.setImageBitmap(rotatedBitmap);

}

    /**
     * Our representation of a coordinate on a bitmap
     */
    private class FloatPoint {
        private float x;
        private float y;

        public FloatPoint(float x, float y){
            this.x = x;
            this.y = y;

        }
        public float getx(){
            return this.x;
        }
        public float gety(){
            return this.y;
        }

    }

    /**
     * opens the com port and reads from arduino
     * @param v
     */
    public void onClickOpen(View v) {
        if(mPhysicaloid.open()) { // default 9600bps
            setEnabledUi(true);

            //****************************************************************
            // TODO : add read callback
            mPhysicaloid.addReadListener(new ReadLisener() {
                String readStr;

                // callback when reading one or more size buffer
                @Override
                public void onRead(int size) {
                    byte[] buf = new byte[size];

                    mPhysicaloid.read(buf, size);
                    try {
                        readStr = new String(buf, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, e.toString());
                        return;
                    }
                    // UI thread
                    tvAppend(tvRead, readStr);
                    // draws gesture on screen
                    drawGesture(readStr);
                }
            });
            //****************************************************************

        }
    }

    /**
     * Draws a gesture on the scaledBitmap
     * @param readStr should be a string with intergers seperated by blankspace
     */
    public void drawGesture(String readStr){

        String[] exploded= readStr.split(" ");
        Boolean first = true;
        String prev = "";
        Bitmap newBmp = Bitmap.createBitmap(scaledBitmap);

        for (String chr: exploded) {
            if (chr.equals("9\n")) {
                // full touch
                newBmp = drawLine(newBmp, "0", "1");
                newBmp = drawLine(newBmp, "1", "2");
                newBmp = drawLine(newBmp, "2", "3");
                newBmp = drawLine(newBmp, "3", "0");

            }else if(chr.equals("\n")){
                // removes newlines
            }
            else {
                if (first) {
                    // first input
                    prev = chr;
                    first = false;
                } else {
                    // rest of input
                    newBmp = drawLine(newBmp, prev, chr);
                    prev = chr;
                }
            }
        }
        // rotate and set bitmap
        Bitmap rotatedBitmap = rotateImg(newBmp, 45);

        imageupdate(imageView,  rotatedBitmap);
    }


    /**
     * rotates a btmap
     * @param bmp the bitmap you want rotated
     * @param degrees how many degrees the bitmap should be rotated
     * @return the rotated bitmap
     */
    private Bitmap rotateImg(Bitmap bmp, int degrees ){
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bmp , 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * called when user clicks close. Closes the reading from comport(arduino)
     * @param v
     */
    public void onClickClose(View v) {
        if(mPhysicaloid.close()) {
            setEnabledUi(false);
            //****************************************************************
            // TODO : clear read callback
            mPhysicaloid.clearReadListener();
            //****************************************************************
        }
    }

 //   public void onClickWrite(View v) {
 //       String str = etWrite.getText().toString();
 //       if(str.length()>0) {
 //           byte[] buf = str.getBytes();
 //           mPhysicaloid.write(buf, buf.length);
 //       }
 //   }

    /**
     * called when user clicks clear. Clears the output textview.
     * @param v
     */
    public void onClickClear(View v) {
        drawGesture("0 1 2 3 0");
        tvRead.setText("");
    }

    /**
     * Draws a line on a bitmap from one point to another
     * @param bmp the bitmap to be drawn on
     * @param bt_s source point
     * @param bt_d destination poinr
     * @return returns the new bitmap with the line on
     */
    private Bitmap drawLine(Bitmap bmp , String bt_s, String bt_d ){
        FloatPoint bt_s_point =  stringToFloatpoint(bt_s);
        FloatPoint bt_d_point =  stringToFloatpoint(bt_d);

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(7f);
        canvas.setBitmap(bmp);

        canvas.drawLine(bt_s_point.getx(), bt_s_point.gety(), bt_d_point.getx(), bt_d_point.gety(), paint);
        return bmp;
    }

    /**
     * converts a string char to a FloatPoint( a (x,y) coordinate)
     * @param str should be the number of one of our input buttons 0-3
     * @return the corresponding buttons coordinates
     */
    private FloatPoint stringToFloatpoint(String str){
        switch (Integer.parseInt(str)){
            case 0:
                return bt0;
            case 1:
                return bt1;
            case 2:
                return bt2;
            case 3:
                return bt3;
            default:
                return bt0;
        }
    }


    //****************************************************************
    // TODO : create upload callback
    // normal process:
    // onPreUpload -> onUploading -> onPostUpload
    //
    // cancel:
    // onPreUpload -> onUploading -> onCancel -> onPostUpload
    //
    // error:
    // onPreUpload  |
    // onUploading  | -> onError
    // onPostUpload |
    /**
     * uploads program from android phone to arduino
     */
    private UploadCallBack mUploadCallback = new UploadCallBack() {
        @Override
        public void onPreUpload() {
            tvAppend(tvRead, "Upload : Start\n");
        }

        @Override
        public void onUploading(int value) {
            tvAppend(tvRead, "Upload : "+value+" %\n");
        }

        @Override
        public void onPostUpload(boolean success) {
            if(success) {
                tvAppend(tvRead, "Upload : Successful\n");
            } else {
                tvAppend(tvRead, "Upload fail\n");
            }
        }

        @Override
        public void onCancel() {
            tvAppend(tvRead, "Cancel uploading\n");
        }

        @Override
        public void onError(UploadErrors err) {
            tvAppend(tvRead, "Error  : "+err.toString()+"\n");
        }
    };
    //****************************************************************

    public void onClickUpload(View v) {
        try {
            //****************************************************************
            // TODO : add upload callback
            mPhysicaloid.upload(Boards.POCKETDUINO, getResources().getAssets().open("SerialEchoback.PocketDuino.hex"), mUploadCallback);
            //****************************************************************
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

    }

    Handler mHandler = new Handler();
    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }


    Handler m1Handler = new Handler();
    private void imageupdate(ImageView iv, Bitmap bitmap) {
        final ImageView fiv = iv;
        final Bitmap fbitmap = bitmap;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fiv.setImageBitmap(fbitmap);
            }
        });
    }

    private void setEnabledUi(boolean on) {
        if(on) {
            btOpen.setEnabled(false);
            btClose.setEnabled(true);
            btUpload.setEnabled(true);
            tvRead.setEnabled(true);
        } else {
            btOpen.setEnabled(true);
            btClose.setEnabled(false);
            btUpload.setEnabled(true);
            tvRead.setEnabled(false);
        }
    }


}
