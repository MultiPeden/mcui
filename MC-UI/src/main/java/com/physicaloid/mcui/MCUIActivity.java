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
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
    private static Context context;

    Physicaloid mPhysicaloid;

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

        setEnabledUi(false);

        mPhysicaloid = new Physicaloid(this);


/*
        Bitmap bitmap = Bitmap.createBitmap(
                600, // Width
                400, // Height
                Bitmap.Config.ARGB_8888 // Configuration
        );
*/

/*
        File sd = Environment.getExternalStorageDirectory();
        File image = new File(sd+"drawable", "ic_launcher.png");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        bitmap = Bitmap.createScaledBitmap(bitmap,50,50,true);
        imageView.setImageBitmap(bitmap);

*/

       // imageView.setImageResource(R.drawable.ic_action_touchpad);
/*
        Canvas canvas = new Canvas();

        Drawable d = getResources().getDrawable(R.drawable.ic_action_touchpad);
      //  d.setBounds(left, top, right, bottom);
        d.draw(canvas);
        imageView.setImageResource(canvas);
        imageView.set
    }*/


       // Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

       // Bitmap bitmap = Bitmap.createBitmap(R.drawable.ic_action_touchpad);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;

        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_action_touchpad,opt);




        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvas.setBitmap(bitmap);

        FloatPoint bt0 = new FloatPoint(42, 25);
        FloatPoint bt1 = new FloatPoint(58,43);
        FloatPoint bt2 = new FloatPoint(42,60);
        FloatPoint bt3 = new FloatPoint(26, 43);



        canvas.drawCircle(bt0.getx(), bt0.gety(), 3, paint);
        canvas.drawCircle(bt1.getx(), bt1.gety(), 3, paint);
        canvas.drawCircle(bt2.getx(), bt2.gety(), 3, paint);
        canvas.drawCircle(bt3.getx(), bt3.gety(), 3, paint);




//        canvas.drawLine(42,25,58,43, paint);
//        canvas.drawLine(58,43,42,60, paint);
//       canvas.drawLine(42,60,26,43, paint);

        imageView.setImageBitmap(bitmap);

}

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
                        Log.e(TAG,e.toString());
                        return;
                    }

                    // UI thread
                    tvAppend(tvRead, readStr);
                }
            });
            //****************************************************************

        }
    }

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

    public void onClickClear(View v) {
        tvRead.setText("");
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

    private void setEnabledUi(boolean on) {
        if(on) {
            btOpen.setEnabled(false);
            btClose.setEnabled(true);
         //   btWrite.setEnabled(true);
            btUpload.setEnabled(true);
       //     etWrite.setEnabled(true);
            tvRead.setEnabled(true);
        } else {
            btOpen.setEnabled(true);
            btClose.setEnabled(false);
      //      btWrite.setEnabled(false);
            btUpload.setEnabled(true);
       //     etWrite.setEnabled(false);
            tvRead.setEnabled(false);
        }
    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
