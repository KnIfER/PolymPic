package com.knziha.polymer.webfeature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.DropDownListView;
import androidx.appcompat.widget.ListPopupWindow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.TestHelper;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.WeakReferenceHelper;
import com.knziha.polymer.databinding.LoginViewBinding;
import com.knziha.polymer.databinding.SearchEnginesItemBinding;
import com.knziha.polymer.widgets.EditFieldHandler;
import com.knziha.polymer.widgets.PopupBackground;
import com.knziha.polymer.widgets.SuperItemListener;
import com.knziha.polymer.widgets.Utils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.ViewAffordable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import static androidx.appcompat.app.GlobalOptions.realWidth;
import static com.knziha.polymer.browser.webkit.WebViewHelper.getTypedTagInAncestors;

public class WebDictsManager extends SuperItemListener implements AdapterView.OnItemLongClickListener {
	private final BrowserActivity a;
	ArrayList<WebDict> WebDicts = null;
	
	private ViewHolder editingView;
	private int editingPosition;
	
	private WebDict editingBean;
	private boolean isSyncShown;
	private boolean isDirty;
	
	private BaseAdapter mAdapter;
	private ListPopupWindow listPopupWindow;
	private PopupBackground layoutListener;
	
	WebDictsManager parentMngr = null;
	WeakReference<WebDictsManager> variantMngrRef = new WeakReference<>(null);
	
	private boolean isLongClicked;
	private boolean longClick;
//	private PopupWindow polypopup;
//	private View polypopupview;
//	private boolean polysearching;
	
	private View.OnTouchListener touchInterceptor;
	private DragSortController mController;
	private LayoutInflater inflater;
	private boolean mIsShowing;
	
	public WebDictsManager(BrowserActivity a) {
		this.a = a;
		
//		WebDicts = new  ArrayList<>(64);
//		TestHelper.testAddWebDicts(WebDicts);
//		a.currentWebDictUrl = WebDicts.get(0).url;
	}
	
	private WebDictsManager(WebDictsManager parentMngr, ArrayList<WebDict> webDicts) {
		this.a = parentMngr.a;
		this.parentMngr = parentMngr;
		this.WebDicts = webDicts;
	}
	
	private static class ViewHolder implements ViewAffordable {
		final SearchEnginesItemBinding itemViewData;
		final SuperItemListener event_listener;
		int position;
		WebDict webDictItem;
		View itemView;
		ViewGroup swipedView;
		View dragHandleView;
		
		public ViewHolder(SearchEnginesItemBinding binding, SuperItemListener event_listener) {
			this.itemViewData = binding;
			this.event_listener = event_listener;
			itemView = binding.getRoot();
			itemView.setTag(this);
			//itemView.setOnClickListener(event_listener);
		}
		
		public View AffordView(int id) {
			CMN.Log("AffordView::", dragHandleView);
			return webDictItem.isEditing?dragHandleView:null;
		}
		
		public void expandEditView(DragSortController mController, boolean expand, boolean animate) {
			if(animate) {
				webDictItem.isEditing = expand;
			}
			if(expand && swipedView==null) {
				ViewStub stub = itemViewData.moreOptions.getViewStub();
				if(stub!=null) {
					stub.setOnInflateListener(null);
					swipedView = (ViewGroup) stub.inflate();
					Utils.setOnClickListenersOneDepth(swipedView, event_listener, 3, null);
					dragHandleView = swipedView.findViewById(R.id.drag_handle);
					dragHandleView.setOnTouchListener(mController);
				}
			}
			if(swipedView!=null&&((swipedView.getVisibility()==View.VISIBLE^expand))) {
				expandViewInner(swipedView, itemViewData.getRoot(), expand, animate);
			}
		}
		
