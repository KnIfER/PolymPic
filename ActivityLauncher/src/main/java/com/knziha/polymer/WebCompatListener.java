package com.knziha.polymer;

import android.util.Pair;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.webstorage.SubStringKey;
import com.knziha.polymer.widgets.WebFrameLayout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.xwalk.core.Utils.getTag;


public class WebCompatListener extends WebCompoundListener{
	public WebCompatListener(BrowserActivity activity) {
		super(activity);
		isLowEnd = false;
	}
	
	@Nullable
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		//CMN.Log("SIR::", url);
		boolean b1 = view instanceof UniversalWebviewInterface;
		View mWebView = (View) (b1?view:getTag());
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		Map<String, String> headers = null;
		if(!b1) {
			headers = ((UniversalWebviewInterface)mWebView).getLastRequestHeaders();
			if(headers!=null) {
				url = headers.get("Url");
			}
		}
		if(layout==null || url==null) {
			return null;
		}
		
		boolean lishanxizhe = false;
		
		String host = null;
		if(true) {
			String addr = jinkeSheaths.get(SubStringKey.new_hostKey(url));
			if(addr!=null) {
				try {
					URL oldUrl = new URL(url);
					host = oldUrl.getHost();
					//if(false)
					url = url.replaceFirst(oldUrl.getHost(), addr);
					CMN.Log("秦王绕柱走", url);
					lishanxizhe = true;
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
		List<Pair> moders = null;
		if (layout.modifiers!=null) {
			for (Pair p:layout.modifiers) {
				if(((Pattern)(p.first)).matcher(url).find()) {
					if (moders==null) moders = new ArrayList<>();
					moders.add(p);
				}
			}
		}
		//CMN.Log("修改了::??", url, moders!=null, layout.modifiers);
		if(lishanxizhe /*&& !(url.endsWith("google.com/")||url.endsWith("google.cn/"))*/
				|| moders!=null) {
			try {
				String method = "GET";
				if (headers==null) {
					headers = new HashMap<>();
				} else {
					method = headers.get("method");
				}
				return getClientResponse(url, host, moders, headers, method);
			} catch (IOException e) {
				CMN.Log(url+"\n", e);
				//return emptyResponse;
				//return null;
			}
		}
		//return view==null?null:shouldInterceptRequest(view, url);
		//CMN.Log("layout.hasPrune::", layout.hasPrune);
		if(layout.hasPrune) {
			WebResourceResponse ret = handlePrunes(layout, url);
			if (ret!=null) return ret;
		}
		//CMN.Log("request.getUrl().getScheme()", request.getUrl().getScheme());
		if(url.startsWith("https://mark.js")&&markjsBytesArr!=null) {
			//CMN.Log("加载中", new String(markjsBytesArr, 0, 200));
			return new WebResourceResponse("text/javascript", "utf8", new ByteArrayInputStream(markjsBytesArr));
		}
		return null;
	}
}
