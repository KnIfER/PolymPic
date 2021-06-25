package com.knziha.polymer.webstorage;


import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.OptionProcessor;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.WebFrameLayout;

import static com.knziha.polymer.widgets.Utils.indexOf;

public class WebViewSettingsDialog extends StandardConfigDialogBase
		implements OptionProcessor, View.OnClickListener {
	SpannableStringBuilder str_tab;
	ForegroundColorSpan YinYangSpan;
	boolean domain;
	int region;
	int regionBackUp=0;
	boolean globalOrTabBackUp;
	boolean tab;
	String domainSettings;
	private String[] groupNym;
	//private String[] groupPrefix;
	private String[] groupApply;
	private boolean bottomTitleSet;
	private final Resources mResource;
	private final BrowserActivity a;
	
	public WebViewSettingsDialog(BrowserActivity a) {
		this.a = a;
		this.mResource = a.mResource;
	}
	
	public void clear(){
		str_global.clear();
		str_tab.clear();
	}

	@Override
	public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
		switch (processId) {
			case 1:
			case 2:
				//a.topMenuRequestedInvalidate|=1<<StorageSettings;
				//a.topMenuRequestedInvalidate|=1<<BackendSettings;
				if (!tab && !domain) {
					WebFrameLayout.GlobalSettingsVersion ++;
				}
				a.topMenuRequestedInvalidate = true;
			break;
			case 666:
				init_web_configs(tab, region==-1?regionBackUp:-1, domain);
			break;
		}
	}

	public void init_web_configs(boolean tab, int groupID, boolean domain) {
		this.domain=domain;
		this.region=groupID;
		if(region!=-1) {
			regionBackUp=region;
		}
		if(!bottomTitleSet) {
			bottomTitle.setVisibility(View.VISIBLE);
			bottomTitle.setOnClickListener(this);
			bottomTitle.setText("网站设定");
			bottomTitleSet = true;
		}
		if (a.currentViewImpl!=null) {
			bottomTitle.setText("网站设定("+a.currentViewImpl.domain+")");
		}
		this.tab = tab;
		if(YinYangSpan==null) {
			str_tab=new SpannableStringBuilder();
			SpannableStringBuilder str_title = new SpannableStringBuilder(title.getText());
			title.setText(str_title, TextView.BufferType.SPANNABLE);
			YinYangSpan=new ForegroundColorSpan(0x8a8f8f8f);
			title.setOnClickListener(this);
		}
		
		
		final String[] Coef = mResource.getString(R.string.coef).split("_");
		SpannableStringBuilder ssb = str_tab;
		ssb.clear();
		ClickSpanBuilder builder = new ClickSpanBuilder(a, a.opt, this, tv, ssb, Coef);
		
		boolean buildSingle=groupID!=-1;
		
		groupApply = mResource.getStringArray(R.array.GroupApply);
		
		int flagIndex=domain?Options.WebViewSettingsSource_DOMAIN:tab?Options.WebViewSettingsSource_TAB:3;
		
		int bitMax = 1;
		int bitMask = 0x1;
		int bitShift = 0;
		boolean isDelegate = tab || domain;
		Spannable text = (Spannable) title.getText();
		if(domain)
		{
//				bitShift = 1;
//				bitMax = 2;
//				bitMask = 0x3;
			text.setSpan(YinYangSpan, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			bottomTitle.setTextColor(0xff000000);
		} else {
			int idx = indexOf(text, '/', 0);
			text.setSpan(YinYangSpan, tab?0:(idx+1), tab?idx:text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			bottomTitle.setTextColor(0x8a8f8f8f);
		}
		
		switch (groupID) {
			default:
				//ssb.append("存储组");
			/* Group#存储 */
			case 0:
				if(!buildSingle) webSepGroupApply(builder, ssb, 0, 6, flagIndex);
				builder.withTitle(mResource.getStringArray(R.array.BenDiCunChu))
					.init_clickspan_with_bits_at(0,  0, bitMask, 2, bitMax, flagIndex, 1)//总开关
					.init_clickspan_with_bits_at(1, 1, bitMask, 3, bitMax, flagIndex, 1)//Cookie
					.init_clickspan_with_bits_at(2, 1, bitMask, 4, bitMax, flagIndex, 1)//DataBase
					.init_clickspan_with_bits_at(3, 1, bitMask, 5, bitMax, flagIndex, 1);//Dom
				
				if(buildSingle) {
					ssb.append("\r\n");
					if(isDelegate) {
						builder.init_clickspan_with_bits_at(4, 0, bitMask, 6, bitMax, flagIndex, 1);//Apply
					}
					break;
				} else {
					append_hr(ssb, tab);
				}
			/* Group#客户端 */
			case 1:
				if(!buildSingle) webSepGroupApply(builder, ssb, 1, 11, flagIndex);
				builder.withTitle(mResource.getStringArray(R.array.KeHuDuan))
					.init_clickspan_with_bits_at(0,  0, bitMask, 7, bitMax, flagIndex, 2)
					.init_clickspan_with_bits_at(1, 0, bitMask, 12, bitMax, flagIndex, 2)
					.init_clickspan_with_bits_at(2, 0, bitMask, 9, bitMax, flagIndex, 2)
					.init_clickspan_with_bits_at(3, 0, bitMask, 10, bitMax, flagIndex, 2)
					.init_clickspan_with_bits_at(4, 0, bitMask, 13, bitMax, flagIndex, 2)
					.init_clickspan_with_bits_at(5, 1, bitMask, 8, bitMax, flagIndex, 2)
				;
				if(buildSingle) {
					ssb.append("\r\n");
					if(isDelegate) {
						builder.init_clickspan_with_bits_at(6, 0, bitMask, 11, bitMax, flagIndex, 2);//Apply
					}
					break;
				} else {
					append_hr(ssb, tab);
				}
				
		}
		
		builder.withTitle(groupApply).withCoef(null)
				.init_clickspan_with_bits_at(buildSingle?1:2, 0, bitMask, 10, bitMax, flagIndex, 666);
		
		if(buildSingle) {
			ssb.delete(ssb.length()-2, ssb.length());
			
			if(tab) {
				ssb.delete(ssb.length()-2, ssb.length());
			}
		}
		
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
	}
	
	private void webSepGroupApply(ClickSpanBuilder builder, SpannableStringBuilder ssb, int group, int pos, int flagIndex) {
		if(groupNym==null) {
			groupNym = mResource.getStringArray(R.array.GroupName);
			//groupPrefix = mResource.getStringArray(R.array.GroupPrefix);
		}
		if(tab || domain) {
			builder.withTitle(groupNym).withBraces(false)/*.withPrefix(groupPrefix[group])*/
					.init_clickspan_with_bits_at(group, 0, 0x1, pos, 1, flagIndex, 0)
					.withPrefix(null)
					.withBraces(true)
			;
		} else {
			ssb/*.append(groupPrefix[group])*/.append(groupNym[group]).append("\r\n").append("\r\n");
		}
	}
	
	private void append_hr(SpannableStringBuilder ssb, boolean globalOrTab) {
		//ssb.append(Html.fromHtml("<HR>", Html.FROM_HTML_MODE_COMPACT));
		ssb.append("\r\n").append("\r\n");
	}
	
	@Override
	public void onClick(View v) {
		boolean intoWebSettings = !domain&&v.getId()==R.id.bottom_title;
		if(intoWebSettings) {
			globalOrTabBackUp = tab;
			init_web_configs(false, region, true);
		} else {
			if(!domain) {
				tab = !tab;
			} else {
				tab = globalOrTabBackUp;
			}
			init_web_configs(tab, region, false);
			v.getBackground().jumpToCurrentState();
		}
//			if(realm==0) {
//			}
	}
}