package com.knziha.polymer.webslideshow;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;

public class ViewUtils {

    /**
     * Get center child in X Axes
     */
    public static View getCenterXChild(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterX(recyclerView, child)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Get position of center child in X Axes
     */
    public static int getCenterXChildPosition(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterX(recyclerView, child)) {
                    return recyclerView.getChildAdapterPosition(child);
                }
            }
        }
        return ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }
    
    public static View getCenterXChildV1(RecyclerViewPager recyclerView) {
        int childCount = recyclerView.getChildCount();
		int middleX = recyclerView.getWidth() / 2;
		View child;
		int pad = recyclerView.itemPad/2;
		for (int i = 0; i < childCount; i++) {
			child = recyclerView.getChildAt(i);
			if (child.getRight()+pad>middleX) {
				return child;
			}
		}
        return null;
    }
    
    public static int getCenterXChildPositionV1(RecyclerViewPager recyclerView) {
        View childView = getCenterXChildV1(recyclerView);
        if (childView!=null) {
			return recyclerView.getChildAdapterPosition(childView);
        }
        return ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }

    /**
     * Get center child in Y Axes
     */
    public static View getCenterYChild(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterY(recyclerView, child)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Get position of center child in Y Axes
     */
    public static int getCenterYChildPosition(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterY(recyclerView, child)) {
                    return recyclerView.getChildAdapterPosition(child);
                }
            }
        }
        return childCount;
    }
	
	public static View getCenterYChildV1(RecyclerViewPager recyclerView) {
		int childCount = recyclerView.getChildCount();
		int middleY = recyclerView.getHeight() / 2;
		if (childCount > 0) {
			View child;
			for (int i = 0; i < childCount; i++) {
				child = recyclerView.getChildAt(i);
				if (child.getBottom()>middleY) {
					return child;
				}
			}
		}
		return null;
	}
	
	public static int getCenterYChildPositionV1(RecyclerViewPager recyclerView) {
		View childView = getCenterYChildV1(recyclerView);
		if (childView!=null) {
			return recyclerView.getChildAdapterPosition(childView);
		}
		return recyclerView.getChildCount();
	}
	
    public static boolean isChildInCenterX(RecyclerView recyclerView, View view) {
        int childCount = recyclerView.getChildCount();
        int[] lvLocationOnScreen = new int[2];
        int[] vLocationOnScreen = new int[2];
        recyclerView.getLocationOnScreen(lvLocationOnScreen);
        int middleX = lvLocationOnScreen[0] + recyclerView.getWidth() / 2;
        if (childCount > 0) {
            view.getLocationOnScreen(vLocationOnScreen);
            if (vLocationOnScreen[0] <= middleX && vLocationOnScreen[0] + view.getWidth() >= middleX) {
                return true;
            }
        }
        return false;
    }

    public static boolean isChildInCenterY(RecyclerView recyclerView, View view) {
        int childCount = recyclerView.getChildCount();
        int[] lvLocationOnScreen = new int[2];
        int[] vLocationOnScreen = new int[2];
        recyclerView.getLocationOnScreen(lvLocationOnScreen);
        int middleY = lvLocationOnScreen[1] + recyclerView.getHeight() / 2;
        if (childCount > 0) {
            view.getLocationOnScreen(vLocationOnScreen);
            if (vLocationOnScreen[1] <= middleY && vLocationOnScreen[1] + view.getHeight() >= middleY) {
                return true;
            }
        }
        return false;
    }
}