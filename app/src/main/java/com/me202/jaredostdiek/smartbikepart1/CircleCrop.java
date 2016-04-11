package com.me202.jaredostdiek.smartbikepart1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * Created by jaredostdiek on 4/9/16.
 */

//adapted from https://gist.github.com/julianshen/5829333
public class CircleCrop implements Transformation {
    @Override
    public Bitmap transform(Bitmap riderImage) {
        int size = Math.min(riderImage.getWidth(), riderImage.getHeight());

        int x = (riderImage.getWidth() - size)/2;
        int y = (riderImage.getHeight() - size)/2;

        //turn original image into a sqaure
        Bitmap squaredBitmap = Bitmap.createBitmap(riderImage, x, y, size, size);
        if(squaredBitmap != riderImage){
            //get rid of original image
            riderImage.recycle();
        }

        //create matrix for scaling image
        Matrix matrix = new Matrix();
        //resize the riderimage bitmap
        Float circleSize = 400f;
        RectF imageRect = new RectF(0,0,squaredBitmap.getWidth(),squaredBitmap.getHeight());
        RectF scaleRect = new RectF(0,0,circleSize,circleSize);
        matrix.setRectToRect(imageRect,scaleRect, Matrix.ScaleToFit.CENTER);

        //create new scaled bitmap
        Bitmap squaredBitmapScaled = Bitmap.createBitmap(
                squaredBitmap, 0, 0, size, size, matrix, false);

        //get rid of old bitmap
        squaredBitmap.recycle();

        //create bitmap to mask with circle
        Bitmap bitmap = Bitmap.createBitmap(squaredBitmapScaled.getWidth(),squaredBitmapScaled.getHeight(), riderImage.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmapScaled, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        //set circle radius and mask
        float radius = circleSize/2f;
        canvas.drawCircle(radius, radius, radius, paint);

        squaredBitmapScaled.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}