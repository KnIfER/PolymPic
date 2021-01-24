package com.knziha.polymer.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

public class LinearFramer extends LinearLayout {
	public LinearFramer(Context context) {
		super(context);
	}
	public LinearFramer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public LinearFramer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public LinearFramer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public int mMaxHeight=-1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(mMaxHeight!=-1) {
			int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			int heightSize = MeasureSpec.getSize(heightMeasureSpec);

			if (heightMode == MeasureSpec.EXACTLY) {
				heightSize = heightSize <= mMaxHeight ? heightSize
						: mMaxHeight;
			}

			if (heightMode == MeasureSpec.UNSPECIFIED) {
				heightSize = heightSize <= mMaxHeight ? heightSize
						: mMaxHeight;
			}
			if (heightMode == MeasureSpec.AT_MOST) {
				heightSize = heightSize <= mMaxHeight ? heightSize
						: mMaxHeight;
			}

			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

	

}
