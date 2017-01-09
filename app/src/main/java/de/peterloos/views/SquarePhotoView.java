package de.peterloos.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Peter on 27.12.2016.
 */

public class SquarePhotoView extends ImageView {

    private static final String TAG = "PeLo";

    public SquarePhotoView(Context context) {
        super(context);
    }

    public SquarePhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquarePhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        int width = this.getMeasuredWidth();
//        this.setMeasuredDimension(width, width);

        int height = this.getMeasuredHeight();
        this.setMeasuredDimension(height, height);

   //     String s = String.format("widthMeasure = %d, heightMeasure = %d", widthMeasureSpec, heightMeasureSpec);
   //     Log.v(TAG, s);
       // Log.v(TAG, "onMeasure ==> getMeasuredHeight = " + Integer.toString(height));
      //  Log.v(TAG, "");
    }
}
