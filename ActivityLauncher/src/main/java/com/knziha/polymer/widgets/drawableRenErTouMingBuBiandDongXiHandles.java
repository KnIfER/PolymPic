package com.knziha.polymer.widgets;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

@RequiresApi(api = Build.VERSION_CODES.M)
public class drawableRenErTouMingBuBiandDongXiHandles {
	public static final PorterDuffColorFilter selectionHandleColorFilter = new PorterDuffColorFilter(0xce2568ED, PorterDuff.Mode.SRC_IN);
	private static abstract class dRen_base extends DrawableWrapper {
		protected dRen_base(int res_id) {
			super(dr(res_id));
			Drawable dr = getDrawable();
			dr.setColorFilter(selectionHandleColorFilter);
		}
		
		private static Drawable dr(int res_id) {
			Drawable dr=null;
			if(CMN.mResource!=null)
				dr=CMN.mResource.getDrawable(res_id);
			return dr;
		}
		
		@Override public void setAlpha(int alpha) { }
	}
	
	public static class dRen_base_L extends dRen_base {
		public dRen_base_L() {
			super(R.drawable.abc_text_select_handle_left_mtrl_dark);
		}
	}
	public static class dRen_base_R extends dRen_base {
		public dRen_base_R() {
			super(R.drawable.abc_text_select_handle_right_mtrl_dark);
		}
	}
	public static class dRen_base_M extends dRen_base {
		public dRen_base_M() {
			super(R.drawable.abc_text_select_handle_middle_mtrl_dark);
		}
	}
}
