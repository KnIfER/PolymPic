package com.knziha.polymer.widgets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.WeakReferenceHelper;
import com.knziha.polymer.browser.AppIconCover.AppIconCover;
import com.knziha.polymer.databinding.LoginViewBinding;
import com.knziha.polymer.preferences.NavHomeEditorDialogSettings;
import com.knziha.polymer.webstorage.BrowserAppPanel;
import com.shockwave.pdfium.treeview.TreeViewNode;

import org.adrianwalker.multilinestring.Multiline;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Pattern;

/** 树形导航视图管理器，类似于桌面浏览器的收藏夹，自带文件夹视图。 | A Bookmark-like Tree View based Navigation Manager */
public class NavigationManager extends BrowserAppPanel implements NavViewListener {
	View root;
	RecyclerView recyclerView;
	NavigationHomeAdapter navAdapter;
	private EditTextmy etSearch;
	
	WeakReference<NavFolderFragment> navFolderRef = new WeakReference<>(null);
	
	public NavigationManager(BrowserActivity a) {
		super(a);
	}
	
	@Override
	protected void init(Context context, ViewGroup root) {
		showPopOnAppbar = true;
		
		a = (BrowserActivity) context;
		navAdapter = new NavigationHomeAdapter(new File(context.getExternalFilesDir(null), "Bookmarks.json"), this);
		root = (ViewGroup) a.inflater.inflate(R.layout.nav_view, a.UIData.webcoord, false);
		//root.setPadding(0, 0, 0, bottomPaddding);
		ViewGroup nav_header = root.findViewById(R.id.nav_header);
		Utils.setOnClickListenersOneDepth(nav_header, this, 1, null);
		etSearch = nav_header.findViewById(R.id.etSearch);
		View clearText = root.findViewById(R.id.clearText);
		etSearch.bNeverBlink = true;
		etSearch.setOnFocusChangeListener((v, hasFocus)
				-> clearText.setVisibility(hasFocus&&etSearch.getText().length()>0?View.VISIBLE:View.GONE));
		Runnable postFilterRunnable = ()-> {
			String text = etSearch.getText().toString();
			Object pattern=text;
			if(text.length()==0) {
				text=null;
			}
			int schFlag = 0;
			if(text!=null) {
				boolean bUseRegex = false;
				if ((navAdapter.currentSchFlg&0x3)!=0) { // 不区分大小写
					text = text.toLowerCase();
				}
				if (bUseRegex) {
					try {
						String patStr = text;
						if ((schFlag&0x5)!=0 && text.charAt(text.length()-1)!='\b') { // 全词匹配
							patStr = "\b"+text+"\b";
						}
						if ((schFlag&0x4)!=0 && text.charAt(0)!='^') { // 从头匹配
							patStr = "^"+text;
						}
						pattern = Pattern.compile(patStr);
					} catch (Exception ignored) { }
				} else {
					if ((schFlag&0x5)!=0) {
						pattern = Pattern.compile("\b"+text+"\b");
					}
				}
			}
			navAdapter.filterTreeView(pattern, schFlag);
		};
		etSearch.addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged(Editable s) {
				clearText.setVisibility(etSearch.hasFocus()&&etSearch.getText().length()>0?View.VISIBLE:View.GONE);
				etSearch.removeCallbacks(postFilterRunnable);
				etSearch.postDelayed(postFilterRunnable, 90);
			}
		});
		clearText.setOnClickListener(this);
		navAdapter.SetRecyclerView(recyclerView = root.findViewById(R.id.recycler_view));
		if (getEditMode()) {
			toggleEditMode();
			nav_header.findViewById(R.id.edit).performClick();
		} else if (getShowDragHandle()) {
			navAdapter.InitDragSort();
		}
		settingsLayout = root;
		bottomPadding *= 2;
	}
	
	@Override
	protected void decorateInterceptorListener(boolean install) {
		if (install) {
			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess_ic_back);
			a.UIData.browserWidget8.setImageResource(R.drawable.ic_baseline_book_24);
		} else {
			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess);
			a.UIData.browserWidget8.setImageResource(R.drawable.chevron_forward);
		}
	}
	
	final ArrayList<NavigationNode> NewNodeInsertionPath = new ArrayList<>();
	boolean NewNodeInsertFolder;
	
	void inflateNewNodePath(NavigationNode node) {
		NewNodeInsertionPath.clear();
		if (node!=null) {
			NewNodeInsertionPath.add(node);
			TreeViewNode p=node;
			while((p=p.getParent())!=null) {
				NewNodeInsertionPath.add((NavigationNode) p);
			}
		}
	}
	
	NavigationNode getNewNodeInsertAfter(boolean get_folder) {
		NavigationNode n;
		for (int i = 0,len=NewNodeInsertionPath.size(); i<len; i++) {
			n = NewNodeInsertionPath.get(i);
			if(n.getParent()!=null && (!get_folder||!n.isLeaf())) {
				return n;
			}
		}
		return navAdapter.navRoot;
	}
	
	public void InsertNavNode(String url, String title) {
		NewNodeInsertFolder = false;
		navAdapter.activeNavHomeBean = null;
		showEditorDlg(navAdapter, null, url, title);
	}
	
	@Override
	public void showEditorDlg(NavigationHomeAdapter navAdapter, NavigationNode editing, String url, String title) {
		navAdapter.editingNavHomeBean = editing;
		AlertDialog editNavHomeDlg = (AlertDialog) a.getReferencedObject(WeakReferenceHelper.edit_navhome);
		if(editNavHomeDlg==null || (editNavHomeDlg.tag1!=navAdapter && editNavHomeDlg.isShowing())) {
			LoginViewBinding schEngEditView = LoginViewBinding.inflate(a.getLayoutInflater(), a.root, false);
			AlertDialog d = new AlertDialog
					.Builder(a)
					.setView(schEngEditView.getRoot())
					.setTitle("编辑导航节点")
					.setOnDismissListener(dialog -> {
						schEngEditView.url.setText(null);
						schEngEditView.name.setText(null);
						((NavigationHomeAdapter)((AlertDialog)dialog).tag1).editingNavHomeBean=null;
//						if (settingsPanel!=null) {
//							settingsPanel.hide();
//						}
					})
					.setPositiveButton("确认", null)
					.setNegativeButton("取消", null)
					.setNeutralButton("设置", null)
					.show();
			d.tag = schEngEditView;
			View.OnClickListener dlgLis = v -> {
				switch (v.getId()) {
					case android.R.id.button1: {
						String new_url = Utils.getFieldInView(schEngEditView.url);
						String new_name = Utils.getFieldInView(schEngEditView.name);
						if (InsertOrModifyNode(navAdapter.editingNavHomeBean, new_url, new_name)) {
							d.dismiss();
						}
					} break;
					case android.R.id.button2: {
						NavHomeEditorDialogSettings settingsPanel = (NavHomeEditorDialogSettings) schEngEditView.getRoot().getTag();
						if (settingsPanel!=null && settingsPanel.isVisible()) {
							settingsPanel.hide();
						} else {
							d.dismiss();
						}
					} break;
					case android.R.id.button3: {
						ViewGroup dRoot = d.findViewById(android.R.id.content);
						//dRoot = (ViewGroup) dRoot.getParent().getParent();
						NavHomeEditorDialogSettings shimObj = (NavHomeEditorDialogSettings) schEngEditView.getRoot().getTag();
						if (shimObj==null) {
							shimObj = new NavHomeEditorDialogSettings(
									a
									, dRoot
									, v.getHeight()
									, a.opt
									, navAdapter);
							schEngEditView.getRoot().setTag(shimObj);
						}
						shimObj.navigationHomeAdapter = (NavigationHomeAdapter) d.tag1;
						shimObj.toggle(dRoot, null);
					} break;
					case R.id.alertTitle: {
						if (navAdapter.activeNavHomeBean!=null) {
							showPopupMenu((NavigationHomeAdapter) d.tag1, v);
						}
					} break;
					case R.id.folder: {
						a.showT("此处逻辑过于复杂，暂不实现！");
						//showFolderView(1);
					} break;
					case R.id.url_morpt:
					case R.id.name_morpt: {
						ViewGroup editView = (ViewGroup) v.getParent();
						FrameLayout panelView = (FrameLayout) d.findViewById(R.id.action_bar_root).getParent();
						EditFieldHandler editFieldHandler = (EditFieldHandler) a.getReferencedObject(WeakReferenceHelper.edit_field);
						if(editFieldHandler==null) {
							a.putReferencedObject(WeakReferenceHelper.edit_field, editFieldHandler = new EditFieldHandler(a, panelView));
						}
						d.setCancelable(false);
						editFieldHandler.showForEditText(panelView
								, (EditText) editView.getChildAt(0)
								,d
								,editView.getTop()+d.findViewById(R.id.topPanel).getHeight()
								,(int) (8* GlobalOptions.density));
					} break;
				}
			};
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(dlgLis);
			d.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(dlgLis);
			View btn = d.getButton(DialogInterface.BUTTON_NEUTRAL);
			btn.setOnClickListener(dlgLis);
			ButtonBarLayout bp = ((ButtonBarLayout) btn.getParent());
			bp.setAllowStacking(false);
			View folderButton = a.getLayoutInflater().inflate(R.layout.pick_folder_button, bp, false);
			View folderText = folderButton.findViewById(R.id.folder);
			folderText.setOnClickListener(dlgLis);
			//Utils.addViewToParent(folderButton, bp, 1);
			Utils.replaceView(folderButton, bp.findViewById(R.id.spacer), false);
			d.getTitleView().setOnClickListener(dlgLis);
			schEngEditView.nameMorpt.setOnClickListener(dlgLis);
			schEngEditView.urlMorpt.setOnClickListener(dlgLis);
			if(getAlwaysShowMultilineEditField()) {
				schEngEditView.url.setSingleLine(false);
				schEngEditView.name.setSingleLine(false);
			}
			schEngEditView.name.setTag(folderText);
			a.putReferencedObject(WeakReferenceHelper.edit_navhome, editNavHomeDlg=d);
		}
		editNavHomeDlg.tag1 = navAdapter;
		LoginViewBinding schEngEditView = (LoginViewBinding) editNavHomeDlg.tag;
		TextView folderText = (TextView) schEngEditView.name.getTag();
		if(editing!=null) {
			url = editing.getUrl();
			title = editing.getName();
		} else if(url==null){
			url=a.currentWebView.getUrl();
			title=a.currentWebView.getTitle();
		}
		folderText.setText(getNewNodeInsertAfter(true).getName());
		if (editing!=null && !editing.isLeaf() || editing==null && NewNodeInsertFolder) {
			schEngEditView.url.setEnabled(false);
			schEngEditView.urlMorpt.setVisibility(View.INVISIBLE);
			url = "文件夹";
		} else {
			schEngEditView.url.setEnabled(true);
			schEngEditView.urlMorpt.setVisibility(View.VISIBLE);
		}
		schEngEditView.url.setText(url);
		schEngEditView.name.setText(title);
		editNavHomeDlg.setTitle(editing==null?"添加导航节点":"编辑导航节点");
		editNavHomeDlg.show();

//		Window window = editNavHomeDlg.getWindow();
//		WindowManager.LayoutParams lp = window.getAttributes();
//		lp.dimAmount =0f;
//		window.setAttributes(lp);
	}
	
	public void showPopupMenu(NavigationHomeAdapter navAdapter, View headerView) {
		PopupMenuHelper popupMenu = a.getPopupMenu();
		boolean isFolderView = headerView!=null||navAdapter.getIsFolderView();
		final int tag = isFolderView?R.string.xinjianwenjianjia:R.string.duoxuanmoshi;
		if (popupMenu.tag!=tag) {
			final int[] texts = isFolderView?new int[]{
					R.string.xinjianwenjianjia
					,R.string.xinjiandaohangjiedian
					,R.string.bianji
					,R.string.delete
			} : new int[] {
					//R.string.duoxuanmoshi,
					R.string.houtaidakai
					,R.string.xinbiaoqianyedaikai
					,R.string.sheweimorenye
					,R.string.fuzhilianjie
					,R.layout.menu_edit_and_delete
					,R.string.share
			};
			PopupMenuHelper.PopupMenuListener listener;
			if (popupMenu.tag==R.string.xinjianwenjianjia || popupMenu.tag==R.string.duoxuanmoshi) {
				listener = popupMenu.getListener();
			}
			else {
				listener = (popupMenuHelper, v, isLongClick) -> {
					boolean ret=true;
					boolean dismiss = !isLongClick;
					//NavigationNode node = (NavigationNode) displayNodes.get(longClickView.getLayoutPosition());
					NavigationNode node = navAdapter.activeNavHomeBean;
					int index = navAdapter.activeNavHomeBeanPos;
					boolean isFolder = !node.isLeaf();
					View blinkView = null;
					switch (v.getId()) {
//					case R.string.duoxuanmoshi:{ // 多选模式
//						if (toggleSelMode()) {
//							if (!selection.contains(node)) {
//								selection.add(node);
//								markIntervalSel(node, longClickView.getLayoutPosition());
//							}
//						}
//						notifyDataSetChanged();
//					} break;
						case R.string.houtaidakai:{ // 后台打开
							if (isFolder) blinkView=v;
							else a.newTab(node.getUrl(), true, true, -1);
						} break;
						case R.string.xinbiaoqianyedaikai:{ // 新标签页
							if (isFolder) blinkView=v;
							else a.newTab(node.getUrl(), false, true, -1);
						} break;
						case R.string.sheweimorenye:{ // 默认页
							if (isFolder) blinkView=v;
						} break;
						case R.string.xinjianwenjianjia:{ // 新建文件夹
							NewNodeInsertFolder = true;
							inflateNewNodePath(node);
							showEditorDlg(navAdapter, null, "", node.getName());
						} break;
						case R.id.new_node:{ // 新建导航节点
							NewNodeInsertFolder = false;
							inflateNewNodePath(node);
							showEditorDlg(navAdapter, null, null, null);
						} break;
						case R.string.xinjiandaohangjiedian:{ // 新建导航节点
							NewNodeInsertFolder = false;
							inflateNewNodePath(node);
							showEditorDlg(navAdapter,null, "", "");
						} break;
						case R.string.bianji:{ // 编辑
							showEditorDlg(navAdapter, node, null, null);
						} break;
						case R.id.delete:
						case R.string.delete:{ // 删除
							String title = "确认删除";
							String name = node.getName();
							if (name.length()>=6) {
								title += name.substring(0, 5)+"... ";
							} else {
								title += name;
							}
							int delSz = 1;
							if (getSelMode() && navAdapter.selection.contains(node)) {
								delSz = navAdapter.selection.size();
							}
							int folderCnt;
							if (delSz>1) {
								title += "等"+delSz+"个节点吗？";
								folderCnt = navAdapter.folderSelCnt;
							} else {
								title += "吗？";
								folderCnt = node.isLeaf()?0:1;
							}
							String msg = folderCnt==0?a.mResource.getString(R.string.cancel)
									:("含"+folderCnt+"个文件夹！");
							AlertDialog.Builder builder2 = new AlertDialog.Builder(a);
							builder2.setTitle(title)
									.setPositiveButton(R.string.delete, (dialog, which) -> {
										navAdapter.deleteSel();
									})
									.setNegativeButton(msg, null)
							;
							AlertDialog dTmp = builder2.create();
							dTmp.show();
							((TextView)dTmp.findViewById(R.id.alertTitle)).setSingleLine(false);
							Window window = dTmp.getWindow();
							WindowManager.LayoutParams lp = window.getAttributes();
							lp.dimAmount =0f;
							window.setAttributes(lp);
							if (opt.getNeedCntNavNodesToDel() && folderCnt>0) {
								NavigationNodeCntTask loader = new NavigationNodeCntTask(navAdapter.selection.toArray(new TreeViewNode[delSz])
										, a.mResource
										, dTmp.findViewById(android.R.id.button2)
										, R.string.has_number_of_nodes);
								Glide.with(a)
									.load(new AppIconCover(loader))
									.skipMemoryCache(true)
									.diskCacheStrategy(DiskCacheStrategy.NONE)
									.listener(new RequestListener<Drawable>() {
										@Override
										public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
											return false;
										}
										@Override
										public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
											return true;
										}
									}).into((ImageView) dTmp.findViewById(android.R.id.icon));
							}
						} break;
						case R.string.share:{ // 分享
							if (isFolder) blinkView=v;
							else a.shareUrlOrText(node.getUrl(), null);
						} break;
					}
					if (blinkView!=null) {
						Utils.blinkView(blinkView, false);
					} else if (dismiss) {
						popupMenuHelper.postDismiss(80);
					}
					return ret;
				};
			}
			popupMenu.initLayout(texts, listener);
			popupMenu.tag=tag;
		}
		if (headerView!=null) {
			popupMenu.show(headerView, 0, 0);
		} else if (isFolderView) {
			popupMenu.show(navAdapter.recyclerView, navAdapter.recyclerView.mLastTouchX, navAdapter.recyclerView.mLastTouchY);
		} else {
			int[] vLocationOnScreen = new int[2];
			recyclerView.getLocationOnScreen(vLocationOnScreen);
			popupMenu.show(a.root, recyclerView.mLastTouchX+vLocationOnScreen[0], recyclerView.mLastTouchY+vLocationOnScreen[1]);
		}
		Utils.preventDefaultTouchEvent(root, -100, -100);
	}
	
	private boolean InsertOrModifyNode(NavigationNode editing, String new_url, String new_name) {
		boolean isFolder = NewNodeInsertFolder;
		isFolder = editing!=null&&!editing.isLeaf() || editing==null&&isFolder;
		if (!isFolder) {
			if (TextUtils.isEmpty(new_url)) {
				a.showT("地址不能为空");
				return false;
			}
			if (editing==null && navAdapter.navRoot.url_table.containsKey(new_url)) {
				a.showT("已存在此URL地址");
				return false;
			}
		}
		if (isFolder) {
			if (TextUtils.isEmpty(new_name)) {
				a.showT("名称不能为空");
				return false;
			}
			new_url = null;
		}
		if(editing!=null) {
			if (TextUtils.equals(new_url, editing.getUrl()) && TextUtils.equals(new_name, editing.getName())) {
				return true;
			}
			editing.setUrlAndName(new_url, new_name);
			if(isVisible()) navAdapter.notifyDataSetChanged();
		} else {
			NavigationNode insertAfter = getNewNodeInsertAfter(false);
			NavigationNode np = (NavigationNode) insertAfter.getParent();
			int index;
			if (!insertAfter.isLeaf() && insertAfter.isExpand(navAdapter.currentExpChannel)) {
				np = insertAfter;
				index = Integer.MAX_VALUE;
			} else {
				index = np.getChildList().indexOf(insertAfter) + 1;
			}
			np.addNewChild(new_url, new_name, index);

			if(isVisible()) navAdapter.refreshList(false);
		}
		navAdapter.MarkDirty();
		return true;
	}
	
	/** @param  source: 0=nav; 1=pick folder; */
	public void showFolderView(int source) {
		NavFolderFragment navFolderFragment=navFolderRef.get();
		if(navFolderFragment==null) {
			navFolderFragment = new NavFolderFragment();
			navFolderFragment.dm = a.dm;
			navFolderFragment.foldViewAdapter = new NavigationHomeAdapter(navAdapter);
			navFolderRef = new WeakReference<>(navFolderFragment);
			navAdapter.folderViewDirty = false;
		}
		navFolderFragment.bPickFolder = source==1;
		if (!navFolderFragment.isAdded()) {
			navFolderFragment.resizeLayout(false);
			navFolderFragment.show(a.getSupportFragmentManager(), "vfld");
			if (navAdapter.folderViewDirty) {
				navFolderFragment.foldViewAdapter.refreshList(false);
			}
		}
	}
	
	RequestBuilder<Drawable> glideSaver;
	
	private void checkDirty() {
		if (navAdapter.navRoot.isDirty) {
			if (glideSaver==null) {
				glideSaver = Glide.with(a)
						.load(navAdapter.navRoot.saveTask)
						.skipMemoryCache(true)
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.listener(new RequestListener<Drawable>() {
							@Override
							public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
								a.showT("保存失败，请检查磁盘是否已写满！");
								a.lastError = e;
								CMN.Log(e);
								return false;
							}
							
							@Override
							public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
								//a.showT("保存成功！");
								a.animateHomeIcon();
								return true;
							}
						});
			}
			glideSaver.into(a.UIData.browserWidget9);
