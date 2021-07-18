package com.knziha.polymer.widgets;

import android.view.View;

import com.shockwave.pdfium.treeview.TreeViewNode;

public interface NavViewListener extends View.OnClickListener{
	void showEditorDlg(NavigationHomeAdapter navigationHomeAdapter, NavigationNode node, String url, String title);
	
	void showPopupMenu(NavigationHomeAdapter navAdapter, View headerView);
	
	boolean getShowMultilineText();
	
	boolean getShowDragHandle();
	
	boolean getSelMode();
	
	boolean getEditMode();
	
	void UseNodeData(NavigationHomeAdapter navAdapter, TreeViewNode node);
}
