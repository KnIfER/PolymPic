package com.knziha.polymer.Utils;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class ReusabeBufferedInputStream extends BufferedInputStream {
	public ReusabeBufferedInputStream(InputStream in) {
		super(in);
	}
	
	public void reuse(InputStream in) {
		this.in = in;
		this.count = 0;
		this.pos = 0;
		this.markpos = -1;
	}
	
}
