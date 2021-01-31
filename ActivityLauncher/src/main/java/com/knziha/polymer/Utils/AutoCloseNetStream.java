package com.knziha.polymer.Utils;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class AutoCloseNetStream extends AutoCloseInputStream {
	final HttpURLConnection connection;
	/**
	 * Creates an automatically closing proxy for the given input stream.
	 *
	 * @param in underlying input stream
	 * @param connection
	 */
	public AutoCloseNetStream(InputStream in, HttpURLConnection connection) {
		super(in);
		this.connection = connection;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		connection.disconnect();
		//CMN.Log("自动关闭连接……");
	}
}
