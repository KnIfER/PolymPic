package com.knziha.polymer.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import com.knziha.polymer.browser.AppIconCover.AppLoadableBean;
import com.knziha.polymer.databinding.LoginViewBinding;
import com.knziha.polymer.preferences.NavHomeEditorDialogSettings;
import com.knziha.polymer.webslideshow.TouchSortHandler;
import com.shockwave.pdfium.treeview.TreeViewAdapter;
import com.shockwave.pdfium.treeview.TreeViewNode;

import org.adrianwalker.multilinestring.Multiline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static com.knziha.polymer.webslideshow.ImageViewTarget.FuckGlideDrawable;
import static com.knziha.polymer.widgets.NavigationNode.removeChild;

public class NavigationHomeAdapter extends TreeViewAdapter<NavigationHomeAdapter.ViewHolder>
		implements View.OnTouchListener, DragSelectRecyclerView.IDragSelectAdapter {
	BrowserActivity a;
	protected View root;
	protected RecyclerView recyclerView;
	JSONObject jsonData = new JSONObject();
	NavigationRootNode navRoot;
	RequestBuilder<Drawable> glideSaver;
	boolean bIsShowing;
	final int bottomPaddding;
	private EditTextmy etSearch;
	TouchSortHandler touchHandler;
	ItemTouchHelper touchHelper;
	private NavigationNode draggingNode;
	private ViewHolder draggingView;
	private NavigationNode editingNavHomeBean;
	private NavigationNode activeNavHomeBean;
	private ArrayList<NavigationNode> mRecyclerBin = new ArrayList<>();
	private int activeNavHomeBeanPos;
	
	static  WeakReference<NavFolderFragment> navFolderRef = new WeakReference<>(null);
	private FolderNavListener folderNavListener;
	private boolean folderViewDirty;
	
	HashSet<NavigationNode> selection = new HashSet<>();
	private int lastDragFromPosition;
	private int lastDragToPosition;
	private NavigationNode draggingPosStPvNode;
	private boolean isDraggingDown;
	
	@Override
	public void setSelected(int idx, boolean selected) {
		if (getSelMode() && idx>=0 && idx<displayNodes.size()-1) { // sanity check
			NavigationNode node = (NavigationNode) displayNodes.get(idx);
			//selected = !selection.containsKey(node);
			boolean upd;
			if (selected) {
				upd = selection.add(node);
			} else {
				upd = selection.remove(node);
			}
			if (upd) {
				notifyItemChanged(idx, "0");
			}
		}
	}
	
	interface FolderNavListener {
		void onNavToFolder(NavigationNode node);
	}
	
	public void setFolderNavListener(FolderNavListener folderNavListener) {
		this.folderNavListener = folderNavListener;
	}
	
	public NavigationHomeAdapter(BrowserActivity activity, int bottomPaddding)
	{
		super(null);
		a = activity;
		this.bottomPaddding = bottomPaddding;
		
		File f = new File(a.getExternalFilesDir(null), "Bookmarks.json");
		try (FileInputStream fin = new FileInputStream(f)){
			jsonData = JSON.parseObject(fin, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		NavigationNode.cc = 0;
		JSONObject navigation = null;
		if (jsonData!=null) {
			JSONObject roots = jsonData.getJSONObject("roots");
			if(roots!=null) {
				navigation = roots.getJSONObject("bookmark_bar");
				// todo search active navigation node
				//navigation = roots.getJSONObject("navigation");
				//if (navigation==null) {
				//	navigation = roots.getJSONObject("bookmark_bar");
				//}
			}
		}
		
		if (navigation==null) {
			jsonData = null;
			navigation = new JSONObject();
		}
		navRoot = new NavigationRootNode(f, jsonData, navigation);
		a.showT(""+NavigationNode.cc);
		rootNode = navRoot;
		footNode = new NavigationNode(new JSONObject());
		footNode.setParent(rootNode);
		//setPadding(15);
	}
	
	private NavigationHomeAdapter(NavigationHomeAdapter parentView)
	{
		super(null);
		a = parentView.a;
		rootNode = parentView.rootNode;
		navRoot = parentView.navRoot;
		isFolderView = true;
		bottomPaddding = 0;
		currentExpChannel=0x2;
		normalExpChannel=0x2;
		schViewExpChannel=0x2;
	}
	
	static class NavigationRootNode extends NavigationNode implements AppLoadableBean
	{
		public final AppIconCover saveTask = new AppIconCover(this);
		boolean isDirty;
		final File f;
		final JSONObject jsonRoot;
		public NavigationRootNode(File f, JSONObject jsonRoot, @NonNull JSONObject itemData) {
			super(itemData);
			this.f = f;
			if (jsonRoot==null) {
				jsonRoot = new JSONObject();
				JSONObject jsonRoots = new JSONObject();
				jsonRoot.put("roots", jsonRoots);
				jsonRoots.put("navigation", itemData);
			}
			this.jsonRoot = jsonRoot;
			setExpanded(0xFF, true);
		}
		
		public void checkDirty() throws IOException {
			if (isDirty) {
				String json = jsonRoot.toString(SerializerFeature.PrettyFormat);
				//CMN.Log(data);
				byte[] data = json.getBytes();
				if (f.length()<data.length) {
					try (FileOutputStream fileOutputStream = new FileOutputStream(f, true)) {
						fileOutputStream.write(data, 0, (int) (data.length-f.length()));
					}
				}
				try (RandomAccessFile raf = new RandomAccessFile(f, "rw")){
					raf.write(data);
					raf.setLength(data.length);
				}
				CMN.Log("保存了……");
				isDirty = false;
			}
		}
		
		@Override
		public Drawable load() throws IOException {
			checkDirty();
			return FuckGlideDrawable;
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		return R.layout.nav_list_item;
	}
	
	@Override
	protected boolean filterNode(TreeViewNode node) {
		String name=((NavigationNode)node).getName();
		boolean ret;
		if ((currentSchFlg&0x3)!=0) { // 不区分大小写
			name = name.toLowerCase();
		}
		Object filter = this.currentFilter;
		if (filter instanceof Pattern) {
			return ((Pattern) filter).matcher(name).find();
		} else if(filter instanceof String) {
			if ((currentSchFlg&0x4)!=0) { // 从头开始比对
				return name.startsWith((String) filter);
			}
			//if (name.equals("私房")) CMN.Log("私房::", name.contains((String) filter));
			return name.contains((String) filter);
		}
		return false;
	}
	
	public View getNavigationView() {
		if (root==null) {
			root = (ViewGroup) a.getLayoutInflater().inflate(R.layout.nav_view, a.UIData.webcoord, false);
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
					if ((currentSchFlg&0x3)!=0) { // 不区分大小写
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
				filterTreeView(pattern, schFlag);
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
			root.setAlpha(0);
			root.setTranslationY(bottomPaddding);
			SetRecyclerView(root.findViewById(R.id.recycler_view));
			if (getEditMode()) {
				toggleEditMode();
				nav_header.findViewById(R.id.edit).performClick();
			} else if (getShowDragHandle()) {
				InitDragSort();
			}
		}
		return root;
	}
	
	public void SetRecyclerView(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
		LinearLayoutManager lm = new LinearLayoutManager(a);
		recyclerView.setLayoutManager(lm);
		recyclerView.setItemAnimator(null);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
		recyclerView.setNestedScrollingEnabled(false);
		recyclerView.addItemDecoration(new RecyclerView.ItemDecoration(){
			final ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
			final int mDividerHeight = 1;
			@Override
			public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
				final int childCount = parent.getChildCount();
				final int width = parent.getWidth();
				for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
					final View view = parent.getChildAt(childViewIndex);
					if (shouldDrawDividerBelow(view, parent)) {
						int top = (int) view.getY() + view.getHeight();
						NavigationNode node = (NavigationNode) displayNodes.get(parent.getChildViewHolder(view).getLayoutPosition());
						//boolean isLeaf = node.isLeaf();
						//mDivider.setColor(isLeaf?Color.GRAY:Color.GREEN);
						boolean dragIndicator = touchHandler != null && touchHandler.isDragging && node == draggingPosStPvNode;
						int color =  dragIndicator?0xff4F7FDF:Color.GRAY;
						mDivider.setColor(color);
						int pad=node.getHeight()*padding;
						mDivider.setBounds(pad, top, width-padding, top + (dragIndicator?3:mDividerHeight));
						mDivider.draw(c);
					}
				}
			}
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
				if (shouldDrawDividerBelow(view, parent)) {
					outRect.bottom = mDividerHeight;
				}
			}
			private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
				//return parent.getChildViewHolder(view).getLayoutPosition()<parent.getChildCount()-1;
				return parent.getChildViewHolder(view).getBindingAdapterPosition()<getItemCount()-2;
			}
		});
		
		setTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
			@Override
			public boolean onClick(TreeViewNode node, RecyclerView.ViewHolder holder) {
				int lastX = recyclerView.mLastTouchX;
				int width = holder.itemView.getWidth();
				if(!isFolderView && getSelMode()) {
					if (lastX > width/3) {
						markIntervalSel(node, holder.getLayoutPosition());
						setSelected(holder.getBindingAdapterPosition(), !selection.contains(node));
						return true;
					} else if (node.isLeaf() && !getEditMode()) {
						holder.itemView.performLongClick();
						return true;
					}
				}
				if(isFolderView && lastX > width*0.75) {
					((ViewHolder)holder).fvGoBtn.performClick();
					return true;
				} else if(!isFolderView && getEditMode() && (node.isLeaf() || getSelMode() || lastX < width/3)) {
					if (!node.isLeaf() && getSelMode() && lastX<28*GlobalOptions.density) {
						performIvArrowClk((ViewHolder) holder);
					} else {
						CMN.Log(lastX, width/3);
						//a.showT("编辑节点");
						longClickView = (ViewHolder) holder;
						showNavEditorDlg((NavigationNode) node, null, null);
					}
					return true;
				} else {
					if (!node.isLeaf()) {
						onToggle(!node.isExpand(currentExpChannel), holder);
					} else {
						Object content = node.getContent();
						if(content instanceof JSONObject) {
							String url = ((JSONObject) content).getString("url");
							if (url!=null) {
								a.execBrowserGoTo(url);
								if (bIsShowing) {
									toggle();
								}
							}
						}
					}
				}
				//CMN.Log("onToggle", bmRv.isLayoutSuppressed(), CMN.id(node.getContent()), CMN.id(bmRv), CMN.id(bmRv.getAdapter()), node.getContent());
				
				return false;
			}
			
			@Override
			public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
				if(holder instanceof ViewHolder) {
					ViewHolder viewholder = (ViewHolder) holder;
					viewholder.ivArrow.animate().rotation(isExpand ? 90 : 0).start();
				}
			}
		});
		setHasStableIds(true);
		recyclerView.setAdapter(this);
		
		if (isFolderView && getAutoExpandAllFoldersForFov()) {
			refresh(null, navRoot.getChildList(isFolderView), 0, true);
		} else {
			refresh(navRoot.getChildList(isFolderView), 1024);
		}
		//lm.scrollToPositionWithOffset(displayNodes.size()-40, 0);
	}
	
	int[] selInterval = new int[4];
	int selIntervalIdx = 0;
	private void markIntervalSel(TreeViewNode node, int position) {
		int[] selInterval = this.selInterval;
		if (selInterval[0]==position) {
			selIntervalIdx=0;
		} else if (selInterval[2]==position) {
			selIntervalIdx=1;
		} else if (selInterval[selIntervalIdx]!=position) {
			selIntervalIdx ++;
		}
		selIntervalIdx %= 2;
		selInterval[selIntervalIdx*2] = position;
		selInterval[selIntervalIdx*2+1] = CMN.id(node.getContent());
	}
	
	AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			if (!bIsShowing) {
				Utils.removeIfParentBeOrNotBe(root, null, false);
			}
		}
	};
	
	public void toggle() {
		ViewGroup root = a.UIData.webcoord;
		float targetAlpha = 1;
		float targetTrans = 0;
		View viewToAdd = getNavigationView();
		if (bIsShowing=!bIsShowing) {
			Utils.addViewToParent(viewToAdd, root, -1);
			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess_ic_back);
			a.UIData.browserWidget8.setImageResource(R.drawable.ic_baseline_book_24);
		} else {
			targetAlpha = 0;
			targetTrans = bottomPaddding;
			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess);
			a.UIData.browserWidget8.setImageResource(R.drawable.chevron_forward);
			a.postResoreSel();
			etSearch.clearFocus();
		}
		viewToAdd
				.animate()
				.alpha(targetAlpha)
				.translationY(targetTrans)
				.setDuration(180)
				.setListener(bIsShowing?null:animatorListenerAdapter)
				//.start()
		;
	}
	
	NavigationNode NewNode_insertAfter;
	boolean NewNode_isFolder;
	
	public void InsertNavNode(String url, String title) {
		//NewNode_insertAfter = ;
		NewNode_isFolder = false;
		activeNavHomeBean = null;
		showNavEditorDlg(null, url, title);
	}
	
	private void showNavEditorDlg(NavigationNode editing, String url, String title) {
		editingNavHomeBean = editing;
		AlertDialog editNavHomeDlg = (AlertDialog) a.getReferencedObject(WeakReferenceHelper.edit_navhome);
		if(editNavHomeDlg==null) {
			LoginViewBinding schEngEditView = LoginViewBinding.inflate(a.getLayoutInflater(), a.root, false);
			AlertDialog d = new AlertDialog
					.Builder(a)
					.setView(schEngEditView.getRoot())
					.setTitle("编辑导航节点")
					.setOnDismissListener(dialog -> {
						schEngEditView.url.setText(null);
						schEngEditView.name.setText(null);
						editingNavHomeBean=null;
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
						if (InsertNavNode_internal(NewNode_insertAfter, NewNode_isFolder, editingNavHomeBean, new_url, new_name)) {
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
									, NavigationHomeAdapter.this);
							schEngEditView.getRoot().setTag(shimObj);
						}
						shimObj.toggle(dRoot);
					} break;
					case R.id.alertTitle: {
						if (activeNavHomeBean!=null) {
							showPopupMenu(v);
						}
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
			d.tag = schEngEditView;
			schEngEditView.name.setTag(folderText);
			a.putReferencedObject(WeakReferenceHelper.edit_navhome, editNavHomeDlg=d);
		}
		LoginViewBinding schEngEditView = (LoginViewBinding) editNavHomeDlg.tag;
		TextView folderText = (TextView) schEngEditView.name.getTag();
		if(editing!=null) {
			url = editing.getUrl();
			title = editing.getName();
			folderText.setText(((NavigationNode)editing.getParent()).getName());
		} else if(url==null){
			url=a.currentWebView.getUrl();
			title=a.currentWebView.getTitle();
		}
		if (editing!=null && !editing.isLeaf() || editing==null && NewNode_isFolder) {
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
	
	private boolean InsertNavNode_internal(NavigationNode insertAfter, boolean isFolder, NavigationNode editing, String new_url, String new_name) {
		isFolder = editing!=null&&!editing.isLeaf() || editing==null&&isFolder;
		if (!isFolder && TextUtils.isEmpty(new_url)) {
			a.showT("地址不能为空");
			return false;
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
			if(isVisible()) notifyDataSetChanged();
		} else {
			NavigationNode np = (NavigationNode) insertAfter.getParent();
			int index;
			if (!insertAfter.isLeaf() && insertAfter.isExpand(currentExpChannel)) {
				np = insertAfter;
				index = Integer.MAX_VALUE;
			} else {
				index = np.getChildList().indexOf(insertAfter) + 1;
			}
			np.addNewChild(new_url, new_name, index);
			
			if(isVisible()) refreshList(false);
		}
		MarkDirty();
		return true;
	}
	
	private void MarkDirty() {
		navRoot.isDirty = true;
		folderViewDirty = true;
	}
	
	public void showFolderView() {
		if (!isFolderView) {
			NavFolderFragment navFolderFragment=navFolderRef.get();
			if(navFolderFragment==null) {
				navFolderFragment = new NavFolderFragment();
				navFolderFragment.dm = a.dm;
				navFolderFragment.foldViewAdapter = new NavigationHomeAdapter(this);
				navFolderRef = new WeakReference<>(navFolderFragment);
				folderViewDirty = false;
				navFolderFragment.foldViewAdapter.setFolderNavListener(node -> {
					lastSelectionId = CMN.id(node.getContent());
					boolean allExpanded = true;
					NavigationNode targetNode = node;
					while ((node = (NavigationNode) node.getParent())!=null) {
						if (!node.isExpand(currentExpChannel)) {
							node.setExpanded(currentExpChannel, true);
							allExpanded = false;
						}
					}
					boolean expandSel = true;
					if (allExpanded) {
						lastSelectionPos = displayNodes.indexOf(targetNode);
						//a.showT("allExpanded"+lastSelectionPos);
						if (lastSelectionPos==-1) {
							allExpanded = false;
						} else {
							LinearLayoutManager lmm = ((LinearLayoutManager) recyclerView.getLayoutManager());
							lmm.scrollToPositionWithOffset(lastSelectionPos, 0);
							//CMN.Log("soft upd::"+recyclerView.getChildAt(0).getTag());
							if (expandSel && !targetNode.isExpand(currentExpChannel)) {
								notifyItemRangeInserted(lastSelectionPos+1
										, addChildNodesFiltered(targetNode, lastSelectionPos+1, 0x1));
							}
						}
					}
					if (!allExpanded) {
						if (expandSel) {
							targetNode.setExpanded(currentExpChannel, true);
						}
						refreshList(false);
					}
					navFolderRef.get().dismiss();
				});
			}
			if (!navFolderFragment.isAdded()) {
				navFolderFragment.resizeLayout(false);
				navFolderFragment.show(a.getSupportFragmentManager(), "vfld");
				if (folderViewDirty) {
					navFolderFragment.foldViewAdapter.refreshList(false);
				}
			}
		}
	}
	
	private void refreshList(boolean findCurrentPos) {
		if (findCurrentPos) {
			View sv = recyclerView.getChildAt(0);
			if (sv!=null) {
				int position = ((ViewHolder)sv.getTag()).getLayoutPosition();
				if (position>=0 && position<displayNodes.size()) {
					NavigationNode node = (NavigationNode) displayNodes.get(position);
					lastSelectionId = CMN.id(node.getContent());
				}
				lastSelectionOffset = sv.getTop();
			}
		}
		findSelection = true;
		refresh(navRoot.getChildList(isFolderView), 0);
		if (findCurrentPos && !findSelection) {
			((LinearLayoutManager)recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastSelectionPos, lastSelectionOffset);
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getActionMasked();
		if (action ==MotionEvent.ACTION_DOWN) {
			if(touchHelper!=null) {
				boolean fastDrag = true;
				if (fastDrag) {
					touchHandler.removeDragDecoration(draggingView);
				}
				ViewHolder vh = (ViewHolder) ((View)v.getParent()).getTag();
				int draggingPosSt = vh.getLayoutPosition();
				if (draggingPosSt!=displayNodes.size()-1 && (fastDrag||!touchHandler.hasDragDecoration)) {
					//lastDragToPosition = -1;
					isDraggingDown = true;
					CMN.Log("—— startDrag ——");
					draggingView = vh;
					lastDragFromPosition = lastDragToPosition = draggingPosSt;
					draggingPosStPvNode = draggingPosSt>0? (NavigationNode) displayNodes.get(draggingPosSt-1) :null;
					draggingNode = (NavigationNode) displayNodes.get(draggingPosSt);
					touchHandler.startDrag(vh);
					touchHelper.startDrag(vh);
				}
			}
		} else if (action ==MotionEvent.ACTION_UP) {
			isDraggingDown = false;
		}
		return true;
	}
	
	private ViewHolder longClickView;
	
	View.OnLongClickListener itemSortStLi = view -> {
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		if (recyclerView instanceof DragSelectRecyclerView) {
			//selMode = true;
			if(getSelMode() && recyclerView.mLastTouchX>view.getWidth()/3) {
				((DragSelectRecyclerView)recyclerView).setDragSelectActive(true, viewHolder.getLayoutPosition());
				//view.jumpDrawablesToCurrentState();
				if (viewHolder.selected) {
					Drawable bg = viewHolder.itemView.getBackground();
					viewHolder.itemView.setBackground(null);
					viewHolder.itemView.setBackground(bg);
				}
				return true;
			}
		}
		longClickView = viewHolder;
		activeNavHomeBeanPos = longClickView.getLayoutPosition();
		activeNavHomeBean = (NavigationNode) displayNodes.get(activeNavHomeBeanPos);
		showPopupMenu(null);
		return true;
	};
	
	private void showPopupMenu(View headerView) {
		PopupMenuHelper popupMenu = a.getPopupMenu();
		boolean isFolderView = headerView!=null||this.isFolderView;
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
					NavigationNode node = activeNavHomeBean;
					int index = activeNavHomeBeanPos;
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
							NewNode_isFolder = true;
							NewNode_insertAfter = node;
							showNavEditorDlg(null, "", node.getName());
						} break;
						case R.id.new_node:{ // 新建导航节点
							NewNode_isFolder = false;
							NewNode_insertAfter = node;
							showNavEditorDlg(null, null, null);
						} break;
						case R.string.xinjiandaohangjiedian:{ // 新建导航节点
							NewNode_isFolder = false;
							NewNode_insertAfter = node;
							showNavEditorDlg(null, "", "");
						} break;
						case R.string.bianji:{ // 编辑
							showNavEditorDlg(node, null, null);
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
							if (getSelMode() && selection.contains(node)) {
								delSz = selection.size();
							}
							if (delSz>1) {
								title += "等"+delSz+"个节点吗？";
							} else {
								title += "吗？";
							}
							AlertDialog.Builder builder2 = new AlertDialog.Builder(a);
							builder2.setTitle(title)
									.setPositiveButton(R.string.delete, (dialog, which) -> {
										if (getSelMode() && selection.contains(node)) {
											NavigationNode bigNode = resolveBigNode(node, selection);
											NavigationNode[] arr = selection.toArray(new NavigationNode[]{});
											selection.clear();
											for (NavigationNode n : arr) {
												removeChild(n);
												mRecyclerBin.add(n);
											}
											refreshList(false);
										} else {
											removeChild(node);
											mRecyclerBin.add(node);
											notifyItemRemoved(index);
										}
									})
									.setNegativeButton(R.string.cancel, null)
							;
							AlertDialog dTmp = builder2.create();
							dTmp.show();
							((TextView)dTmp.findViewById(R.id.alertTitle)).setSingleLine(false);
							Window window = dTmp.getWindow();
							WindowManager.LayoutParams lp = window.getAttributes();
							lp.dimAmount =0f;
							window.setAttributes(lp);
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
			popupMenu.show(recyclerView, recyclerView.mLastTouchX, recyclerView.mLastTouchY);
		} else {
			int[] vLocationOnScreen = new int[2];
			recyclerView.getLocationOnScreen(vLocationOnScreen);
			popupMenu.show(a.root, recyclerView.mLastTouchX+vLocationOnScreen[0], recyclerView.mLastTouchY+vLocationOnScreen[1]);
		}
		Utils.preventDefaultTouchEvent(root, -100, -100);
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ViewHolder ret = new ViewHolder(a.getLayoutInflater().inflate(isFolderView?R.layout.nav_list_folder_item:R.layout.nav_list_item, parent, false), isFolderView);
		//ret.ivArrow.setOnClickListener(this);
		ret.itemView.setOnClickListener(this);
		ret.dragHandleView.setOnTouchListener(this);
		if (isFolderView) {
			ret.fvGoBtn.setOnClickListener(this);
		}
		ret.itemView.setOnLongClickListener(itemSortStLi);
		//ret.dragHandleView.setVisibility(View.GONE);
		return ret;
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		onBindViewHolder(holder, position, Collections.emptyList());
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position, @NonNull List<Object> payloads) {
		NavigationNode node = (NavigationNode) displayNodes.get(position);
		if (node==footNode) {
			//holder.tvName.setText(" --- --- ");
			holder.itemView.setVisibility(View.INVISIBLE);
			holder.itemView.getLayoutParams().height=bottomPaddding;
			return;
		}
		holder.itemView.getLayoutParams().height=-2;
		holder.MarkSelected(getSelMode() && selection.contains(node));
		//CMN.Log(payloads.toArray());
		Object pl = null;
		if (position==getOldPosition(holder) && payloads.size()>0) {
			pl = payloads.get(0);
		}
		if(pl=="0" && position==getOldPosition(holder)) {
			//CMN.Log("更新选择");
			return;
		}
		if (!node.isLeaf()) {
			holder.ivArrow.setRotation(node.isExpand(currentExpChannel) ? 90 : 0);
		}
		if(pl=="1") {
			//CMN.Log("更新折叠");
			return;
		}
		JSONObject json = node.getContent();
		String name = json.getString("name");
		int h = node.getHeight();
		// debugShowChildSz
		//if(!node.isLeaf()) name += " " + node.getChildList().size() + " : "+ node.children.size();
		if (TextUtils.isEmpty(name)) {
			name = "( " + json.getString("url") + " )";
		}
		if (node.isLeaf()) {
			holder.ivArrow.setVisibility(h==1?View.GONE:View.INVISIBLE);
		} else {
			holder.ivArrow.setVisibility(View.VISIBLE);
		}
		if(h>1 && node.isLeaf()) {
			h-=1;
		}
		//holder.itemView.setPaddingRelative(h * padding, 3, 3, 3);
		holder.itemView.setPadding(h * padding, 3, 3, 3);
		boolean indentNxtLn = false;
		if(indentNxtLn) {
			SpannableString spannableString = new SpannableString(name);
			LeadingMarginSpan.Standard what =new LeadingMarginSpan.Standard(0, (int) (GlobalOptions.density*7));
			spannableString.setSpan(what, 0, spannableString.length(), SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
			holder.tvName.setText(spannableString);
		} else {
			holder.tvName.setText(name);
		}
		holder.tvName.setSingleLine(!getShowMultilineText());
		if (!isFolderView) {
			holder.dragHandleView.setVisibility((getShowDragHandle()||getEditMode())?View.VISIBLE:View.GONE);
		}
		holder.itemView.setVisibility(View.VISIBLE);
	}
	
	private int getOldPosition(ViewHolder holder) {
		int ret=holder.getOldPosition();
		if (ret==-1) {
			return holder.getLayoutPosition();
		}
		return ret;
	}
	
	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final ImageView ivArrow;
		public final TextView tvName;
		public final View dragHandleView;
		public View fvGoBtn;
		public boolean selected;
		private  Drawable background;
		
		public ViewHolder(View rootView, boolean isFolder) {
			super(rootView);
			this.ivArrow = rootView.findViewById(R.id.iv_arrow);
			this.tvName = rootView.findViewById(R.id.tv_name);
			this.dragHandleView = rootView.findViewById(R.id.dragHandle);
			if (isFolder) {
				this.fvGoBtn = rootView.findViewById(R.id.search_go_btn);
			}
			itemView.setTag(this);
		}
		
		public void MarkSelected(boolean selected) {
			if (this.selected ^ selected) {
				if (this.selected = selected) {
					if (background ==null) {
						background = itemView.getBackground();
					}
					//tvName.setTextColor(Color.WHITE);
					itemView.setBackgroundResource(R.drawable.frame_selected_layer);
				} else {
					//tvName.setTextColor(Color.BLACK);
					itemView.setBackground(background);
				}
			}
		}
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.ivBack:
				if (bIsShowing) {
					dismiss(false);
				}
			break;
			case R.id.search:
			
			break;
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
					InitDragSort();
				}
				if (!getShowDragHandle()) {
					notifyDataSetChanged();
				}
				ld.getDrawable(1).setAlpha(editMode?255:0);
			} break;
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
								notifyDataSetChanged();
							} break;
							case R.string.showMultilineText:{
								v.setActivated(toggleShowMultilineText());
								notifyDataSetChanged();
							} break;
							case R.string.allowDragSort:{
								boolean dragHandle = toggleShowDragHandle();
								v.setActivated(dragHandle);
								if (dragHandle) {
									InitDragSort();
								}
								notifyDataSetChanged();
							} break;
							case R.string.hangjianlianxuan:{
								View blinkView = null;
								if (!getSelMode()) {
									blinkView = popupMenuHelper.lv.getChildAt(0);
								} else {
									int selSt = selInterval[0];
									int selEd = selInterval[2];
									boolean valid = selSt!=selEd;
									if (valid) {
										if (selSt<0 || selSt>=displayNodes.size() || CMN.id(displayNodes.get(selSt).getContent())!=selInterval[1]) {
											valid = false;
										}
										if (selEd<0 || selEd>=displayNodes.size() || CMN.id(displayNodes.get(selEd).getContent())!=selInterval[3]) {
											valid = false;
										}
									}
									if (valid) {
										if (selSt>selEd) {
											int tmp = selEd;
											selEd = selSt;
											selSt = tmp;
										}
										boolean removeSel = !selection.contains(displayNodes.get(selSt)) && !selection.contains(displayNodes.get(selEd));
										for (int i = selSt; i < selEd; i++) {
											if (removeSel) {
												selection.remove(displayNodes.get(i));
											} else {
												selection.add((NavigationNode) displayNodes.get(i));
											}
										}
										notifyDataSetChanged();
									} else {
										blinkView = v;
									}
								}
								if (blinkView!=null) {
									Utils.blinkView(blinkView, false);
									dismiss = false;
								}
							} break;
							case R.string.foldAll: {
								navRoot.collapseAll(currentExpChannel);
								refreshList(true);
							} break;
							case R.string.expAll: {
								navRoot.expandAll(currentExpChannel);
								refreshList(true);
							} break;
							case R.id.level: {
								View svp = (View) v.getParent();
								boolean collapse = Utils.getViewIndex(svp)==3;
								int level = Utils.getViewIndex(v)+1;
								if (collapse) {
									navRoot.collapseLevel(currentExpChannel, level);
								} else {
									navRoot.expandLevel(currentExpChannel, level);
								}
								refreshList(true);
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
				popupMenu.modifyMenu(0, null, getSelMode());
				popupMenu.modifyMenu(6, null, getShowMultilineText());
				popupMenu.modifyMenu(7, null, getShowDragHandle());
				popupMenu.show(a.root, a.root.getWidth(), 0);
			} break;
			case R.id.clearText:
				etSearch.setText(null);
				view.setVisibility(View.GONE);
			break;
			case R.id.search_go_btn: {
				ViewHolder viewHolder = (ViewHolder) ((ViewGroup)view.getParent()).getTag();
				int position = viewHolder.getLayoutPosition();
				NavigationNode node = (NavigationNode) displayNodes.get(position);
				//a.showT(node.getName());
				if(folderNavListener!=null) {
					folderNavListener.onNavToFolder(node);
				}
			} break;
//			case R.id.iv_arrow: {
//				performIvArrowClk(longClickView);
//			} break;
			case R.id.itemRoot: {
				super.onClick(view);
			} break;
		}
	}
	
	public void InitDragSort() {
		if(touchHelper==null) {
			TouchSortHandler.MoveSwapAdapter itemTouchAdapter = new TouchSortHandler.MoveSwapAdapter() {
				@Override
				public void onMove(int fromPosition, int toPosition) {
					if (fromPosition==displayNodes.size()-1 || toPosition==displayNodes.size()-1){
						return;
					}
					CMN.Log("—— onMove ——", fromPosition, toPosition);
					TreeViewNode hotNode = displayNodes.get(fromPosition);
					if (!hotNode.isLeaf() && hotNode.isExpand(currentExpChannel)) {
						CMN.Log("—— onMove collapse ——", hotNode);
						// 先折叠
						collapseNode(fromPosition);
						notifyItemChanged(fromPosition, "1");
					} else {
						lastDragToPosition = toPosition;
						moveDisplayNode(fromPosition, toPosition);
					}
				}
				
				@Override
				public void onDragFinished(RecyclerView.ViewHolder viewHolder) {
					final NavigationNode fromNode = draggingNode;
					if(fromNode != null && viewHolder!=null && touchHandler.isDragging) {
						int toPosition = lastDragToPosition;
						//if(toPosition>=displayNodes.size()-1) {
						//	toPosition = displayNodes.size()-2;
						//}
						boolean bNotMoved = lastDragFromPosition==toPosition;
						//if (bNotMoved) a.showT("111");
						// 所移之节近乎下矣。
						boolean parentToBottomSame = viewHolder.itemView.getTranslationY()>0 || toPosition==0;
						// 批量移动
						boolean moveMultiple = getSelMode() && selection.contains(fromNode) && selection.size()>1;
						CMN.Log("—— onDragFinished ——", parentToBottomSame, viewHolder.itemView.getTranslationY(), toPosition);
						if (moveMultiple) {
							boolean topInval=false, botInval=false;
							if (toPosition>0) {
								topInval = selection.contains(displayNodes.get(toPosition-1));
							}
							if (toPosition+1<displayNodes.size()) {
								botInval = selection.contains(displayNodes.get(toPosition+1));
							}
							boolean invalidMove = topInval&&botInval;
							if (invalidMove) {
								moveDisplayNode(toPosition, lastDragFromPosition);
								Utils.blinkView(draggingView.itemView, true);
								//moveMultiple = false;
								return;
							}
						}
						NavigationNode toNode = (NavigationNode) displayNodes.get(toPosition+(parentToBottomSame?1:-1));
						NavigationNode np = (NavigationNode) fromNode.getParent();// 所移节点之父
						NavigationNode mp = (NavigationNode) toNode.getParent();// 近乎上则此值为上之父，否则为下之父
						CMN.Log(toNode.getName(), mp.getName());
						if(!parentToBottomSame) {
							// 所移之节近乎上，上临当显，上父自为，易哉。
							if(!toNode.isLeaf() && toNode.isExpand(currentExpChannel)) {
								mp = toNode;
							}
							// 重取下临
							toNode = (NavigationNode) displayNodes.get(toPosition+1);
							if(mp!=toNode.getParent()) {
								// 下父悖于上，无临，取其末也。
								toNode = null;
							}
						}
						if(toNode==footNode) {
							toNode = null;
						}
						//CMN.Log(np, mp);
						//CMN.Log(fromNode, toNode);
						if(!moveMultiple) {
							if(mp!=fromNode && np!=fromNode) {
								if(np!=mp) {
									removeChild(fromNode);
									mp.insertNodeBefore(toNode, fromNode);
								} else {
									if (bNotMoved) {
										// 同福不移
										return;
									}
									np.reorderNodeBefore(toNode, fromNode);
								}
							}
						}
						else if(!bNotMoved) {
							int index = 0;
							List<TreeViewNode> cl = mp.getChildList();
							if (toNode!=null && selection.contains(toNode)) {
								index = cl.indexOf(toNode);
								for (int i = index+1,len=cl.size(); i <= len; i++) {
									if (i==len) {
										toNode = null;
									} else if (!selection.contains(cl.get(i))) {
										toNode = (NavigationNode) cl.get(i);
										break;
									}
								}
							}
							int sz = selection.size();
							HashSet<NavigationNode> selections = new HashSet<>(sz);
							selections.addAll(selection); selection.clear();
							np = resolveBigNode(np, selections);
							ArrayList<NavigationNode> flattenedSelection = new ArrayList<>(selections.size());
							TraverseChildTree(np, new TreeTraveller<NavigationNode>() {
								int cc=0;
								@Override
								public boolean onNodeReached(NavigationNode node) {
									if (selections.contains(node)) {
										removeChild(node);
										cc++;
										flattenedSelection.add(node);
										return true;
									}
									return false;
								}
								@Override
								public boolean ended() {
									return cc>=sz;
								}
							});
							// insert to mp
							if (toNode==null) {
								index = cl.size();
							} else {
								index = mp.fastIndexOf(toNode, index);
							}
							NavigationNode node;
							for (int i = 0, len=flattenedSelection.size(); i < len; i++) {
								node = flattenedSelection.get(i);
								mp.insertNode(index+i, node);
								selection.add(node);
							}
							refreshList(false);
						}
						if(!np.checkHarmony()
								|| !mp.checkHarmony()) {
							CMN.Log(np.getName(), np.checkHarmony(), mp.getName(), mp.checkHarmony());
							CMN.Log(np, mp);
							CMN.Log("dragging="+fromNode.toString());
							CMN.Log("toNode="+toNode);
							throw new IllegalStateException("Not match tree!");
						}
						MarkDirty();
						recyclerView.post(() -> notifyItemChanged(toPosition));
						//draggingNode = null;
						// 别看了古文是炼气士发明的！
					}
				}
				
				@Override
				public void onSwiped(int position) {
					displayNodes.remove(position);
					notifyItemRemoved(position);
				}
			};
			touchHelper = new ItemTouchHelper(touchHandler=new TouchSortHandler(itemTouchAdapter, ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0));
			touchHandler.touchHelper = touchHelper;
			touchHandler.mItemPadEnd = 1;
			touchHandler.mBottomScrollTol = bottomPaddding;
			touchHandler.dragBackground = new ColorDrawable(Color.LTGRAY);
			touchHelper.attachToRecyclerView(recyclerView);
		}
	}
	
	/** Resolve the big node for the selection. The selection will be modified. */
	private NavigationNode resolveBigNode(NavigationNode np, HashSet<NavigationNode> selections) {
		int sz =  selections.size();
		NavigationNode bigNode = np;
		int bigNodeHeight = bigNode.getHeight();
		HashSet<NavigationNode> treeNodes = new HashSet<>(sz/2);
		NavigationNode[] arr = selections.toArray(new NavigationNode[sz]);
		for (NavigationNode node:arr) {
			if (node.getParent().getHeight()<bigNodeHeight) {
				bigNode = (NavigationNode) node.getParent();
				bigNodeHeight = bigNode.getHeight();
			}
			if (!node.isLeaf()) {
				treeNodes.add(node);
			}
		}
		for (NavigationNode node:arr) {
			if (node.isLeaf()) {
				while((np = (NavigationNode) node.getParent())!=null) {
					if (treeNodes.contains(np)) {
						selections.remove(np);
						break;
					}
				}
			}
		}
		return bigNode;
	}
	
	private void moveDisplayNode(int fromPosition, int toPosition) {
		if (fromPosition < toPosition) {
			for (int i = fromPosition; i < toPosition; i++) {
				swap(i, i + 1);
			}
		} else {
			for (int i = fromPosition; i > toPosition; i--) {
				swap(i, i - 1);
			}
		}
		//TreeViewNode item = displayNodes.remove(fromPosition);
		//displayNodes.add(toPosition, item);
		notifyItemMoved(fromPosition, toPosition);
	}
	
	private void swap(int i, int j) {
		NavigationNode nI = (NavigationNode) displayNodes.get(i);
		NavigationNode nJ = (NavigationNode) displayNodes.set(j, nI);
		displayNodes.set(i, nJ);
		//Collections.swap(displayNodes, i, j);
		//NavigationNode np = (NavigationNode) nI.getParent();
		//if(np!=nJ.getParent()) {
		//
		//} else {
		//	np.getChildList().remove()
		//}
	}
	
	public boolean dismiss(boolean clearSel) {
		if(bIsShowing) {
			if (recyclerView instanceof DragSelectRecyclerView && ((DragSelectRecyclerView) recyclerView).getDragSelectActive()) {
				return true;
			}
			if (clearSel && getSelMode()
					&& selection.size()>0
			) {
				selection.clear();
				//toggleSelMode();
				notifyDataSetChanged();
			} else {
				checkDirty();
				toggle();
			}
			return true;
		}
		return false;
	}
	
	
	private void checkDirty() {
		if (navRoot.isDirty) {
			if (glideSaver==null) {
				glideSaver = Glide.with(a)
					.load(navRoot.saveTask)
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
	
	public void OnNavHomeEditorActions(int action) {
		AlertDialog editNavHomeDlg = (AlertDialog) a.getReferencedObject(WeakReferenceHelper.edit_navhome);
		if(editNavHomeDlg!=null) {
			LoginViewBinding schEngEditView = (LoginViewBinding) editNavHomeDlg.tag;
			int dismiss = 0;
			switch (action) {
				case 0: {
					dismiss = 0x7;
					if (editingNavHomeBean!=null && !editingNavHomeBean.isLeaf()) {
						// 使用文件夹
						if (bIsShowing && longClickView!=null) {
							performIvArrowClk(longClickView);
						}
						dismiss = 0x1|0x2;
					} else {
						a.execBrowserGoTo(schEngEditView.url.getText().toString());
					}
				} break;
				case 1:{
					dismiss = 0x1;
					schEngEditView.url.setText(a.currentWebView.getUrl());
					schEngEditView.name.setText(a.currentWebView.getTitle());
				} break;
				case 2:{
					dismiss = 0x1;
					boolean sl = !getAlwaysShowMultilineEditField();
					schEngEditView.url.setSingleLine(sl);
					schEngEditView.name.setSingleLine(sl);
				} break;
			}
			if((dismiss&0x1)!=0) {
				NavHomeEditorDialogSettings settingsPanel = (NavHomeEditorDialogSettings) schEngEditView.getRoot().getTag();
				settingsPanel.dismiss();
			}
			if((dismiss&0x2)!=0) {
				editNavHomeDlg.dismiss();
			}
			if((dismiss&0x4)!=0) {
				dismiss(false);
			}
		}
	}
	
	private void performIvArrowClk(ViewHolder holder) {
		int position = holder.getLayoutPosition();
		TreeViewNode node = displayNodes.get(position);
		if(!node.isLeaf()) {
			OnTreeNodeListener listener = onTreeNodeListener;
			onTreeNodeListener = null;
			super.onClick(holder.itemView);
			onTreeNodeListener = listener;
			notifyItemChanged(position, "1");
		}
	}
	
	public boolean isVisible() {
		return bIsShowing;
	}
	
	@Multiline(flagPos=58, shift=0) private boolean getAlwaysShowMultilineEditField(){ a.opt.FirstFlag=a.opt.FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=59, shift=1) private boolean getAppendNewNavNodeToEnd(){ a.opt.FirstFlag=a.opt.FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=60, shift=1) private boolean getAutoExpandAllFoldersForFov(){ a.opt.FirstFlag=a.opt.FirstFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=25) private boolean getShowMultilineText(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=25) private boolean toggleShowMultilineText(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=26) private boolean getShowDragHandle(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=26) private boolean toggleShowDragHandle(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=27) private boolean getSelMode(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=27) private boolean toggleSelMode(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=28) private boolean getEditMode(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=28) private boolean toggleEditMode(){ a.opt.ThirdFlag=a.opt.ThirdFlag; throw new IllegalArgumentException(); }

}
