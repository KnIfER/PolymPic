package com.knziha.polymer.webstorage;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.OptionProcessor;
import com.knziha.polymer.Utils.Options;

import static com.knziha.polymer.Toastable_Activity.AppBlack;
import static com.knziha.polymer.widgets.Utils.indexOf;

public class StandardConfigDialogBase {
	public AlertDialog dlg;
	public TextView tv;
	public TextView title;
	public TextView bottomTitle;
	public SpannableStringBuilder str_global=new SpannableStringBuilder();
	
	public static class ClickSpanBuilder {
		BrowserActivity a;
		OptionProcessor optprs;
		TextView tv;
		SpannableStringBuilder text;
		String[] title;
		String[] coef;
		String[] tmpTitle;
		String[] tmpCoef;
		boolean tmpIsTitle;
		String prefix;
		int coefOff;
		boolean prenth=true;
		Options opt;
		
		public ClickSpanBuilder(BrowserActivity a1, Options opt1, OptionProcessor optprs1, TextView tv1, SpannableStringBuilder text1, String[] coef1) {
			a = a1;
			opt = opt1;
			optprs = optprs1;
			tv = tv1;
			text = text1;
			coef = coef1;
		}
		
		public ClickSpanBuilder init_clickspan_with_bits_at(int titleOff,
															int coefShift, long mask,
															int flagPosition, int flagMax, int flagIndex,
															int processId) {
			final String[] title = tmpTitle!=null?tmpTitle:this.title;
			final String[]  coef = tmpIsTitle?null:tmpCoef!=null?tmpCoef:this.coef;
			tmpTitle=tmpCoef=null;
			final boolean prenth = !tmpIsTitle && this.prenth;
			tmpIsTitle=false;
			int start = text.length();
			int now = start+ title[titleOff].length();
			text.append(prenth?"[":"{ ");
			if(prefix!=null) {
				text.append(prefix);
			}
			text.append(title[titleOff]);
			if(coef!=null){
				text.append(" :");
				long val = (opt.Flag(a, flagIndex)>>flagPosition)&mask;
				text.append(coef[coefOff+(int) ((val)+coefShift)%(flagMax+1)]);
			}
			text.append(prenth?"]":" }").append("\r\n");
			text.setSpan(new ClickableSpan() {
				@Override
				public void updateDrawState(@NonNull TextPaint paint) {
					if(prenth) {
						super.updateDrawState(paint);
					}
				}
				@Override
				public void onClick(@NonNull View widget) {
					if(coef==null){
						if(optprs!=null) {
							optprs.processOptionChanged(this, widget, processId , -1);
						}
						return;
					}
					long flag = opt.Flag(a, flagIndex);
					long val = (flag>>flagPosition)&mask;
					val=(val+1)%(flagMax+1);
					flag &= ~(mask << flagPosition);
					flag |= (val << flagPosition);
					opt.Flag(a, flagIndex, flag);
					int fixedRange = indexOf(text, ':', now);
					text.delete(fixedRange+1, prenth?indexOf(text, ']', fixedRange):(fixedRange+3));
					val=(val+coefShift)%(flagMax+1);
					text.insert(fixedRange+1,coef[(int) (coefOff+val)]);
					tv.setText(text);
					if(optprs!=null) {
						optprs.processOptionChanged(this, widget, processId, (int) val);
					}
				}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			text.append("\r\n");
			return this;
		}
		
		public ClickSpanBuilder withTitle(String[] dictOpt1) {
			title =dictOpt1;
			return this;
		}
		
		public ClickSpanBuilder withCoef(String[] coef1) {
			coef=coef1;
			return this;
		}
		
		public ClickSpanBuilder withPrefix(String prefix1) {
			prefix=prefix1;
			return this;
		}
		
		public ClickSpanBuilder withBraces(boolean prenth1) {
			prenth=prenth1;
			return this;
		}
		
		public ClickSpanBuilder withTmpTitleAndCoef(String[] title, String[] coef) {
			if(title!=null) {
				tmpTitle=title;
			}
			if(coef!=null) {
				tmpCoef = coef;
			}
			return this;
		}
		
		public ClickSpanBuilder withTmpIsTitle(boolean title) {
			tmpIsTitle=title;
			return this;
		}
	}
	
	public static void buildStandardConfigDialog(Context context, StandardConfigDialogBase resultHolder, View.OnClickListener onclick, int title_id, Object...title_args) {
		final View dv = LayoutInflater.from(context).inflate(R.layout.dialog_about,null);
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		Resources mResource = context.getResources();
		if(title_id!=0) {
			if(title_args.length>0){
				title.setText(mResource.getString(title_id, title_args));
			} else {
				title.setText(title_id);
			}
		}
		title.setTextSize(GlobalOptions.isLarge?19f:18f);
		title.setTextColor(AppBlack);
		//title.getPaint().setFakeBoldText(true);
		
		int topad = (int) mResource.getDimension(R.dimen._18_);
		((ViewGroup)title.getParent()).setPadding(topad*3/5, topad/2, 0, 0);
		//((ViewGroup)title.getParent()).setClipToPadding(false);
		//((ViewGroup.MarginLayoutParams)title.getLayoutParams()).setMarginStart(-topad/4);
		
		Options.setAsLinkedTextView(tv);
		
		final AlertDialog configurableDialog =
				new AlertDialog.Builder(context,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line)
						.setView(dv)
						.create();
		configurableDialog.setCanceledOnTouchOutside(true);
		
		dv.findViewById(R.id.cancel).setOnClickListener(v -> {
			if(onclick!=null) onclick.onClick(v);
			configurableDialog.dismiss();
		});
		
		resultHolder.dlg=configurableDialog;
		resultHolder.tv=tv;
		resultHolder.title=title;
		resultHolder.bottomTitle = dv.findViewById(R.id.bottom_title);
	}
}
