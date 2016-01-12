package com.bzsoft.childmodeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by s_Berezovikov on 25.11.2015.
 */
public class CustomSwipeAdapter extends PagerAdapter {

    private Context ctx;
    private ArrayList<String> _imageUrlList;
    private int _width;
    private int _height;

    public CustomSwipeAdapter(Context ctx, ArrayList<String> imageUrlList, int width, int height)
    {
        this.ctx = ctx;
        _imageUrlList = imageUrlList;
        _width = width;
        _height = height;
    }

    @Override
    public int getCount() {
        return _imageUrlList.size() + 2;
    }

    public int getRealCount() {
        return _imageUrlList.size();
    }

    int toRealPosition(int position) {

        int realCount = getRealCount();
        if (realCount == 0)
            return 0;

        int realPosition = (position-1) % realCount;
        if (realPosition < 0)
            realPosition += realCount;

        return realPosition;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        int realPosition = toRealPosition(position);
        LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        TextView textView = (TextView)item_view.findViewById(R.id.image_count);
        textView.setText("Image: " + position);



        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(_imageUrlList.get(realPosition), options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        String s = "Image: " + position + ". Size: " + imageWidth + "x" + imageHeight;

        int maxScreenSize = Math.max(_width, _height);
        int maxImageSize = Math.max(imageWidth, imageHeight);

        options.inJustDecodeBounds = false;
        int inSampleSize = maxImageSize / maxScreenSize;
        if (inSampleSize > 1)
            options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(_imageUrlList.get(realPosition), options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;

        if (inSampleSize > 1)
            s += ". inSampleSize: " +  inSampleSize + ", " + imageWidth + "x" + imageHeight;
        Log.d("TAG", s);

        ImageView imageView = (ImageView)item_view.findViewById(R.id.image_view);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
        imageView.setImageBitmap(bitmap);


        Matrix matrix = new Matrix();

        if (bitmap.getWidth() > bitmap.getHeight())
        {
            RectF drawableRect = new RectF(0, 0, imageWidth, imageHeight);
            RectF viewRect = new RectF(0, 0, _height, _width);
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            matrix.postRotate(90);
            matrix.postTranslate(_width, 0);
        }
        else
        {
            RectF drawableRect = new RectF(0, 0, imageWidth, imageHeight);
            RectF viewRect = new RectF(0, 0, _width, _height);
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
        }





        imageView.setImageMatrix(matrix);


        container.addView(item_view);

        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}
