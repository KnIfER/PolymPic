package com.knziha.polymer.webstorage;

import org.adrianwalker.multilinestring.Multiline;

public class WebOptions {
	public final static int StorageSettings=0;
	public final static int BackendSettings=1;
	public static long tmpFlag;
	@Multiline(flagPos=2) public static boolean getForbidLocalStorage(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=2) public static boolean toggleForbidLocalStorage(long flag){ flag=flag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=3, shift=1) public static boolean getUseCookie(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=4, shift=1) public static boolean getUseDomStore(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=5, shift=1) public static boolean getUseDatabase(long flag){ flag=flag; throw new RuntimeException(); }
	
	@Multiline(flagPos=7) public static boolean getPCMode(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=7) public static boolean togglePCMode(){ tmpFlag=tmpFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=8, shift=1) public static boolean getEnableJavaScript(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=9) public static boolean getNoAlerts(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=10) public static boolean getNoCORSJump(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=12) public static boolean getForbidNetworkImage(long flag){ flag=flag; throw new RuntimeException(); }
	@Multiline(flagPos=13) public static boolean getPremature(long flag){ flag=flag; throw new RuntimeException(); }


}
