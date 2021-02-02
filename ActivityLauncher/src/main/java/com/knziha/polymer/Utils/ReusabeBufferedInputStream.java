package com.knziha.polymer.Utils;

import java.io.InputStream;

public class ReusabeBufferedInputStream extends BufferedInputStream {
	public ReusabeBufferedInputStream(InputStream in) {
		super(in);
	}
	
	protected ReusabeBufferedInputStream(InputStream in, byte[] buf) {
		super(in, buf);
	}
	
	public void reuse(InputStream in) {
		this.in = in;
		this.count = 0;
		this.pos = 0;
		this.markpos = -1;
	}
	
	public ReusabeBufferedInputStream reconstruct(InputStream in) {
		return new ReusabeBufferedInputStream(in, buf);
	}
	
}
