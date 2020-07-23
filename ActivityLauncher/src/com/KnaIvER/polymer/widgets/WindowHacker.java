package com.knaiver.polymer.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.knaiver.polymer.R;

public class WindowHacker extends RelativeLayout {
	private final WindowManager wm;
	View floatView;
	int floatViewId;
	public WindowHacker(Context context) {
		this(context, null);
	}

	public WindowHacker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WindowHacker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		if (!isInEditMode()) {
			TypedArray StyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.SplitView);
			floatViewId = StyledAttributes.getResourceId(R.styleable.SplitView_handle, 0);
			if(floatViewId==0){
				throw new IllegalArgumentException(StyledAttributes.getPositionDescription() +
						": The required attribute floatView must refer to a valid parent view.");
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	public void setLayoutParams(ViewGroup.LayoutParams params) {
		if(params.height!=0 || params.width!=-1){
			if (!isInEditMode()) {
				if(floatView==null){
					ViewParent p=this;
					while((p=p.getParent()) instanceof  ViewGroup){
						if(((ViewGroup) p).getId()==floatViewId){
							floatView = (View) p;
							break;
						}
					}
				}
			}
			if(floatView!=null && floatView.getTag() instanceof WindowManager.LayoutParams){
				//CMN.Log("hacking!!!", floatView);
				WindowManager.LayoutParams lp = (WindowManager.LayoutParams) floatView.getTag();
				((LinearLayout.LayoutParams)getLayoutParams()).weight=1;
				lp.height=params.height+((SplitView)floatView).getHandle().getMeasuredHeight();
				wm.updateViewLayout(floatView, lp);
				//CMN.Log("hacking.." ,((LinearLayout.LayoutParams)getLayoutParams()).weight+"??");
			}
		}else {
			super.setLayoutParams(params);
			//CMN.Log("not hacking.." ,((LinearLayout.LayoutParams)getLayoutParams()).weight+"??");
		}
	}
}
