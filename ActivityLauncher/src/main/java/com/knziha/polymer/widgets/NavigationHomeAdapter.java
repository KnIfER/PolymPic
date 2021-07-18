package com.knziha.polymer.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.AppIconCover.AppIconCover;
import com.knziha.polymer.browser.AppIconCover.AppLoadableBean;
import com.knziha.polymer.webslideshow.TouchSortHandler;
import com.shockwave.pdfium.treeview.TreeViewAdapter;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static com.knziha.polymer.webslideshow.ImageViewTarget.FuckGlideDrawable;
import static com.knziha.polymer.widgets.NavigationNode.removeChild;

public class NavigationHomeAdapter extends TreeViewAdapter<NavigationHomeAdapter.ViewHolder>
		implements View.OnTouchListener, DragSelectRecyclerView.IDragSelectAdapter {
	protected View root;
	protected RecyclerView recyclerView;
	JSONObject jsonData = new JSONObject();
	NavigationRootNode navRoot;
	boolean bIsShowing;
	private EditTextmy etSearch;
	TouchSortHandler touchHandler;
	ItemTouchHelper touchHelper;
	private NavigationNode draggingNode;
	private ViewHolder draggingView;
	NavigationNode editingNavHomeBean;
	NavigationNode activeNavHomeBean;
	private ArrayList<NavigationNode> mRecyclerBin = new ArrayList<>();
	public int activeNavHomeBeanPos;
	
	HashSet<NavigationNode> selection = new HashSet<>();
	int folderSelCnt;
	private int lastDragFromPosition;
	private int lastDragToPosition;
	private NavigationNode draggingPosStPvNode;
	private boolean isDraggingDown;
	private NavViewListener navListener;
	private LayoutInflater inflater;
	
	@Override
	public void setSelected(int idx, boolean selected) {
		if (getSelMode() && idx>=0 && idx<displayNodes.size()-1) { // sanity check
			NavigationNode node = (NavigationNode) displayNodes.get(idx);
			//selected = !selection.containsKey(node);
			int dn = selected?1:-1;
			if (selected?selection.add(node):selection.remove(node)) {
				if (!node.isLeaf()) folderSelCnt += dn;
				notifyItemChanged(idx, "0");
			}
		}
	}
	
	public boolean select_between_nodes() {
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
			NavigationNode n;
			int dn = removeSel?-1:1;
			for (int i = selSt; i < selEd; i++) {
				n = (NavigationNode) displayNodes.get(i);
				if (removeSel?selection.remove(n):selection.add(n)) {
					if (!n.isLeaf()) folderSelCnt += dn;
				}
			}
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	public TreeViewNode getDisplayedNodeAt(int position) {
		return displayNodes.get(position);
	}
	
	public void NavToFolder(NavigationNode node) {
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
	}
	
	public NavigationHomeAdapter(File bmFile, NavViewListener navListener)
	{
		super(null);
		this.navListener = navListener;
		//File f = bmFile;//new File(a.getExternalFilesDir(null), "Bookmarks.json");
		try (FileInputStream fin = new FileInputStream(bmFile)){
			jsonData = JSON.parseObject(fin, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		navRoot = new NavigationRootNode(bmFile, jsonData, navigation);
		
		rootNode = navRoot;
		
//		footNode = new NavigationNode(new JSONObject());
//		footNode.setParent(rootNode);
		//setPadding(15);
	}
	
	public NavigationHomeAdapter(NavigationHomeAdapter parentView)
	{
		super(null);
		rootNode = parentView.rootNode;
		navRoot = parentView.navRoot;
		this.navListener = parentView.navListener;
		isFolderView = true;
//		bottomPaddding = 0;
		currentExpChannel=0x2;
		normalExpChannel=0x2;
		schViewExpChannel=0x2;
	}
	
	public void clearSel() {
		selection.clear();
		folderSelCnt = 0;
		notifyDataSetChanged();
	}
	
	public void deleteSel() {
		NavigationNode node = activeNavHomeBean;
		int index = activeNavHomeBeanPos;
		if (index>=0&&index<displayNodes.size() && node==displayNodes.get(index)) {
			boolean bIsInSel = selection.contains(node);
			if (bIsInSel && getSelMode()) {
				NavigationNode bigNode = resolveBigNode(node, selection);
				NavigationNode[] arr = selection.toArray(new NavigationNode[]{});
				selection.clear();
				for (NavigationNode n : arr) {
					removeChild(n);
					mRecyclerBin.add(n);
				}
				refreshList(false);
			} else {
				if (!node.isLeaf()) {
					collapseNode(index);
				}
				node.tag = node.getIdsPath();
				removeChild(node);
				displayNodes.remove(node);
				mRecyclerBin.add(node);
				notifyItemRemoved(index);
				if (bIsInSel) {
					selection.remove(node);
				}
			}
			MarkDirty();
		}
	}
	
	static class NavigationRootNode extends NavigationNode implements AppLoadableBean
	{
		public final AppIconCover saveTask = new AppIconCover(this);
		final File f;
		final JSONObject jsonRoot;
		boolean isDirty;
		
		public NavigationRootNode(File f, JSONObject jsonRoot, @NonNull JSONObject itemData) {
			super(null, itemData);
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
				jsonRoot.put("id_prefix", id_prefix);
				jsonRoot.put("id_max", id_max);
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

		return root;
	}
	
	public void SetRecyclerView(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
		inflater = LayoutInflater.from(recyclerView.getContext());
		LinearLayoutManager lm = new LinearLayoutManager(recyclerView.getContext());
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
					} else if(navListener!=null){
						CMN.Log(lastX, width/3);
						//a.showT("编辑节点");
						longClickView = (ViewHolder) holder;
						navListener.showEditorDlg(NavigationHomeAdapter.this, (NavigationNode) node, null, null);
					}
					return true;
				} else {
					if (!node.isLeaf()) {
						onToggle(!node.isExpand(currentExpChannel), holder);
					} else {
						Object content = node.getContent();
						if(content instanceof JSONObject) {
							navListener.UseNodeData(NavigationHomeAdapter.this, node);
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
//		ViewGroup root = a.UIData.webcoord;
//		float targetAlpha = 1;
//		float targetTrans = 0;
//		View viewToAdd = getNavigationView();
//		if (bIsShowing=!bIsShowing) {
//			Utils.addViewToParent(viewToAdd, root, -1);
//			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess_ic_back);
//			a.UIData.browserWidget8.setImageResource(R.drawable.ic_baseline_book_24);
//		} else {
//			targetAlpha = 0;
//			targetTrans = bottomPaddding;
//			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess);
//			a.UIData.browserWidget8.setImageResource(R.drawable.chevron_forward);
//			a.postResoreSel();
//			etSearch.clearFocus();
//		}
//		viewToAdd
//				.animate()
//				.alpha(targetAlpha)
//				.translationY(targetTrans)
//				.setDuration(180)
//				.setListener(bIsShowing?null:animatorListenerAdapter)
//				//.start()
//		;
	}
	
	boolean folderViewDirty;
	
	void MarkDirty() {
		navRoot.isDirty = true;
		folderViewDirty = true;
	}
	
	protected void refreshList(boolean findCurrentPos) {
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
		if (navListener!=null) {
			navListener.showPopupMenu(this, null);
		}
		return true;
	};
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ViewHolder ret = new ViewHolder(inflater.inflate(isFolderView?R.layout.nav_list_folder_item:R.layout.nav_list_item
				, parent
				, false)
				, isFolderView);
		//ret.ivArrow.setOnClickListener(this);
		ret.itemView.setOnClickListener(this);
		ret.dragHandleView.setOnTouchListener(this);
		if (isFolderView) {
			ret.fvGoBtn.setOnClickListener(navListener);
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
//		if (node==footNode) {
//			//holder.tvName.setText(" --- --- ");
//			holder.itemView.setVisibility(View.INVISIBLE);
//			holder.itemView.getLayoutParams().height=bottomPadding;
//			return;
//		}
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
		holder.node = node;
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
		private Drawable background;
		NavigationNode node;
		
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
							selections.addAll(selection); selection.clear(); folderSelCnt=0;
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
								selection.add(node); if (!node.isLeaf()) folderSelCnt++;
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
			//touchHandler.mBottomScrollTol = bottomPadding;
			touchHandler.dragBackgroundColor = Color.LTGRAY;
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
				np = node;
				while((np = (NavigationNode) np.getParent())!=null) {
					if (treeNodes.contains(np)) {
						selections.remove(node);
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
	
	public void OnNavHomeEditorActions(int action) {

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
	
	private boolean getAutoExpandAllFoldersForFov(){
		return false;
	}
	
	private boolean getShowMultilineText(){
		return navListener!=null && navListener.getShowMultilineText();
	}
	
	private boolean getShowDragHandle(){
		return navListener!=null && navListener.getShowDragHandle();
	}
	
	private boolean getSelMode(){
		return navListener!=null && navListener.getSelMode();
	}
	
	private boolean getEditMode(){
		return navListener!=null && navListener.getEditMode();
	}
}