		private static void expandViewInner(ViewGroup viewToSwipe, View root, boolean expand, boolean animate) {
			int width = root.getWidth();
			if(animate) {
				if(expand) {
					viewToSwipe.setAlpha(0);
					viewToSwipe.setTranslationX(width/3);
					viewToSwipe.animate().setListener(null).alpha(1)
							.translationX(0);
				} else {
					ViewGroup finalInflated = viewToSwipe;
					viewToSwipe.animate().setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							finalInflated.setVisibility(View.GONE);
						}
					}).alpha(0).translationX(width/3);
				}
			}
			if(expand||!animate) {
				if (expand) {
					viewToSwipe.setTranslationX(0);
					viewToSwipe.setAlpha(1);
				}
				viewToSwipe.setVisibility(expand?View.VISIBLE:View.GONE);
			}
		}
		
		public static void expandSyncView(View storageV, SuperItemListener onClickListener, boolean expand, boolean animate) {
			ViewGroup inflated = (ViewGroup) storageV.getTag();
			if(inflated==null&&expand) {
				ViewStub stub = storageV.findViewById(R.id.more_options);
				if(stub!=null) {
					stub.setOnInflateListener(null);
					inflated = (ViewGroup) stub.inflate();
					Utils.setOnClickListenersOneDepth(inflated, onClickListener, 3, null);
					storageV.setTag(inflated);
				}
			}
			if(inflated!=null&&inflated.getVisibility()==View.VISIBLE^expand) {
				expandViewInner(inflated, storageV, expand, animate);
			}
		}
	}
	
	class SearchEngineAdapter extends BaseAdapter implements DragSortListView.DragSortListener {
		public int getCount() {
			return WebDicts.size();
		}
		public Object getItem(int position) {
			return WebDicts.get(position);
		}
		public long getItemId(int position) {
			return 0;
		}
		// bindView
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			if(convertView==null) {
				vh = new ViewHolder(SearchEnginesItemBinding.inflate(inflater, parent, false), WebDictsManager.this);
				vh.itemViewData.moreVar.setOnClickListener(WebDictsManager.this);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			SearchEnginesItemBinding itemViewData = vh.itemViewData;
			
			vh.webDictItem = WebDicts.get(vh.position = position);
			
			WebDict webDictItem = vh.webDictItem;
			String name = webDictItem.name;
			if (webDictItem.activeChildName!=null && !TextUtils.equals(name, webDictItem.activeChildName)) {
				if (name.length()>3 && name.endsWith("搜索") || name.endsWith("词典") || name.endsWith("翻译")) {
					name = name.substring(0, name.length()-2);
				}
				name += " | "+ webDictItem.activeChildName;
			}
			itemViewData.title.setText(name);
			
			boolean hasMore = webDictItem.hasMoreVariants();
			
			itemViewData.tick.setImageResource(hasMore?R.drawable.delta_solid:R.drawable.ic_yes_blue);
			
			if (TextUtils.equals(a.currentWebDictUrl, webDictItem.url)) {
				itemViewData.tick.setVisibility(View.VISIBLE);
			} else {
				itemViewData.tick.setVisibility(View.GONE);
			}
			
			itemViewData.moreVar.setVisibility(hasMore?View.VISIBLE:View.GONE);
			
			vh.expandEditView(mController, webDictItem.isEditing, false);
			
			return vh.itemView;
		}
		@Override
		public void drag(int from, int to) {
			//CMN.Log("drag", from, to);
			if (from < to) {
				for (int i = from; i < to; i++) {
					Collections.swap(WebDicts, i, i + 1);
				}
			} else {
				for (int i = from; i > to; i--) {
					Collections.swap(WebDicts, i, i - 1);
				}
			}
		}
		@Override
		public void drop(int from, int to) {
			//CMN.Log("drop", from, to);
			if (from!=to) {
				isDirty = true;
				notifyDataSetChanged();
			}
		}
		@Override
		public void remove(int which) {
		}
	}
	View popline;
	
	private File getSavePath() {
		return new File(a.getExternalFilesDir(null), "WebDicts.json");
	}
	
	// 初始化
	@SuppressLint("ClickableViewAccessibility")
	private void init() {
		inflater = a.getLayoutInflater();
		int polypopupW = (int) (a._45_*1.5);
		layoutListener = a.UIData.layoutListener;
		listPopupWindow = new ListPopupWindow(a);
		GestureDetector mDetector = new GestureDetector(a, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
								   float velocityX, float velocityY) {
				View ca = editingView.itemView;
				ListView listView = listPopupWindow.getListView();
				if(listView!=null && ((DragSortListView) listView).isNotDragging()
						&& mController.getFloatView().getParent()==null
						&&  ca!=null && Math.abs(velocityX)*1.2f>Math.abs(velocityY)) {
					Object tag = ca.getTag();
					CMN.Log("fling!!!");
					if(velocityX!=0) {
						//listView.setPressed(false);
						Utils.preventDefaultTouchEvent(listPopupWindow.getListView(), -100, -100);
						// 向左滑动展开视图，向右则收起视图。 | left-right swipe to show-hide the view
						if(tag instanceof ViewHolder) {
							// 展开编辑视图： 删除、编辑、排序 | slide expand edit view
							((ViewHolder) tag).expandEditView(mController, velocityX<0, true);
						} else {
							// 展开同步视图： 上传、下载、选择 | slide expand sync view
							ViewHolder.expandSyncView(ca, WebDictsManager.this, isSyncShown=velocityX<0, true);
						}
						return true;
					}
				}
				return false;
			}
		});
		mDetector.setIsLongpressEnabled(false);
		touchInterceptor = (view, event) -> {
			ViewGroup vg = (ViewGroup) view;
			if(event.getActionMasked()== MotionEvent.ACTION_DOWN) {
				float y = event.getY();
				View ca=null;
				int i=0,len=vg.getChildCount();
				for(;i<len;i++) {
					ca=vg.getChildAt(i);
					if(ca.getBottom()>y) {
						break;
					}
				}
				if (i<len && ca!=null && ca.getTag() instanceof ViewHolder) {
					editingView = (ViewHolder) ca.getTag();
				} else {
					editingView = null;
				}
			}
			return mDetector.onTouchEvent(event);
		};
		listPopupWindow.setAdapter(mAdapter = new SearchEngineAdapter());
		listPopupWindow.setOnItemClickListener(WebDictsManager.this);
		listPopupWindow.setOnItemLongClickListener(WebDictsManager.this);
		
		if (parentMngr==null) {
			listPopupWindow.setAnchorView(a.UIData.popline);
		} else {
			popline = new View(a);
			popline.setVisibility(View.INVISIBLE);
			popline.setLayoutParams(new FrameLayout.LayoutParams(1, 1));
			listPopupWindow.setAnchorView(popline);
		}
		
		//searchEnginePopup.setOverlapAnchor(true); //21 禁开
		listPopupWindow.setDropDownAlwaysVisible(true);
		listPopupWindow.setOnDismissListener(() -> {
			mIsShowing = false;
			layoutListener.popups.remove(listPopupWindow);
			if (layoutListener.popups.size()==0) {
				layoutListener.setVisibility(View.GONE);
			}
//			if(polypopup!=null) polypopup.dismiss();
			if(isDirty) {
				if (parentMngr==null) {
					// 保存数据
					if(true) { // 调试默认值 true false
						JSONObject jsonData = new JSONObject();
						jsonData.put("WebDicts", parseListIntoJson(WebDicts));
						try (FileOutputStream fou = new FileOutputStream(getSavePath())){
							JSON.writeJSONString(fou, jsonData);
						} catch (IOException e) {
							CMN.Log(e);
						}
					}
				} else {
					parentMngr.notifyEditingItemChanged(true);
				}
				isDirty = false;
			}
			if (parentMngr!=null) {
				Utils.removeView(popline);
			}
		});
		
		//searchEnginePopup.mPopup.setEnterTransition(null);
