package com.knziha.polymer.pdviewer;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.databinding.ImageviewDebugBinding;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;

public class PDocViewerActivity extends Toastable_Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageviewDebugBinding UIData = DataBindingUtil.setContentView(this, R.layout.imageview_debug);
		
		try {
			UIData.wdv.dm=dm;
			
			//PDocument pdoc = new PDocument(this, "/sdcard/myFolder/sample_hetero_dimension.pdf");
			//PDocument pdoc = new PDocument(this, "/sdcard/myFolder/Gpu Pro 1.pdf", dm, null);
			//UIData.wdv.setDocument(pdoc);
			
			UIData.wdv.a=this;
			
			UIData.wdv.setSelectionPaintView(UIData.sv);
			
			//UIData.wdv.setDocumentPath("/sdcard/myFolder/Gpu Pro 1.pdf");
			UIData.wdv.setDocumentPath("/sdcard/myFolder/YotaSpec02.pdf");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
}
