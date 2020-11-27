package com.knziha.polymer.pdviewer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;

import java.io.File;
import java.io.IOException;

public class PDocBenchMarkctivity extends Toastable_Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//ImageviewDebugBinding UIData = DataBindingUtil.setContentView(this, R.layout.imageview_debug);
		
		
		
		try {
			//PDocument pdoc = new PDocument(this, "/sdcard/myFolder/sample_hetero_dimension.pdf");
			PDocument pdoc = new PDocument(this, Uri.fromFile(new File("/sdcard/myFolder/Gpu Pro 1.pdf")), dm, null);
//			PDocument pdoc = new PDocument(this, "/sdcard/myFolder/YotaSpec2.pdf", dm, null);
			Bitmap bm = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
//			bm = pdoc.drawTumbnail(bm, 0, 1);
			
			long start = System.currentTimeMillis();
			for (int i = 0; i < 10; i++) {
				bm = pdoc.drawTumbnail(bm, i, 1);
			}
			long time = System.currentTimeMillis() - start;
			showT("PDF_RENDER TIME::"+time);
			CMN.Log("PDF_RENDER TIME::", time);
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
}
