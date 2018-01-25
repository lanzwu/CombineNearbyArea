package com.zxy.combinearea;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    Canvas canvas;
    Bitmap picture;
    private int areaWidth = 1200;
    private int areaHeight = 825;

    private Rect[] rects = {
            new Rect(100, 100, 200, 200),
            new Rect(100, 300, 200, 400),
            new Rect(150, 450, 300, 700),
            new Rect(200, 200, 300, 300),
            new Rect(400, 600, 500, 700),
            new Rect(750, 200, 800, 600),
            new Rect(650, 350, 700, 450),
            new Rect(900, 550, 1000, 800),
            new Rect(1000, 0, 1200, 200)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newCanvas();

        Paint rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStrokeWidth(2);
        rectPaint.setColor(Color.GRAY);

        for(Rect rect : rects){
            canvas.drawRect(rect,rectPaint);
        }

        Paint framePaint = new Paint();
        framePaint.setAntiAlias(true);
        framePaint.setStrokeWidth(2);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setColor(Color.BLACK);

        canvas.drawRect(new Rect(0,0, areaWidth, areaHeight),framePaint);

        Combine combine = new Combine(rects, areaWidth, areaHeight,false);

        for(Rect rect : combine.getResult()){
            //canvas.drawRect(rect,rectPaint);
            canvas.drawRect(rect,framePaint);
        }

        ImageView imageView = findViewById(R.id.img);
        imageView.setImageBitmap(picture);
    }

    public void newCanvas() {
        picture = Bitmap.createBitmap(areaWidth, areaHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(picture);
    }
}
