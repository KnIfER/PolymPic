package com.knziha.filepicker.view;

import android.app.Dialog;
import android.content.Context;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

//https://www.jianshu.com/p/6c9d0412d30f
public class GoodKeyboardDialog extends Dialog {
    public GoodKeyboardDialog(@NonNull Context context) {
        super(context);
    }
    //public GoodKeyboardDialog(@NonNull Context context, int themeResId) {
    //    super(context, themeResId);
    //}

    //protected GoodKeyboardDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
    //    super(context, cancelable, cancelListener);
    //}

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        	float x=ev.getX(), y=ev.getY();
        	if(getWindow()!=null){
				View decorView=getWindow().getDecorView();
				if(y>decorView.getBottom() || y<decorView.getTop() || x>decorView.getRight() || x<decorView.getLeft()){
					hideKeyboard();
				}
			}
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
		View v = getCurrentFocus();
		if (v instanceof EditText) {
			IBinder token = v.getWindowToken();
			if (token != null) {
				InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
    }

	@Override
	public void dismiss() {
		hideKeyboard();
		super.dismiss();
	}
}