//			try {
//			} catch (Exception e) {
//				CMN.Log(e);
//				a.lastError = e;
//				a.showT("保存失败，请检查磁盘是否写满！");
//			}
		}
	}
	
	
	public boolean dismiss(boolean clearSel) {
		if(bIsShowing) {
			if (recyclerView instanceof DragSelectRecyclerView && ((DragSelectRecyclerView) recyclerView).getDragSelectActive()) {
				return true;
			}
			if (clearSel && getSelMode() && navAdapter.selection.size()>0
			) {
				navAdapter.clearSel();
				//toggleSelMode();
			} else {
				checkDirty();
				toggle(a.root, null);
			}
			return true;
		}
		return false;
	}
	
	// click
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.browser_widget8:
				a.mInterceptorListenerHandled = true;
				showFolderView(0);
				break;
			case R.id.browser_widget9:
				a.mInterceptorListenerHandled = true;
				dismiss();
				break;
			case R.id.ivBack:
				if (bIsShowing) {
					dismiss(false);
				}
				break;
			case R.id.search:
				
				break;
			/* 编辑模式 */
			case R.id.edit: {
				LayerDrawable ld = (LayerDrawable) view.getTag();
				if(ld==null) {
					view.setBackgroundResource(R.drawable.frame_checked_layer);
					view.setTag(ld=(LayerDrawable) view.getBackground());
				}
				boolean editMode = toggleEditMode();
				if (editMode) {
					ld.jumpToCurrentState();
				}
				a.showT(editMode?"编辑模式":"浏览模式");
				if(editMode) {
					navAdapter.InitDragSort();
				}
				if (!getShowDragHandle()) {
					navAdapter.notifyDataSetChanged();
				}
				ld.getDrawable(1).setAlpha(editMode?255:0);
			} break;
			/* 下拉菜单 */
			case R.id.ivOverflow:{
				PopupMenuHelper popupMenu = a.getPopupMenu();
				if (popupMenu.tag!=R.layout.level_12345) {
					final int[] texts = new int[] {
							R.string.duoxuanmoshi
							,R.string.hangjianlianxuan
							,R.string.foldAll
							,R.layout.level_12345
							,R.string.expAll
							,R.layout.level_12345
							,R.string.showMultilineText
							,R.string.allowDragSort
					};
					PopupMenuHelper.PopupMenuListener listener = (popupMenuHelper, v, isLongClick) -> {
						boolean ret=true;
						boolean dismiss = !isLongClick;
						switch (v.getId()) {
							case R.string.duoxuanmoshi:{
								v.setActivated(toggleSelMode());
								navAdapter.notifyDataSetChanged();
							} break;
							case R.string.showMultilineText:{
								v.setActivated(toggleShowMultilineText());
								navAdapter.notifyDataSetChanged();
							} break;
							case R.string.allowDragSort:{
								boolean dragHandle = toggleShowDragHandle();
								v.setActivated(dragHandle);
								if (dragHandle) {
									navAdapter.InitDragSort();
								}
								navAdapter.notifyDataSetChanged();
							} break;
							case R.string.hangjianlianxuan: {
								View blinkView = null;
								if (!getSelMode()) {
									blinkView = popupMenuHelper.lv.getChildAt(0);
								} else if(!navAdapter.select_between_nodes()){
									blinkView = v;
								}
								if (blinkView!=null) {
									Utils.blinkView(blinkView, false);
									dismiss = false;
								}
							} break;
							case R.string.foldAll: {
								navAdapter.collapseAll();
							} break;
							case R.string.expAll: {
								navAdapter.expandAll();
							} break;
							case R.id.level: {
								View svp = (View) v.getParent();
								boolean collapse = Utils.getViewIndex(svp)==3;
								int level = Utils.getViewIndex(v)+1;
								navAdapter.collapseExpandToLevel(collapse, level);
								dismiss = false;
							} break;
						}
						if (dismiss) {
							popupMenuHelper.postDismiss(80);
						}
						return ret;
					};
					popupMenu.initLayout(texts, listener);
					popupMenu.tag=R.layout.level_12345;
				}
				popupMenu.modifyMenu(0, null,getSelMode());
				popupMenu.modifyMenu(6, null,getShowMultilineText());
				popupMenu.modifyMenu(7, null,getShowDragHandle());
				popupMenu.show(a.root, a.root.getWidth(), 0);
			} break;
			/* 清除文本 */
			case R.id.clearText:
				etSearch.setText(null);
				view.setVisibility(View.GONE);
				break;
			case R.id.search_go_btn: {
				NavigationHomeAdapter.ViewHolder viewHolder = (NavigationHomeAdapter.ViewHolder) Utils.getViewHolderInParents(view);
				NavigationNode node = viewHolder.node;
				//a.showT(node.getName());
				navAdapter.NavToFolder(node);
				navFolderRef.get().dismiss();
			} break;
