package com.example.myapplicationdrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class DrawCanvas extends Activity {
    /** Called when the activity is first created. */
    LinearLayout linearLayout2;
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Path> paths = new ArrayList<Path>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_main);
        linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
        final DrawingPanel dp = new DrawingPanel(this);
        linearLayout2.addView(dp);
        ((Button) findViewById(R.id.button1))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (paths.size() > 0) {
                            undonePaths.add(paths
                                    .remove(paths.size() - 1));
                            dp.invalidate();
                        }
                    }
                });
        ((Button) findViewById(R.id.button2))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (undonePaths.size()>0) {
                            paths.add(undonePaths.remove(undonePaths.size()-1));
                            dp.invalidate();
                        }
                    }
                });
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
            mPaint.setColor(0xFFFFFFFF);
            outercirclePaint.setColor(0x44FFFFFF);
            circlePaint.setColor(0xAADD5522);
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
}
