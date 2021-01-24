package com.knziha.polymer.Utils;

import java.util.regex.Pattern;

public class RgxPlc {
	public final Pattern p;
	public final String rep;
	
	public RgxPlc(Pattern p, String rep) {
		this.p = p;
		this.rep = rep;
	}
}
