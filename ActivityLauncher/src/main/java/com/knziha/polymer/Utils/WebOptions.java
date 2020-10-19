package com.knziha.polymer.Utils;

import org.adrianwalker.multilinestring.Multiline;

public interface WebOptions {
	public final static int StorageSettings=0;
	public final static int BackendSettings=1;
	
	boolean getForbidLocalStorage();
	boolean toggleForbidLocalStorage();
	boolean getForbidCookie();
	boolean getForbidDom();
	boolean getForbidDatabase();
	
	boolean getPCMode();
	boolean togglePCMode();
	boolean getEnableJavaScript();
	boolean getForbitNetworkImage();
	boolean getMuteAlert();
	boolean getMuteDownload();
	boolean getPremature();
	
	
	
}
