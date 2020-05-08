package com.knziha.filepicker.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


public class RecyclerViewmy extends RecyclerView {
	public RecyclerViewmy(Context context) {
		super(context);
	}
	public RecyclerViewmy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public RecyclerViewmy(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}
	public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    private ScrollViewListener scrollViewListener = null;
    public boolean bScrollEnabled=true;
    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface ScrollViewListener {
        void onScrollChanged(View scrollView, int x, int y, int oldx, int oldy);
    }




	
}