//			case R.id.iv_arrow: {
//				performIvArrowClk(longClickView);
//			} break;
			case R.id.itemRoot: {
				super.onClick(view);
			} break;
		}
	}
	
	public void OnNavHomeEditorActions(int action) {
//		AlertDialog editNavHomeDlg = (AlertDialog) a.getReferencedObject(WeakReferenceHelper.edit_navhome);
//		if(editNavHomeDlg!=null) {
//			LoginViewBinding schEngEditView = (LoginViewBinding) editNavHomeDlg.tag;
//			int dismiss = 0;
//			switch (action) {
//				case 0: {
//					dismiss = 0x7;
//					if (editingNavHomeBean!=null && !editingNavHomeBean.isLeaf()) {
//						// 使用文件夹
//						if (bIsShowing && longClickView!=null) {
//							performIvArrowClk(longClickView);
//						}
//						dismiss = 0x1|0x2;
//					} else {
//						a.execBrowserGoTo(schEngEditView.url.getText().toString());
//					}
//				} break;
//				case 1:{
//					dismiss = 0x1;
//					schEngEditView.url.setText(a.currentWebView.getUrl());
//					schEngEditView.name.setText(a.currentWebView.getTitle());
//				} break;
//				case 2:{
//					dismiss = 0x1;
//					boolean sl = !getAlwaysShowMultilineEditField();
//					schEngEditView.url.setSingleLine(sl);
//					schEngEditView.name.setSingleLine(sl);
//				} break;
//			}
//			if((dismiss&0x1)!=0) {
//				NavHomeEditorDialogSettings settingsPanel = (NavHomeEditorDialogSettings) schEngEditView.getRoot().getTag();
//				settingsPanel.dismiss();
//			}
//			if((dismiss&0x2)!=0) {
//				editNavHomeDlg.dismiss();
//			}
//			if((dismiss&0x4)!=0) {
//				dismiss(false);
//			}
//		}
	}
	
	
	@Override
	public void UseNodeData(NavigationHomeAdapter navAdapter, TreeViewNode node) {
		if (this.navAdapter==navAdapter) {
			String url = ((JSONObject)node.getContent()).getString("url");
			if (url!=null) {
				a.execBrowserGoTo(url, true);
				dismiss();
			}
		} else {
			navAdapter.NavToFolder((NavigationNode) node);
			navFolderRef.get().dismiss();
		}
	}
	
	@Multiline(flagPos=58, shift=0) private boolean getAlwaysShowMultilineEditField(){ opt.FirstFlag=opt.FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=59, shift=1) private boolean getAppendNewNavNodeToEnd(){ opt.FirstFlag=opt.FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=60, shift=1) private boolean getAutoExpandAllFoldersForFov(){ opt.FirstFlag=opt.FirstFlag; throw new RuntimeException(); }

	@Multiline(flagPos=38) public boolean getShowMultilineText(){ opt.SecondFlag=opt.SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=38) private boolean toggleShowMultilineText(){ opt.SecondFlag=opt.SecondFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=39) public boolean getShowDragHandle(){ opt.SecondFlag=opt.SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=39) private boolean toggleShowDragHandle(){ opt.SecondFlag=opt.SecondFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=40) public boolean getSelMode(){ opt.SecondFlag=opt.SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=40) private boolean toggleSelMode(){ opt.SecondFlag=opt.SecondFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=41) public boolean getEditMode(){ opt.SecondFlag=opt.SecondFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=41) private boolean toggleEditMode(){ opt.SecondFlag=opt.SecondFlag; throw new IllegalArgumentException(); }
	
}
