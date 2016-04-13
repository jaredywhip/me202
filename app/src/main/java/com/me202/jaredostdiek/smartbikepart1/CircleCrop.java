package com.me202.jaredostdiek.smartbikepart1;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * Created by jaredostdiek on 4/11/16.
 *File Description: Class to create a cicle mask
 * around a bitmap using Picasso.
 */

//adapted from https://gist.github.com/julianshen/5829333
public class CircleCrop implements Transformation {
    @Override
    public Bitmap transform(Bitmap riderImage) {

        //get smallest dimension from the bitmap
        int size = Math.min(riderImage.getWidth(), riderImage.getHeight());

        //set x,y center point
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
        Bitmap bitmap = Bitmap.createBitmap(squaredBitmapScaled.getWidth(),squaredBitmapScaled.getHeight(), squaredBitmapScaled.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmapScaled, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        //set circle radius and mask
        float radius = circleSize/2f;
        canvas.drawCircle(radius, radius, radius, paint);

        //get rid of unused bitmap
        squaredBitmapScaled.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}