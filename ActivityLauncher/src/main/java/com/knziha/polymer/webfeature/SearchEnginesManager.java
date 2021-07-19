package com.knziha.polymer.webfeature;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.DropDownListView;
import androidx.appcompat.widget.ListPopupWindow;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.TestHelper;
import com.knziha.polymer.WeakReferenceHelper;
import com.knziha.polymer.databinding.LoginViewBinding;
import com.knziha.polymer.databinding.SearchEnginesItemBinding;
import com.knziha.polymer.webstorage.WebDict;
import com.knziha.polymer.widgets.EditFieldHandler;
import com.knziha.polymer.widgets.PopupBackground;
import com.knziha.polymer.widgets.SuperItemListener;
import com.knziha.polymer.widgets.Utils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;

import static androidx.appcompat.app.GlobalOptions.realWidth;
import static com.knziha.polymer.browser.webkit.WebViewHelper.getTypedTagInAncestors;

public class SearchEnginesManager {
	final BrowserActivity a;
	
	public ArrayList<WebDict> WebDicts = new  ArrayList<>(64);
	
	private WebDict editingSchEngBean;
	private View currentSchEngItemView;
	private boolean isSyncSchEngShown;
	private boolean isSchEngsDirty;
	private BaseAdapter searchEngineAdapter;
	
	private ListPopupWindow searchEnginePopup;
	private PopupBackground layoutListener;
	private PopupWindow polypopup;
	private View polypopupview;
	private boolean polysearching;
	private DragSortController mController;
	
	public SearchEnginesManager(BrowserActivity a) {
		this.a = a;
		TestHelper.testAddWebDicts(WebDicts);
		a.currentWebDictUrl = WebDicts.get(0).url;
	}
	
