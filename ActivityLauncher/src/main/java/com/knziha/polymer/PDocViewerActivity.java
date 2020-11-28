package com.knziha.polymer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.knziha.filepicker.utils.FU;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.databinding.ActivityPdfviewerBinding;
import com.knziha.polymer.pdviewer.PDFPageParms;
import com.knziha.polymer.pdviewer.PDocView;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.pdviewer.bookmarks.BookMarksFragment;
import com.knziha.polymer.widgets.AppIconsAdapter;
import com.knziha.polymer.widgets.DescriptiveImageView;
import com.knziha.polymer.widgets.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.knziha.polymer.BrowserActivity.GoogleTranslate;

public class PDocViewerActivity extends Toastable_Activity implements View.OnClickListener {
	ActivityPdfviewerBinding UIData;
	private boolean hidingContextMenu;
	public PDocView currentViewer;
	protected boolean this_instanceof_PDocMainViewer;
	private TextPaint menu_grid_painter;
	private boolean MainMenuListVis;
	
	ArrayList<String> menuList = new ArrayList<>();
	
	public static boolean MultiInstMode = false;
	final static Set<Object> instTidBucket = Collections.synchronizedSet(new HashSet<>());
	public static int singleInstCout;
	private boolean isSingleInst;
	private int BST;
	private boolean hasNoPermission;
	
	
	@Override
	public void onBackPressed() {
		if(currentViewer.tryClearSelection()) {
		} else {
			if(BST!=0) {
				Activity act = PDocShortCutActivity.blackSmithStack.get(BST);
				if(act!=null) {
					act.finish();
				}
			}
			if(isSingleInst) {
				//showT("单例");
				singleInstCout=0;
			}
			super.onBackPressed();
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Window win = getWindow();
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		boolean transit = Options.getTransitSplashScreen();
		if(!transit) setTheme(R.style.AppThemeRaw);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.activity_pdfviewer);
		root=UIData.root;
		currentViewer = UIData.wdv;
		
		try {
			currentViewer.dm=dm;
			
			currentViewer.a=this;
			
			currentViewer.setContextMenuView(UIData.contextMenu);
			
			if(transit){
				//root.setAlpha(0);
				closeSplashScreen();
			}
			
			currentViewer.setSelectionPaintView(UIData.sv);
			
			MenuBuilder context_menu = new MenuBuilder(this);
			getMenuInflater().inflate(R.menu.context_menu, context_menu);
			SpannableStringBuilder text = new SpannableStringBuilder();
			for (int i = 0; i < context_menu.size(); i++) {
				int start = text.length();
				MenuItem item = context_menu.getItem(i);
				text.append(item.getTitle());
				text.setSpan(new ClickableSpan() {
					@Override
					public void updateDrawState(TextPaint ds) {
					
					}
					@Override
					public void onClick(@NonNull View widget) {
						OnMenuClicked(item);
					}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.append("  ");
			}
			UIData.contextMenu.setText(text, TextView.BufferType.SPANNABLE);
			
			
			UIData.contextMenu.setOnClickListener(CMN.XYTouchRecorder());
			
			UIData.contextMenu.setOnTouchListener(CMN.XYTouchRecorder());
			
			Utils.setOnClickListenersOneDepth(UIData.bottombar2, this, 1, null);
			
			if(transit){
				currentViewer.setImageReadyListener(() -> {
					root.post(this::closeSplashScreen);
					currentViewer.setImageReadyListener(null);
				});
			}
			
			currentViewer.setImageReadyListener(() -> {
				root.post(() -> UIData.mainProgressBar.setVisibility(View.GONE));
				currentViewer.setImageReadyListener(null);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		requireStorage = true;
		checkLog(savedInstanceState);
		
		menuList.add("单例模式");
		menuList.add("选段翻译");
		menuList.add("高亮查词");
		
		TwoWayGridView mainMenuLst = UIData.mainMenuLst;
		mainMenuLst.setHorizontalSpacing(0);
		mainMenuLst.setVerticalSpacing(0);
		mainMenuLst.setHorizontalScroll(true);
		mainMenuLst.setStretchMode(GridView.NO_STRETCH);
		MenuAdapter menuAda = new MenuAdapter();
		mainMenuLst.setAdapter(menuAda);
		mainMenuLst.setOnItemClickListener(menuAda);
		mainMenuLst.setScrollbarFadingEnabled(false);
		mainMenuLst.setSelector(getResources().getDrawable(R.drawable.listviewselector0));
		
		mainMenuLst.post(() -> mainMenuLst.setTranslationY(mainMenuLst.getHeight()-UIData.bottombar2.getHeight()));
		
		menu_grid_painter = DescriptiveImageView.createTextPainter();
		
		Intent intent = getIntent();
		
		if(intent.getBooleanExtra("sin", false)) { // I am single, remove dead history
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
				List<ActivityManager.AppTask> tasks = am.getAppTasks();
				CMN.Log("tasks", tasks);
				int taskId = getTaskId();
				if(instTidBucket.size()>0) {
					for (int i = 0; i < tasks.size(); i++) {
						ActivityManager.AppTask appTask = tasks.get(i);
						int id = appTask.getTaskInfo().id;
						CMN.Log("隐藏了???", id);
						if(id==-1 || id!=taskId&&instTidBucket.remove(id)){
							//appTask.setExcludeFromRecents(true);
							appTask.finishAndRemoveTask();
							CMN.Log("隐藏了");
						}
					}
					//instTidBucket.clear();
				}
				CMN.Log("启动???", taskId, instTidBucket.size());
				instTidBucket.add(taskId);
			}
			isSingleInst=true;
			singleInstCout=1;
		}
		
		processBST(getIntent());
		
		systemIntialized = true;
	}
	
	@Override
	protected void further_loading(Bundle savedInstanceState) {
		super.further_loading(savedInstanceState);
		processIntent(getIntent(), true, hasNoPermission);
	}
	
	private void processBST(Intent intent) {
		BST = intent.getIntExtra("BST", 0);
		
		if(BST!=0) { // remove the background splash screen.
			Activity act = PDocShortCutActivity.blackSmithStack.get(BST);
			if(act!=null) {
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					List<ActivityManager.AppTask> tasks = am.getAppTasks();
					if (instTidBucket.size() > 0) {
						for (int i = 0; i < tasks.size(); i++) {
							ActivityManager.AppTask appTask = tasks.get(i);
							if (appTask.getTaskInfo().id == BST) {
								appTask.finishAndRemoveTask();
								break;
							}
						}
					}
				}
				//act.finish();
				//showT("act.finish()");
			}
		}
	}
	
	public void toggleMainMenuList() {
		int TargetTransY = -UIData.bottombar2.getHeight();
		if(MainMenuListVis) { // 隐藏
			TargetTransY = TargetTransY + UIData.mainMenuLst.getHeight();
			MainMenuListVis=false;
		} else { // 显示
			MainMenuListVis=true;
			UIData.mainMenuLst.setVisibility(View.VISIBLE);
		}
		UIData.mainMenuLst
				.animate()
				.translationY(TargetTransY)
				.setDuration(220)
				.start();
	}
	
	static class MenuItemViewHolder {
		private final DescriptiveImageView tv;
		public MenuItemViewHolder(View convertView) {
			tv = convertView.findViewById(R.id.text);
		}
	}
	
	//for menu list
	public class MenuAdapter extends BaseAdapter implements TwoWayAdapterView.OnItemClickListener
	{
		@Override
		public int getCount() {
			return menuList.size();
		}
		
		@Override
		public View getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItemViewHolder holder;
			if(convertView==null) {
				convertView = getLayoutInflater().inflate(R.layout.menu_item, parent, false);
				convertView.setTag(holder=new MenuItemViewHolder(convertView));
				holder.tv.textPainter = menu_grid_painter;
			} else {
				holder = (MenuItemViewHolder) convertView.getTag();
			}
			holder.tv.setText(menuList.get(position));
			return convertView;
		}
		
		@Override
		public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
			switch (position) {
				case 0:{
					MultiInstMode = !MultiInstMode;
					menuList.set(0, MultiInstMode ?"多实例模式":"单例模式");
					notifyDataSetChanged();
				} break;
				case 1:{
					currentViewer.enlargeSelection(true);
					translateSelection(null, false);
				} break;
				case 2: {
					if(currentViewer.shouldDrawSelection()) {
						String text = currentViewer.getSelection();
						if(currentViewer.hasSelection()) {
							currentViewer.highlightSelection();
						}
						colorForWord(text);
					}
				} break;
			}
		}
	}
	
	private void processIntent(Intent intent, boolean 人生若只如初见, boolean 不如不见) {
		Uri uri = intent.getData();
		if(!currentViewer.isDocTheSame(uri)) {
			if(!不如不见) {
				uri = Utils.getSimplifiedUrl(this, intent.getData());
			}
			CMN.Log("processIntent", intent, uri);
			if(uri!=null) {
				if(人生若只如初见) {
					String file_path = uri.getPath();
					int ret=100;
					if(!TextUtils.isEmpty(file_path)
							&& (ret=FU.checkSdcardPermission(this, new File(file_path), R.string.pls_pick_permission, 666, uri))==-1) {
						return;
					}
					CMN.Log("人生若只如初见", file_path, ret);
				}
				if(currentViewer.setDocumentUri(uri))
				if (!this_instanceof_PDocMainViewer && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
					ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
							new File(uri.getPath()).getName(),//title
							BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),//图标
							ResourcesCompat.getColor(getResources(), R.color.colorPrimary,
									getTheme()));
					setTaskDescription(taskDesc);
				}
			} else { //tg
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/Gpu Pro 1.pdf");
				
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/YotaSpec02.pdf"); // √
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/sample.pdf");
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/sig-notes.pdf");
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/sig-notes-new-txt.pdf");
				
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/sig-notes-t.pdf");
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/tmp.pdf");
				//currentViewer.setDocumentPath("/storage/emulated/0/myFolder/sig-notes-new-txt-page0.pdf");
				currentViewer.setDocumentPath("/storage/emulated/0/myFolder/1.pdf");
			}
		}
		PDFPageParms pageParms = parsePDFPageParms(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		CMN.Log("onActivityResult", resultCode==RESULT_OK, data, data!=null&&data.hasExtra("ASD"));
		if(requestCode==666) {
			if(resultCode==RESULT_OK&&data!=null) {
				Uri treeUri = data.getData();
				if(treeUri!=null) {
					int GRANTFLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
					grantUriPermission(getPackageName(), treeUri, GRANTFLAGS);
					getContentResolver().takePersistableUriPermission(treeUri, GRANTFLAGS);
				}
				CMN.Log("treeUri", treeUri);
			}
			processIntent(getIntent(), false, false);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode==321) {
			hasNoPermission = grantResults[0] != PackageManager.PERMISSION_GRANTED;
			further_loading(null);
		}
	}
	
