package com.knziha.polymer.webfeature;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.knziha.polymer.browser.AppIconCover.AppLoadableBean;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.util.List;

import static com.knziha.polymer.webslideshow.ImageViewTarget.FuckGlideDrawable;

public class NavigationNodeCntTask implements AppLoadableBean, Runnable {
	final TreeViewNode[] array;
	final Resources mResources;
	final TextView tv;
	final int resID;
	int folderCnt;
	int nodeCnt;
	int incrementCnt;
	public NavigationNodeCntTask(TreeViewNode[] array, Resources mResources, TextView tv, int resID) {
		this.array = array;
		this.mResources = mResources;
		this.tv = tv;
		this.resID = resID;
	}
	
	@Override
	public Drawable load() {
		for(TreeViewNode n : array) {
			if (!traverseNode(n)) {
				return FuckGlideDrawable;
			}
		}
		if(tv.isShown())
		{
			tv.post(this);
		}
		return FuckGlideDrawable;
	}
	
	private boolean traverseNode(TreeViewNode n) {
		boolean isLeaf = n.isLeaf();
		if (isLeaf) {
			nodeCnt++;
		} else {
			folderCnt++;
		}
		incrementCnt++;
		if (incrementCnt>1000) {
			if (tv.isShown())
			{
				tv.removeCallbacks(this);
				tv.post(this);
			}
			else {
				//CMN.Log("提前终止了哟");
				return false;
			}
			incrementCnt=0;
			try {
				Thread.sleep(300); // 懒死了
			} catch (InterruptedException ignored) { }
		}
		if (!isLeaf) {
			List<TreeViewNode> list = n.getChildList();
			for (int i = 0, len=list.size(); i < len; i++) {
				if (!traverseNode(list.get(i))) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void run() {
		tv.setText(mResources.getString(resID, nodeCnt, folderCnt));
	}
}