	SuperItemListener swiped_layout_listener;
	
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
		public View getView(int position, View convertView, ViewGroup parent) {
			SearchEnginesItemBinding itemViewData;
			if(convertView==null) {
				itemViewData = SearchEnginesItemBinding.inflate(a.getLayoutInflater(), parent, false);
				convertView = itemViewData.getRoot();
				//convertView.setOnClickListener(BrowserActivity.this);
				convertView.setTag(itemViewData);
			} else {
				itemViewData = (SearchEnginesItemBinding) convertView.getTag();
			}
			WebDict webDictItem = WebDicts.get(position);
			itemViewData.businessCard.setTag(webDictItem);
			itemViewData.title.setTag(position);
			String name=webDictItem.name;
			itemViewData.title.setText(name==null?"Untitled":name);
			itemViewData.tick.setVisibility(TextUtils.equals(a.currentWebDictUrl, webDictItem.url)?View.VISIBLE:View.GONE);
			webDictItem.expandEditView(itemViewData, swiped_layout_listener, webDictItem.isEditing, false);
			return convertView;
		}
		@Override
		public void drag(int from, int to) {
			//CMN.Log("drag", from, to);
		}
		@Override
		public void drop(int from, int to) {
			//CMN.Log("drop", from, to);
			if (from!=to) {
				if (from < to) {
					for (int i = from; i < to; i++) {
						Collections.swap(WebDicts, i, i + 1);
					}
				} else {
					for (int i = from; i > to; i--) {
						Collections.swap(WebDicts, i, i - 1);
					}
				}
				//a.root.postDelayed(this::notifyDataSetChanged, 350);
				notifyDataSetChanged();
			}
		}
		@Override
		public void remove(int which) {
		}
	}
	
	View.OnTouchListener touchInterceptor;
	
	// 初始化
	@SuppressLint("ClickableViewAccessibility")
	private void init() {
		int polypopupW = (int) (a._45_*1.5);
		layoutListener = a.UIData.layoutListener;
		searchEnginePopup = new ListPopupWindow(a);
		swiped_layout_listener = new SuperItemListener() {
			boolean isLongClicked;
			boolean longClick;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onClick(view);
			}
			@Override
			public boolean onLongClick(View v) {
				isLongClicked=true;
				longClick=true;
				onClick(v);
				return longClick;
			}
			@Override
			public void onClick(View v) {
				SearchEnginesItemBinding itemData = (SearchEnginesItemBinding) getTypedTagInAncestors(v, 3, SearchEnginesItemBinding.class);
				if(itemData!=null) {
					int position = (int) itemData.title.getTag();
					int id = v.getId();
					switch (id) {
						case R.id.search_engine_edit: {
							a.currentWebDictUrl = ((WebDict) itemData.businessCard.getTag()).url;
							searchEnginePopup.dismiss();
							searchEngineAdapter.notifyDataSetChanged();
						} break;
//						case R.id.up:
//						case R.id.down: {
//							boolean up=id==R.id.up;
//							if(up?position>0:position<WebDicts.size()-1) {
//								WebDict webDictItem = WebDicts.set(up?position-1:position+1, WebDicts.get(position));
//								WebDicts.set(position, webDictItem);
//								searchEngineAdapter.notifyDataSetChanged();
//							}
//							isSchEngsDirty=true;
//						} break;
						case R.id.edit:{
							showEditSchEngDlg((WebDict) itemData.businessCard.getTag());
						} break;
						case R.id.delete:{
							WebDicts.remove(position);
							searchEngineAdapter.notifyDataSetChanged();
							isSchEngsDirty=true;
						} break;
					}
				}
				else {
					showEditSchEngDlg(null);
				}
			}
		};
		GestureDetector mDetector = new GestureDetector(a, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
								   float velocityX, float velocityY) {
				View ca = currentSchEngItemView;
				if(ca!=null && Math.abs(velocityX)*1.2f>Math.abs(velocityY)) {
					ca = currentSchEngItemView.findViewById(R.id.search_engine_item);
					if (ca!=null) {
						Object tag = ca.getTag();
						if(velocityX!=0) {
							if(tag instanceof SearchEnginesItemBinding) {
								// 展开编辑视图： 删除、编辑、排序 | slide expand edit view
								SearchEnginesItemBinding itemData = (SearchEnginesItemBinding) tag;
								WebDict webDictItem = (WebDict) itemData.businessCard.getTag();
								webDictItem.expandEditView(itemData, swiped_layout_listener, velocityX<0, true);
							} else {
								// 展开同步视图： 上传、下载、下载 | slide expand sync view
								WebDict.expandSyncView(ca, swiped_layout_listener, isSyncSchEngShown=velocityX<0, true);
							}
							return true;
						}
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
				currentSchEngItemView = i<len?ca:null;
			}
			return mDetector.onTouchEvent(event);
		};
		searchEnginePopup.setAdapter(searchEngineAdapter = new SearchEngineAdapter());
		searchEnginePopup.setOnItemClickListener(swiped_layout_listener);
		searchEnginePopup.setAnchorView(a.findViewById(R.id.popline));
		//searchEnginePopup.setOverlapAnchor(true); //21 禁开
		searchEnginePopup.setDropDownAlwaysVisible(true);
		searchEnginePopup.setOnDismissListener(() -> {
			layoutListener.setVisibility(View.GONE);
			layoutListener.popup=null;
			if(polypopup!=null) polypopup.dismiss();
			if(isSchEngsDirty) {
				// todo save and load search engines
				isSchEngsDirty = false;
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
	
	public boolean toggle() {
		int polypopupW = (int) (a._45_*1.5);
		int searchEnginePopupW = (int) (a.etSearch.getWidth()*0.90);
		searchEnginePopupW = Math.min(searchEnginePopupW, (int)Math.max(realWidth, 555* GlobalOptions.density));
		
		if(searchEnginePopup==null) {
			init();
		}
		boolean isVisible = searchEnginePopup.isShowing();
		if(isVisible) {
			if (searchEnginePopup!=null) {
				searchEnginePopup.dismiss();
				mController.bindTo(null);
			}
		} else {
			int iconWidth = a.UIData.ivBack.getWidth();
			int etSearch_getWidth = a.root.getWidth()-iconWidth*2;
			searchEnginePopup.Selection=5;
			layoutListener.popup = searchEnginePopup;
			layoutListener.setVisibility(View.VISIBLE);
			//layoutListener.supressNextUpdate=true;
			searchEnginePopup.setWidth(searchEnginePopupW);
			searchEnginePopup.setHeight(-2);
			searchEnginePopup.show();
			DropDownListView draSortListView = ((DropDownListView) searchEnginePopup.getListView());
			View syncView = a.getLayoutInflater().inflate(R.layout.search_engines_add, draSortListView, false);
			WebDict.expandSyncView(syncView, swiped_layout_listener, isSyncSchEngShown, false);
			View headerV = new View(a);
			//draSortListView.addHeaderView(headerV);
			headerV.setLayoutParams(new FrameLayout.LayoutParams(-1, 0));
//			draSortListView.addFooterView(syncView);
			
			draSortListView.setOnDispatchTouchListener(touchInterceptor);
			if (mController==null) {
				mController = buildController(draSortListView);
			} else {
				mController.bindTo(draSortListView);
			}
			draSortListView.setFloatViewManager(mController);
			draSortListView.setOnTouchListener(mController);
			draSortListView.setDragEnabled(true);
			
			//searchEnginePopup.mDropDownList.getLayoutParams().height=-2;
			//searchEnginePopup.mDropDownList.requestLayout();
			//((ViewGroup)searchEnginePopup.mDropDownList.getParent()).getLayoutParams().height=-2;
			//((ViewGroup)searchEnginePopup.mDropDownList.getParent()).requestLayout();
			//polypopup.showAtLocation(ivBack, Gravity.RIGHT|Gravity.TOP,10, appbar.getHeight());
			if (polypopup!=null) {
				polypopup.showAsDropDown(a.UIData.ivBack,iconWidth+((etSearch_getWidth+searchEnginePopupW+iconWidth)-(polypopupW))/2, -polypopupW/2);
			}
		}
		return !isVisible;
	}
	
	private DragSortController buildController(DragSortListView dslv) {
		// defaults are
		//   dragStartMode = onDown
		//   removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		//controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(false);
		controller.setRemoveMode(DragSortController.FLING_REMOVE);
		controller.setSortEnabled(true);
		controller.setDragInitMode(DragSortController.ON_DOWN);
		controller.setBackgroundColor(Color.YELLOW);
		return controller;
	}
	
	private void showEditSchEngDlg(WebDict editing) {
		editingSchEngBean = editing;
		AlertDialog editSchEngDlg = (AlertDialog) a.getReferencedObject(WeakReferenceHelper.edit_scheng);
		if(editSchEngDlg==null) {
			LoginViewBinding schEngEditView = LoginViewBinding.inflate(a.getLayoutInflater(), a.root, false);
			AlertDialog d = new AlertDialog
					.Builder(a)
					.setView(schEngEditView.getRoot())
					.setTitle("添加搜索引擎")
					.setOnDismissListener(dialog -> {
						schEngEditView.url.setText(null);
						schEngEditView.name.setText(null);
						editingSchEngBean=null;
					})
					.setPositiveButton("确认", null)
					.setNegativeButton("取消", null)
					.show();
			d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
				String url = Utils.getFieldInView(schEngEditView.url);
				String name = Utils.getFieldInView(schEngEditView.name);
				if(url.startsWith("http")) {
					if(editingSchEngBean==null) {
						WebDicts.add(new WebDict(url, name));
					} else {
						editingSchEngBean.url = url;
						editingSchEngBean.name = name;
					}
					isSchEngsDirty=true;
					if(searchEngineAdapter!=null) {
						searchEngineAdapter.notifyDataSetChanged();
					}
					d.dismiss();
				} else {
					a.showT("无效的URL地址");
				}
			});
			d.getTitleView().setOnLongClickListener(v -> {
				for(WebDict webDictItem:WebDicts) {
					webDictItem.isEditing = false;
				}
				if(searchEngineAdapter!=null) {
					searchEngineAdapter.notifyDataSetChanged();
				}
				return true;
			});
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
			schEngEditView.nameMorpt.setOnClickListener(lis);
			schEngEditView.urlMorpt.setOnClickListener(lis);
			d.tag = schEngEditView;
			a.putReferencedObject(WeakReferenceHelper.edit_scheng, editSchEngDlg=d);
		}
		if(editingSchEngBean!=null) {
			LoginViewBinding schEngEditView = (LoginViewBinding) editSchEngDlg.tag;
			schEngEditView.url.setText(editingSchEngBean.url);
			schEngEditView.name.setText(editingSchEngBean.name);
		}
		editSchEngDlg.setTitle(editing==null?"添加搜索引擎":"修改搜索引擎");
		editSchEngDlg.show();
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
		return searchEnginePopup!=null && searchEnginePopup.isShowing();
	}
}