	private void closeSplashScreen() {
		root.setAlpha(0);
		ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(root, "alpha", 0, 1);
		fadeInContents.setInterpolator(new AccelerateDecelerateInterpolator());
		fadeInContents.setDuration(350);
		fadeInContents.addListener(new Animator.AnimatorListener() {
			@Override public void onAnimationStart(Animator animation) { }
			@Override public void onAnimationEnd(Animator animation) {
				getWindow().setBackgroundDrawable(null);
			}
			@Override public void onAnimationCancel(Animator animation) { }
			@Override public void onAnimationRepeat(Animator animation) { }
		});
		root.post(fadeInContents::start);
	}
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		super.onConfigurationChanged(newConfig);
		if(!systemIntialized) return;
		if(mConfiguration.orientation!=newConfig.orientation) {
		
		}
		mConfiguration.setTo(newConfig);
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser_widget10: {
				if(InvalidSate()) break;
				int id = WeakReferenceHelper.top_menu;
				BookMarksFragment bmks = (BookMarksFragment) getReferencedObject(id);
				if(bmks==null) {
					putReferencedObject(id, bmks=new BookMarksFragment(dm));
				} else if(bmks.isAdded()) {
					break;
				}
				bmks.resizeLayout(false);
				bmks.show(getSupportFragmentManager(), "bkmks");
			} break;
			case R.id.browser_widget11: {
				toggleMainMenuList();
			} break;
		}
	}
	
	private boolean InvalidSate() {
		if(currentViewer.pdoc==null) {
			showT("No Opened Document !");
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(hidingContextMenu) {
			currentViewer.showContextMenuView();
			hidingContextMenu=false;
		}
		if(!this_instanceof_PDocMainViewer) {
			PDFPageParms pageParms = parsePDFPageParms(null);
			if(pageParms!=null) {
				currentViewer.navigateTo(pageParms);
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(PDocument.SavingScheme==PDocument.SavingScheme_AlwaysSaveOnPause) {
			currentViewer.checkDoc(this, false, true);
		}
		if(hidingContextMenu) {
			currentViewer.hideContextMenuView();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(PDocument.SavingScheme==PDocument.SavingScheme_SaveOnClose) {
			currentViewer.checkDoc(this, false, false);
		}
		currentViewer.setDocumentUri(null);
		if(isSingleInst) {
			singleInstCout=0;
		}
	}
	
	@SuppressLint("NonConstantResourceId")
	public void OnMenuClicked(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ctx_copy:{
				String text = getSelection();
				if(text!=null) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
					clipboard.setPrimaryClip(ClipData.newPlainText("POLYM", text));
					showT("已复制！");
					currentViewer.clearSelection();
				}
			} break;
			case R.id.ctx_hightlight:{
				currentViewer.highlightSelection();
			} break;
			case R.id.ctx_enlarge:{
				currentViewer.enlargeSelection(false);
			} break;
			case R.id.ctx_share:{
				shareUrlOrText(getSelection());
			} break;
			case R.id.ctx_dictionay:{
				if(currentViewer.shouldDrawSelection()) {
					colorForWord(null);
				}
			} break;
			case R.id.ctx_translation:{
				translateSelection(null, false);
			} break;
		}
	}
	
	private void colorForWord(String word) {
		if(word==null) {
			word = getSelection();
		}
		if(word!=null) {
			if(false) {
				Intent intent = new Intent("colordict.intent.action.SEARCH");
				intent.putExtra("EXTRA_QUERY", word);
				//hidingContextMenu=true;
				startActivity(intent);
			} else {
				translateSelection(word, true);
			}
		}
	}
	
	private void translateSelection(String phrase, boolean processText) {
		if(phrase==null) {
			phrase = getSelection();
		}
		if(phrase!=null) {
			String Action=processText?Intent.ACTION_PROCESS_TEXT:Intent.ACTION_SEND;
			String Extra=processText?Intent.EXTRA_PROCESS_TEXT:Intent.EXTRA_TEXT;
			Intent intent = new Intent(Action);
			intent.setType("text/plain");
			try {
				intent.setPackage(GoogleTranslate);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(Extra, phrase);
				startActivity(intent);
			} catch (Exception e) {
				showT(R.string.gt_no_inst);
			}
		}
	}
	
	private String getSelection() {
		String ret = currentViewer.getSelection();
		if(true && ret!=null) {
			ret = ret.replace("\r\n", " ");
		}
		return ret;
	}
	
	private void shareUrlOrText(String selection) {
		//CMN.Log("menu_icon6menu_icon6");
		//CMN.rt("分享链接……");
		int id = WeakReferenceHelper.share_dialog;
		BottomSheetDialog dlg = (BottomSheetDialog) getReferencedObject(id);
		if(dlg==null) {
			putReferencedObject(id, dlg=new AppIconsAdapter(this).shareDialog);
		}
		//CMN.pt("新建耗时：");
		AppIconsAdapter shareAdapter = (AppIconsAdapter) dlg.tag;
		shareAdapter.pullAvailableApps(this, null, selection);
		//CMN.pt("拉取耗时：");
	}
	
	final static SparseArray<PDFPageParms> PDFPageParmsMap = new SparseArray<>();
	
	PDFPageParms parsePDFPageParms(Intent intent) {
		PDFPageParms ret;
		if(true) {
			ret = parsePDFPageParmsFromIntent(intent);
			if(ret!=null) {
				return ret;
			}
		}
		ret = PDFPageParmsMap.get(getTaskId());
		if(ret!=null) {
			PDFPageParmsMap.remove(getTaskId());
		}
		return ret;
	}
	
	static PDFPageParms parsePDFPageParmsFromIntent(Intent intent) {
		if(intent!=null && intent.hasExtra("PPP")) {
			return new PDFPageParms( intent.getIntExtra("p", 0)
					, intent.getIntExtra("x", 0)
					, intent.getIntExtra("y", 0)
					, intent.getFloatExtra("s", 0) );
		}
		return null;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//if(this_instanceof_PDocMainViewer)
		{
			Uri newUri = intent.getData();
			if(newUri!=null) {
				setIntent(intent);
				PDFPageParms pageParms = parsePDFPageParms(intent);
				showT("新的来啦！");
				processIntent(intent, true, false);
			}
		}
		processBST(intent);
	}
}
