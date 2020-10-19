package com.knziha.polymer.pdviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import androidx.databinding.DataBindingUtil;

import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.ImageviewDebugBinding;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PDocViewerActivity extends Toastable_Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageviewDebugBinding UIData = DataBindingUtil.setContentView(this, R.layout.imageview_debug);
		
		//UIData.image.setImageBitmap();
		PdfiumCore pdfiumCore = new PdfiumCore(this);
		UIData.image.setImageResource(R.drawable.ic_launcher);
		try {
			PDocument pdoc = new PDocument(this, "/sdcard/myFolder/sample.pdf");
			
			//UIData.image.setImageBitmap(pdoc.renderBitmap(0, 1));
			
			UIData.wdv.dm=dm;
			
			UIData.wdv.setDocument(pdoc);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