//		polypopupview = LayoutInflater.from(a).inflate(R.layout.polymer, a.root, false);
//		polypopupview.setOnClickListener(v1 -> {
//			polysearching=!polysearching;
//			polypopupview.setAlpha(polysearching?1:0.25f);
//			a.showT(polysearching?"聚合搜索":"已关闭");
//			//m_currentToast.setGravity(Gravity.TOP, 0, appbar.getHeight());
//		});
//		polypopupview.setAlpha(polysearching?1:0.25f);
//		polypopup = new PopupWindow(polypopupview, polypopupW, polypopupW, false);
//		polypopup.setOutsideTouchable(false);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			polypopup.setEnterTransition(null);
//			polypopup.setExitTransition(null);
//		}
	}
	
	private void notifyEditingItemChanged(boolean changed) {
		if(changed) isDirty = true;
		BaseAdapter ada = mAdapter;
		if (ada!=null) {
			if (editingView !=null && editingView.position==editingPosition) {
				if(changed) ada.getView(editingPosition, editingView.itemView, listPopupWindow.getListView());
				ListView lv = listPopupWindow.getListView();
				if (lv!=null) {
					for (int i = 0, len=lv.getChildCount(); i < len; i++) {
						View itemV = lv.getChildAt(i);
						if (itemV!=null && itemV.getTag() instanceof ViewHolder) {
							ViewHolder vh = ((ViewHolder) itemV.getTag());
							vh.itemViewData.tick.setVisibility(TextUtils.equals(a.currentWebDictUrl, vh.webDictItem.url)?View.VISIBLE:View.GONE);
						}
					}
				}
			} else {
				ada.notifyDataSetChanged();
			}
		}
	}
	
	public boolean toggle() {
		int polypopupW = (int) (a._45_*1.5);
		int popupWidth = (GlobalOptions.screenIsVertical()?a.root.getWidth():a.root.getHeight())/2;
		int idealW = (int)Math.max(realWidth, 555* GlobalOptions.density);
		if (popupWidth<=0) popupWidth = idealW;
		else popupWidth = Math.min(popupWidth, idealW);
		
		if(listPopupWindow==null) {
			init();
		}
		boolean isVisible = listPopupWindow.isShowing();
		if(isVisible) {
			mIsShowing = false;
			if (listPopupWindow!=null) {
				listPopupWindow.dismiss();
				mController.bindTo(null);
			}
		} else {
			mIsShowing = true;
			if (parentMngr==null) {
				if (WebDicts==null) {
					// 读取数据
					WebDicts = new ArrayList<>(64);
					JSONObject jsonData = null;
					try (FileInputStream fin = new FileInputStream(getSavePath())){
						jsonData = JSON.parseObject(fin, null);
					} catch (IOException e) {
						CMN.Log(e);
					}
					if (jsonData!=null) {
						JSONArray dictsArrJs = jsonData.getJSONArray("WebDicts");
						parseJsonIntoList(dictsArrJs, WebDicts);
					}
				}
				if (WebDicts.size()==0) {
					TestHelper.testAddWebDicts(WebDicts);
				}
			}
			int iconWidth = a.UIData.ivBack.getWidth();
			int etSearch_getWidth = a.root.getWidth()-iconWidth*2;
			listPopupWindow.Selection=5;
			layoutListener.popups.add(listPopupWindow);
			layoutListener.setVisibility(View.VISIBLE);
			//layoutListener.supressNextUpdate=true;
			listPopupWindow.setWidth(popupWidth);
			listPopupWindow.setHeight(-2);
			if (parentMngr!=null) {
				Utils.addViewToParent(popline, a.UIData.layoutListener);
				popline.setTranslationX(collectOffset(0));
				popline.setTranslationY(collectOffset(1));
			}
			listPopupWindow.show();
			DropDownListView draSortListView = ((DropDownListView) listPopupWindow.getListView());
			View syncView = inflater.inflate(R.layout.search_engines_add, draSortListView, false);
			ViewHolder.expandSyncView(syncView, this, isSyncShown, false);
			View headerV = new View(a);
			//draSortListView.addHeaderView(headerV);
			headerV.setLayoutParams(new FrameLayout.LayoutParams(-1, 0));
			draSortListView.addFooterView(syncView);
			
			draSortListView.setOnDispatchTouchListener(touchInterceptor);
			if (mController==null) {
				mController = buildController(draSortListView);
			} else {
				mController.bindTo(draSortListView);
			}
			draSortListView.setFloatViewManager(mController);
			//draSortListView.setOnTouchListener(mController);
			draSortListView.setDragEnabled(true);
			
			//searchEnginePopup.mDropDownList.getLayoutParams().height=-2;
			//searchEnginePopup.mDropDownList.requestLayout();
			//((ViewGroup)searchEnginePopup.mDropDownList.getParent()).getLayoutParams().height=-2;
			//((ViewGroup)searchEnginePopup.mDropDownList.getParent()).requestLayout();
			//polypopup.showAtLocation(ivBack, Gravity.RIGHT|Gravity.TOP,10, appbar.getHeight());
//			if (polypopup!=null) {
//				polypopup.showAsDropDown(a.UIData.ivBack,iconWidth+((etSearch_getWidth+popupWidth+iconWidth)-(polypopupW))/2, -polypopupW/2);
//			}
		}
		return !isVisible;
	}
	
	private float collectOffset(int axis) {
		WebDictsManager mngr = this;
		float collected = 0;
		while((mngr = mngr.parentMngr)!=null) {
			DragSortListView dsl = (DragSortListView) mngr.listPopupWindow.getListView();
			if (dsl!=null) {
				if (axis==0) {
					collected += dsl.lastX;
				} else {
					collected += dsl.lastY;
				}
			}
		}
		return collected;
	}
	
	private void parseJsonIntoList(@Nullable JSONArray dictsArrJs, ArrayList<WebDict> webDicts) {
		if(dictsArrJs!=null) {
			for (int i = 0, len=dictsArrJs.size(); i < len; i++) {
				JSONObject wd = dictsArrJs.getJSONObject(i);
				WebDict parsed = new WebDict(wd.getString("url"), wd.getString("name"));
				parsed.activeChildName = wd.getString("act");
				webDicts.add(parsed);
				parseJsonIntoList(wd.getJSONArray("children"), parsed.getChildren());
			}
		}
	}
	
	private JSONArray parseListIntoJson(ArrayList<WebDict> webDicts) {
		JSONArray dictsArrJs = new JSONArray();
		for (int i = 0, len=webDicts.size(); i < len; i++) {
			WebDict wd = webDicts.get(i);
			JSONObject json = new JSONObject();
			json.put("url", wd.url);
			json.put("name", wd.name);
			if (wd.activeChildName!=null) {
				json.put("act", wd.activeChildName);
			}
			if (wd.hasMoreVariants()) {
				json.put("children", parseListIntoJson(wd.getChildren()));
			}
			dictsArrJs.add(json);
		}
		return dictsArrJs;
	}
	
	private DragSortController buildController(DragSortListView dslv) {
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		//controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(false);
		controller.setRemoveMode(DragSortController.FLING_REMOVE);
		controller.setSortEnabled(true);
		controller.setDragInitMode(DragSortController.ON_DOWN);
		//controller.setBackgroundColor(0xFF6B7AFF);
		//controller.setBackgroundColor(0xFFD6E6FF);
		controller.setBackgroundColor(0xEE008EFF);
		return controller;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		onClick(view);
	}
	
	@Override
	public boolean onLongClick(View v) {
		isLongClicked=true;
		longClick=true;
		onClick(v);
		isLongClicked=false;
		return longClick;
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return onLongClick(view);
	}
	
	// click
	@Override
	public void onClick(View v) {
		WebDictsManager variantMngr = variantMngrRef.get();
		if (variantMngr!=null && variantMngr.isVisible()) {
			variantMngr.toggle();
			v.jumpDrawablesToCurrentState();
			return;
		}
		ViewHolder vh = (ViewHolder) getTypedTagInAncestors(v, 3, ViewHolder.class);
		if(vh!=null) {
			WebDict wd = vh.webDictItem;
			int position = vh.position;
			int id = v.getId();
			switch (id) {
				// 显示子视图（二级树）
				case R.id.more_var: {
					processItemLongClk(vh, wd, true);
				} break;
				// 切换搜索引擎
				case R.id.search_engine_item: {
					if (isLongClicked) {
						processItemLongClk(vh, wd, false);
					} else {
						editingPosition = vh.position;
						editingView = vh;
						editingBean = wd;
						a.currentWebDictUrl = wd.url;
						if (parentMngr!=null && parentMngr.editingBean!=null) {
							parentMngr.editingBean.url = wd.url;
							parentMngr.editingBean.activeChildName = wd.name;
							isDirty = true;
						}
						listPopupWindow.dismiss();
						notifyEditingItemChanged(false);
					}
				} break;
				case R.id.ivBack:
					if(isLongClicked) {
						for (WebDict webDict:WebDicts) {
							webDict.isEditing = false;
						}
						mAdapter.notifyDataSetChanged();
						Utils.preventDefaultTouchEvent(listPopupWindow.getListView(), -100, -100);
					} else {
						vh.expandEditView(mController, false, true);
					}
					break;
				case R.id.edit:
					showEditDialog(wd);
					break;
				case R.id.delete:{
					WebDicts.remove(position);
					mAdapter.notifyDataSetChanged();
					isDirty =true;
				} break;
			}
		}
		else {
			showEditDialog(null);
		}
	}
	
	private void processItemLongClk(ViewHolder vh, WebDict wd, boolean showVariants) {
		if (!showVariants) {
			showVariants = true;
		}
		if (showVariants) {
			// 允许多级变体
			editingPosition = vh.position;
			editingView = vh;
			editingBean = wd;
			WebDictsManager variantMngr = variantMngrRef.get();
			if (variantMngr==null) {
				variantMngrRef = new WeakReference<>(variantMngr=new WebDictsManager(this, null));
			}
			variantMngr.setEnginsList(wd.getChildren());
			if (!variantMngr.isVisible()) {
				variantMngr.toggle();
			}
			variantMngr.listPopupWindow.show();
			//WebDictsManager finalVariantMngr = variantMngr; v.post(() -> finalVariantMngr.listPopupWindow.show());
		} else {
			// todo 否则，在此处新建节点
			showEditDialog(null);
		}
	}
	
	private void setEnginsList(ArrayList<WebDict> list) {
		if (WebDicts != list) {
			WebDicts = list;
			if (mAdapter!=null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private void showEditDialog(WebDict editing) {
		editingBean = editing;
		AlertDialog edtDlg = (AlertDialog) a.getReferencedObject(WeakReferenceHelper.edit_scheng);
		if(edtDlg==null) {
			LoginViewBinding dialogEditView = LoginViewBinding.inflate(inflater, a.root, false);
			AlertDialog d = new AlertDialog
					.Builder(a)
					.setView(dialogEditView.getRoot())
					.setTitle("添加搜索引擎")
					.setOnDismissListener(dialog -> {
						dialogEditView.url.setText(null);
						dialogEditView.name.setText(null);
					})
					.setPositiveButton("确认", null)
					.setNegativeButton("取消", null)
					.show();
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
				WebDictsManager mngr = ((WebDictsManager) d.tag1);
				WebDict edtBean = mngr.editingBean;
				String url = Utils.getFieldInView(dialogEditView.url);
				String name = Utils.getFieldInView(dialogEditView.name);
				if(url.startsWith("http")) {
					if(edtBean==null) {
						mngr.WebDicts.add(new WebDict(url, name));
					} else {
						edtBean.url = url;
						edtBean.name = name;
					}
					isDirty =true;
					if(mAdapter !=null) {
						mAdapter.notifyDataSetChanged();
					}
					d.dismiss();
				} else {
					a.showT("无效的URL地址");
				}
			});
//			d.getTitleView().setOnLongClickListener(v -> {
//			});
			View.OnClickListener lis = v14 -> {
				ViewGroup editView = (ViewGroup) v14.getParent();
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
			};
			dialogEditView.nameMorpt.setOnClickListener(lis);
			dialogEditView.urlMorpt.setOnClickListener(lis);
			d.tag = dialogEditView;
			a.putReferencedObject(WeakReferenceHelper.edit_scheng, edtDlg=d);
		}
		edtDlg.tag1 = this;
		LoginViewBinding dialogEditView = (LoginViewBinding) edtDlg.tag;
		if(editingBean!=null) {
			dialogEditView.url.setText(editingBean.url);
			dialogEditView.name.setText(editingBean.name);
		} else if(true) {
			dialogEditView.url.setText(a.currentWebView.getUrl());
			dialogEditView.name.setText(a.currentWebView.getTitle());
		}
		edtDlg.setTitle(editing==null?(parentMngr==null?"添加搜索引擎":"添加搜索引擎变体"):"修改搜索引擎");
		edtDlg.show();
	}
	
	
	/** 看见 ： https://www.baidu.com/s?ie=UTF-8&wd=%E5%AE%81%E6%B3%A2%E5%B7%A5%E7%A8%8B%E5%AD%A6%E9%99%A2
	 * 提议 : 复古刑，正人欲，惩凶恶！
	 * 复古刑，要将古代的刑罚重新拿出来，要在现代的司法框架内，正确施用古代刑法的惩罚形式，包括但不限于绞刑，车裂，埋剥，凌迟等等。
	 * 		此文中的黑人凶手 | the Black Murderer Abdulmateen Shadeed，杀死宁波工程学院在校23岁的女性，残忍糟蹋并灭绝我国种子，应当被施以正确的刑罚。
	 * 正人欲，要收束人类不好的欲望，要激发对人类有益的欲望。不好的欲望主要指无收束的兽欲，既然无收束，其必愈演愈烈而寻求新的施加对象与刺激形式。
	 * 		好的欲望指待人之热情，既投之以桃必希冀其报还以李；以及奋斗之激情，即通过奋斗可以改变阶级面貌，继而促进人民团结。
	 * 惩凶恶，各级人士要在法律失力之时，（学习）效法中国古代李白、杜甫等人的侠义情怀，不仅要心怀其情，必定要行法其宗。
	 *		心怀其情，指鼓励对古代文化的热爱与研究，如恢复汉服等的国家地位、与国歌同级；行法其宗，指当面对外来民族的欺压凌辱，当法律哪怕暂时失力之时，任意有志青年当有出手仗剑行义之权力。
	 *
	 *呜呼，美利坚合众国时常有之游行，在我国为乌有，每每听号哭于狼藉，则我民何以团结？
	 * 华夏儿女何在，汉家子弟何在？ 你，看见了吗。
	 * */
	public boolean isVisible() {
		return mIsShowing;
	}
}
