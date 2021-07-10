package com.knziha.polymer.browser.webkit;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

public class InvisibleActionModeWebView extends XWalkWebView {

    private static final String CROSS_INTERFACE = "Client";

    private OnTextSelectionListener mTextSelectedListener;

    public InvisibleActionModeWebView(Context context) {
        super(context);
        addJavascriptInterface(new WebAppInterface(), CROSS_INTERFACE);
    }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        ActionMode actionMode = super.startActionModeForChild(originalView, new SelectTextActionModeCallback());
        return actionMode;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        ActionMode actionMode = super.startActionMode(new SelectTextActionModeCallback());
        return actionMode;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        ActionMode actionMode = super.startActionMode(new SelectTextActionModeCallback(), type);
        return actionMode;
    }

    private class SelectTextActionModeCallback implements ActionMode.Callback {

        private View mDummyView;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (mode != null) {
                mDummyView = new View(getContext());
                mode.setCustomView(mDummyView);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            loadUrl("javascript:" + CROSS_INTERFACE + ".onTextSelected(window.getSelection().toString());");
            if (mode != null) {
                if (mDummyView != null && mDummyView.getParent() != null) {
                    View actionModeCointainer = (View) mDummyView.getParent();
                    ViewGroup.LayoutParams params = actionModeCointainer.getLayoutParams();
                    params.height = 0;
                    actionModeCointainer.setLayoutParams(params);
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mTextSelectedListener != null) {
                mTextSelectedListener.onSelectionDismissed();
            }
            mDummyView = null;
        }
    }

    public void setTextSelectionListener(OnTextSelectionListener onTextSelectedListener) {
        this.mTextSelectedListener = onTextSelectedListener;
    }

    private class WebAppInterface {

        @JavascriptInterface
        public void onTextSelected(String selection) {
            if (mTextSelectedListener != null) {
                mTextSelectedListener.onTextSelected(selection);
            }
        }
    }

    public interface OnTextSelectionListener {
        /**
         * Activity.runOnUiThread should be used, if UI modification is involved!
         */
        void onTextSelected(String text);

        /**
         * Activity.runOnUiThread should be used, if UI modification is involved!
         */
        void onSelectionDismissed();
    }
}