package com.knziha.polymer.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

public class QRGenerator extends Activity {
	private View root;
	private ImageView qr_frame;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qr1);
		qr_frame = findViewById(R.id.qr_frame);
		root = findViewById(R.id.root);
		root.setOnClickListener(v -> finish());
		Intent intent = getIntent();
		String text = intent==null?null:intent.getStringExtra(Intent.EXTRA_TEXT);
		if(TextUtils.isEmpty(text)) {
			text = "QRCode";
		}
		try {
			Bitmap bm = generateQRCode(text);
			qr_frame.setImageBitmap(bm);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public static Bitmap generateQRCode(String url) throws WriterException {
		if (url == null || url.equals("")) {
			return null;
		}
		
		// 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300);
		
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		
		// 二维矩阵转为一维像素数组,也就是一直横着排了
		int[] pixels = new int[width * height];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				} else {
					pixels[y * width + x] = 0xffffffff;
				}
			}
		}
		return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
//		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//		return bitmap;
	}
}
