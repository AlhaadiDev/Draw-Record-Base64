package com.example.myapplicationdrawer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CanvasImageUndo extends AppCompatActivity {
    public ArrayList<FormInfo> formArrayList = new ArrayList<FormInfo>();

    public static CanvasImageUndo canvasImageUndo;
    Button btnLoadImage, btnSaveImage;
    LinearLayout imageResult;
    Context context;

    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;

    final int RQS_IMAGE1 = 1;
    int RQS_RECORDING = 1;
    int prvX, prvY;
    Paint paintDraw;
    Bitmap tempBitmap = null;
    boolean isZoom = true;
    View view;

    public static String imageBase64;
    public static String audioBase64;

    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Path> paths = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_undo_image);

        canvasImageUndo = this;
        btnLoadImage = (Button) findViewById(R.id.loadimage);
        btnSaveImage = (Button) findViewById(R.id.saveimage);
        imageResult = findViewById(R.id.result);
        view = imageResult;
        final DrawingPanel dp = new DrawingPanel(this);
        imageResult.addView(dp);

        canvasMaster = new Canvas();
        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.RED);
        paintDraw.setStrokeWidth(2);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paths.size() > 0) {
                    undonePaths.add(paths
                            .remove(paths.size() - 1));
                    dp.invalidate();
                }
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (undonePaths.size() > 0) {
                    paths.add(undonePaths.remove(undonePaths.size() - 1));
                    dp.invalidate();
                }
            }
        });


        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawingPanel dp2 = new DrawingPanel(CanvasImageUndo.this);
                imageResult.addView(dp2);
                if (view != null) {
                    view.setDrawingCacheEnabled(true);
                    view.buildDrawingCache();
                    bitmapMaster = view.getDrawingCache();
                    if (bitmapMaster != null) {
                        saveBitmap(bitmapMaster);
                    }
                }
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawingPanel dn = new DrawingPanel(CanvasImageUndo.this);
                imageResult.addView(dn);
                paths = new ArrayList<>();
                paths.clear();
                canvasMaster = new Canvas();
                dp.invalidate();

            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CanvasImageUndo.this, AudioOnTouchActivity.class));
            }
        });

        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!imageBase64.equalsIgnoreCase("") || !audioBase64.equalsIgnoreCase("")) {
                if (imageBase64 != null && audioBase64 != null) {
                    sendTheForm(imageBase64, audioBase64);
                } else {
                    Toast.makeText(CanvasImageUndo.this, "NoInfp Brooo", Toast.LENGTH_SHORT).show();
                }

                Log.d("ArrayBase64", "base64Image: " + imageBase64 + "base64Audio: " + audioBase64);

            }
        });

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveBitmap(Bitmap bm) {
        File file = Environment.getExternalStorageDirectory();
        File newFile = new File(file, "test.jpg");
        String pathFile = newFile.getAbsolutePath();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(CanvasImageUndo.this,
                    "Save Bitmap: " + fileOutputStream.toString(),
                    Toast.LENGTH_LONG).show();
            imageBase64 = fileOutputStream.toString();
            Log.d("urlStoreImages", "Url: " + fileOutputStream.toString() + "  :path: " + pathFile);
            bitmapToBase64String(pathFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(CanvasImageUndo.this,
                    "Something wrong: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(CanvasImageUndo.this,
                    "Something wrong: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String bitmapToBase64String(String filePath) {
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        bm.recycle();
        bm = null;
        byte[] b = baos.toByteArray();
        String b64 = Base64.encodeToString(b, Base64.DEFAULT);
        Log.e("base64 ", b64);
//        Log.d("urlStoreImages", "data:image/png;base64," + b64);

//        imageBase64 = b64;


        final int chunkSize = 1000;
        for (int i = 0; i < b64.length(); i += chunkSize) {
            Log.d("Base64Image: ", b64.substring(i, Math.min(b64.length(), i + chunkSize)));
        }

        return "data:image/png;base64," + b64;
    }

    public class DrawingPanel extends View implements View.OnTouchListener {

        private Canvas mCanvas;
        private Path mPath;
        private Paint mPaint, circlePaint, outercirclePaint;

        // private ArrayList<Path> undonePaths = new ArrayList<Path>();
        private float xleft, xright, xtop, xbottom;

        public DrawingPanel(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            this.setOnTouchListener(this);

            circlePaint = new Paint();
            mPaint = new Paint();
            outercirclePaint = new Paint();
            outercirclePaint.setAntiAlias(true);
            circlePaint.setAntiAlias(true);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.RED);
            outercirclePaint.setColor(Color.RED);
            circlePaint.setColor(Color.RED);
            outercirclePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStyle(Paint.Style.FILL);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(6);
            outercirclePaint.setStrokeWidth(6);
            mCanvas = new Canvas();
            mPath = new Path();
            paths.add(mPath);
        }

        public void colorChanged(int color) {
            mPaint.setColor(color);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            for (Path p : paths) {
                canvas.drawPath(p, mPaint);
            }

        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 0;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath = new Path();
            paths.add(mPath);
        }

        @Override
        public boolean onTouch(View arg0, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // if (x <= cx+circleRadius+5 && x>= cx-circleRadius-5) {
                    // if (y<= cy+circleRadius+5 && cy>= cy-circleRadius-5){
                    // paths.clear();
                    // return true;
                    // }
                    // }
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }


//    -----------------------------SENDING FORM--------------------------------------------------------

    public void sendTheForm(String imageBase64, String audioBase64) {
        boolean isInternet = true;
        if (isInternet) {
            sendUserFormToServer();
            if (formArrayList.size()>0){
                sendPendingUserFormToServer();
            }else{
                formArrayList.clear();
            }
        } else {
            sendStoreUserFormToArray(imageBase64,audioBase64);
        }
    }

    public void sendUserFormToServer() {
        Toast.makeText(CanvasImageUndo.this, "Send UserFrm to Server normally", Toast.LENGTH_SHORT).show();
    }

    public void sendStoreUserFormToArray(String imageBase64, String audioBase64) {
        FormInfo formInfo = new FormInfo();
        formInfo.imageBase64 = imageBase64;
        formInfo.audioBase64 = audioBase64;
        formArrayList.add(formInfo);

        Log.d("checkArray", "ChevckArry: " + formArrayList.size());
    }

    public void sendPendingUserFormToServer(){
        for (int y=0;y<formArrayList.size();y++){
            sendUserFormToServer();
        }
    }

}
