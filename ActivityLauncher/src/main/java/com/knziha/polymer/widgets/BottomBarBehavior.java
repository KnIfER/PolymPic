package com.knziha.polymer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.knziha.polymer.Utils.CMN;

public class BottomBarBehavior extends CoordinatorLayout.Behavior<View> {
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
	private boolean hiding;
	
	public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);  
    }  
  
    @Override  
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, @ViewCompat.NestedScrollType int type) {
        return true;
    }  
  
    @Override  
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {  
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);  
		//CMN.Log("onNestedPreScroll", dy);
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dy > 10) {
                if(!hiding)
                	hide(child);
            } else if (dy < -10) {
				if(hiding)
					show(child);
            }  
        }  
    }  
  
    private void hide(final View view) {
    	hiding = true;
        view.animate()
			.translationY(view.getHeight())
			.setInterpolator(INTERPOLATOR)
			.setDuration(500);
    }
    
    private void show(final View view) {
		hiding = false;
        view.animate()
			.translationY(0)
			.setInterpolator(INTERPOLATOR)
			.setDuration(500);
    }  
  
}  