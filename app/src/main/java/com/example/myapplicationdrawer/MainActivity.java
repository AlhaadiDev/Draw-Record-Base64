package com.example.myapplicationdrawer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnLoadImage, btnSaveImage;
    ImageView imageResult;
    //    PhotoView imageResult;
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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadImage = (Button) findViewById(R.id.loadimage);
        btnSaveImage = (Button) findViewById(R.id.saveimage);
        imageResult = (ImageView) findViewById(R.id.result);

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

//        btnLoadImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                Intent intent = new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, RQS_IMAGE1);
//            }
//        });

        findViewById(R.id.clearImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasMaster = new Canvas(bitmapMaster);
                canvasMaster.drawBitmap(tempBitmap, 0, 0, null);
                imageResult.setImageBitmap(bitmapMaster);
            }
        });

        findViewById(R.id.undoimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmapMaster != null) {
                    saveBitmap(bitmapMaster);
                }
            }
        });


        ////////////////////////////////////////////////////////////////////////////////////////////
        bitmapMaster = BitmapFactory.decodeResource(getResources(), R.drawable.images);
        try {
            Uri uri = Uri.parse("android.resource://com.example.myapplicationdrawer/drawable/images");
            tempBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap.Config config;
        if (tempBitmap.getConfig() != null) {
            config = tempBitmap.getConfig();
        } else {
            config = Bitmap.Config.ARGB_8888;
        }
        bitmapMaster = Bitmap.createBitmap(
                tempBitmap.getWidth(),
                tempBitmap.getHeight(),
                config);
        canvasMaster = new Canvas(bitmapMaster);
        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);
        imageResult.setImageBitmap(bitmapMaster);
        ////////////////////////////////////////////////////////////////////////////////////////////

        imageResult.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        prvX = x;
                        prvY = y;
                        drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                        prvX = x;
                        prvY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                        break;
                }
                /*
                 * Return 'true' to indicate that the event have been consumed.
                 * If auto-generated 'false', your code can detect ACTION_DOWN only,
                 * cannot detect ACTION_MOVE and ACTION_UP.
                 */
                return true;
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

    /* Project position on ImageView to position on Bitmap draw on it */
    private void drawOnProjectedBitMap(ImageView iv, Bitmap bm, float x0, float y0, float x, float y) {
        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return;
        } else {
            float ratioWidth = (float) bm.getWidth() / (float) iv.getWidth();
            float ratioHeight = (float) bm.getHeight() / (float) iv.getHeight();

            canvasMaster.drawLine(
                    x0 * ratioWidth,
                    y0 * ratioHeight,
                    x * ratioWidth,
                    y * ratioHeight,
                    paintDraw);
            imageResult.invalidate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap tempBitmap;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RQS_IMAGE1:
                    source = data.getData();

                    try {
//                        tempBitmap is Immutable bitmap,
//                        cannot be passed to Canvas constructor
                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(source));

                        Bitmap.Config config;
                        if (tempBitmap.getConfig() != null) {
                            config = tempBitmap.getConfig();
                        } else {
                            config = Bitmap.Config.ARGB_8888;
                        }

                        //bitmapMaster is Mutable bitmap
                        bitmapMaster = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);

                        canvasMaster = new Canvas(bitmapMaster);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

                        imageResult.setImageBitmap(bitmapMaster);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    break;
            }
        }
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
            Toast.makeText(MainActivity.this,
                    "Save Bitmap: " + fileOutputStream.toString(),
                    Toast.LENGTH_LONG).show();
            Log.d("urlStoreImages", "Url: " + fileOutputStream.toString() + "  :path: " + pathFile);
            bitmapToBase64String(pathFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,
                    "Something wrong: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,
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
        Log.d("urlStoreImages", "data:image/png;base64," + b64);

        return "data:image/png;base64," + b64;
    }


}
