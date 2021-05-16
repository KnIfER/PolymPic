package com.knziha.polymer.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.shockwave.pdfium.treeview.TreeViewAdapter;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class NavigationHomeAdapter extends TreeViewAdapter<NavigationHomeAdapter.ViewHolder> implements View.OnTouchListener {
	BrowserActivity a;
	protected ViewGroup root;
	protected RecyclerView recyclerView;
	JSONObject jsonData = new JSONObject();
	NavigationNode navRoot;
	boolean bIsShowing;
	final int bottomPaddding;
	private EditTextmy etSearch;
	TouchSortHandler touchHandler;
	ItemTouchHelper touchHelper;
	private NavigationNode draggingNode;
	private ViewHolder draggingView;
	
	public NavigationHomeAdapter(BrowserActivity activity, int bottomPaddding)
	{
		super(null);
		a = activity;
		this.bottomPaddding = bottomPaddding;
		
		File f = new File(a.getExternalFilesDir(null), "Bookmarks.json");
		try {
			jsonData = JSON.parseObject(new FileInputStream(f), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject roots = jsonData.getJSONObject("roots");
		
		if(roots!=null) {
			JSONObject navigation = roots.getJSONObject("navigation");
			if(navigation!=null) {
				navRoot = new NavigationNode(navigation);
			}
		}
		if (navRoot==null) {
			navRoot = new NavigationNode(new JSONObject());
		}
		rootNode = navRoot;
		footNode = new NavigationNode(new JSONObject());
		footNode.setParent(rootNode);
		//setPadding(15);
	}
	
	static class NavigationNode extends TreeViewNode<JSONObject>
	{
		JSONArray children;
		public NavigationNode(@NonNull JSONObject itemData) {
			super(itemData);
			JSONArray children = content.getJSONArray("children");
			if (children!=null) {
				this.children = children;
				JSONObject child;
				for (int i = 0, len=children.size(); i < len; i++) {
					child = children.getJSONObject(i);
					if (child!=null) {
						NavigationNode node = new NavigationNode(child);
						childList.add(node);
						node.parent=this;
					}
				}
			}
		}
		
		public void removeChild(NavigationNode hotNode) {
			int index = childList.indexOf(hotNode);
			if(index>=0) {
				childList.remove(index);
			}
			index = fastIndexOf(hotNode.content, index);
			if(index>=0) {
				children.remove(index);
			}
			hotNode.parent = null;
		}
		
		private int fastIndexOf(JSONObject content, int index) {
			if(index>=0 && index<children.size() && children.get(index)==content) {
				return index;
			}
			return children.indexOf(index);
		}
		
		public TreeViewNode insertNodeBefore(NavigationNode idx, NavigationNode node) {
			if (childList == null)
				childList = new ArrayList<>();
			if(idx==null) {
				childList.add(node);
				children.add(node.content);
			} else {
				int index = Math.max(0, childList.indexOf(idx));
				childList.add(index, node);
				index = Math.max(0, fastIndexOf(idx.content, index));
				children.add(index, node.content);
			}
			
			node.parent = this;
			node.height=UNDEFINE;
			return this;
		}
		
		@Override
		public boolean isLeaf() {
			return children==null;
		}
		
		public void reorderNodeBefore(NavigationNode toNode, NavigationNode draggingNode) {
			if(toNode==null) {
				removeChild(draggingNode);
				addChild(draggingNode);
			} else {
				int fromPosition = childList.indexOf(draggingNode);
				int toPosition = childList.indexOf(toNode);
				//CMN.Log("reorderNodeBefore::", fromPosition, toPosition);
				if (fromPosition < toPosition) {
					for (int i = fromPosition; i < toPosition-1; i++) {
						Collections.swap(childList, i, i + 1);
					}
				} else {
					for (int i = fromPosition; i > toPosition; i--) {
						Collections.swap(childList, i, i - 1);
					}
				}
				fromPosition = fastIndexOf(draggingNode.content, fromPosition);
				toPosition = fastIndexOf(toNode.content, toPosition);
				if (fromPosition < toPosition) {
					for (int i = fromPosition; i < toPosition-1; i++) {
						Collections.swap(children, i, i + 1);
					}
				} else {
					for (int i = fromPosition; i > toPosition; i--) {
						Collections.swap(children, i, i - 1);
					}
				}
			}
		}
		
		@Override
		public String toString() {
			return "NavigationNode{" +
					"name=" + content.getString("name") +
					'}';
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		return R.layout.nav_list_item;
	}
	
	@Override
	protected boolean filterNode(TreeViewNode node) {
		String name=((NavigationNode)node).getContent().getString("name");
		boolean ret;
		if ((currentSchFlg&0x3)!=0) { // 不区分大小写
			name = name.toLowerCase();
		}
		if (currentFilter instanceof Pattern) {
			return ((Pattern) currentFilter).matcher(name).find();
		} else if(currentFilter instanceof String) {
			if ((currentSchFlg&0x4)!=0) { // 从头开始比对
				return name.startsWith((String) currentFilter);
			}
			return name.contains((String) currentFilter);
		}
		return false;
	}
	
	public ViewGroup getNavigationView() {
		if (root==null) {
			root = (ViewGroup) a.getLayoutInflater().inflate(R.layout.nav_view, a.UIData.webcoord, false);
			//root.setPadding(0, 0, 0, bottomPaddding);
			etSearch = root.findViewById(R.id.etSearch);
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
			recyclerView = root.findViewById(R.id.recycler_view);
			LinearLayoutManager lm = new LinearLayoutManager(a);
			recyclerView.setLayoutManager(lm);
			recyclerView.setItemAnimator(null);
			recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
			recyclerView.setHasFixedSize(true);
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
							int pad=node.getHeight()*padding;
							mDivider.setBounds(pad, top, width-padding, top + mDividerHeight);
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
			
			setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
				@Override
				public boolean onClick(TreeViewNode node, RecyclerView.ViewHolder holder) {
					if (!node.isLeaf()) {
						onToggle(!node.isExpand(), holder);
					} else {
						Object content = node.getContent();
						if(content instanceof JSONObject) {
							String url = ((JSONObject) content).getString("url");
							if (url!=null) {
								a.execBrowserGoTo(url);
								if (bIsShowing) {
									toggle(null);
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
			
			refresh(navRoot.getChildList(), 1024);
			lm.scrollToPositionWithOffset(displayNodes.size()-40, 0);
		}
		EnableDragSort();
		return root;
	}
	
	AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			if (!bIsShowing) {
				Utils.removeIfParentBeOrNotBe(root, null, false);
			}
		}
	};
	
	public void toggle(ViewGroup root) {
		float targetAlpha = 1;
		float targetTrans = 0;
		View viewToAdd = getNavigationView();
		if (bIsShowing=!bIsShowing) {
			Utils.addViewToParent(viewToAdd, root);
		} else {
			targetAlpha = 0;
			targetTrans = bottomPaddding;
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
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getActionMasked();
		if (action ==MotionEvent.ACTION_DOWN) {
			if(touchHelper!=null) {
				touchHandler.removeDragDecoration(draggingView);
				ViewHolder vh = (ViewHolder) ((View)v.getParent()).getTag();
				if (vh.getLayoutPosition()!=displayNodes.size()-1) {
					draggingView = vh;
					draggingNode = (NavigationNode) displayNodes.get(vh.getLayoutPosition());
					touchHandler.startDrag(vh);
					touchHelper.startDrag(vh);
				}
			}
		}
		return true;
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ViewHolder ret = new ViewHolder(a.getLayoutInflater().inflate(R.layout.nav_list_item, parent, false));
		ret.itemView.setOnClickListener(this);
		ret.dragHandleView.setOnTouchListener(this);
		//ret.dragHandleView.setVisibility(View.GONE);
		return ret;
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (position>=displayNodes.size()-1) {
			holder.tvName.setText(" --- --- ");
			holder.itemView.getLayoutParams().height=bottomPaddding;
			return;
		}
		holder.itemView.getLayoutParams().height=-2;
		NavigationNode node = (NavigationNode) displayNodes.get(position);
		JSONObject json = node.getContent();
		String name = json.getString("name");
		int h = node.getHeight();
		holder.ivArrow.setRotation(node.isExpand() ? 90 : 0);
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
	}
	
	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final ImageView ivArrow;
		public final TextView tvName;
		public final View dragHandleView;
		
		public ViewHolder(View rootView) {
			super(rootView);
			this.ivArrow = rootView.findViewById(R.id.iv_arrow);
			this.tvName = rootView.findViewById(R.id.tv_name);
			this.dragHandleView = rootView.findViewById(R.id.dragHandle);
			itemView.setTag(this);
		}
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.clearText:
				etSearch.setText(null);
				view.setVisibility(View.GONE);
			break;
			default:
				super.onClick(view);
		}
	}
	
	public void EnableDragSort() {
		if(touchHelper==null) {
			MoveSwapAdapter itemTouchAdapter = new MoveSwapAdapter() {
				@Override
				public void onMove(int fromPosition, int toPosition) {
					if (fromPosition==displayNodes.size()-1 || toPosition==displayNodes.size()-1){
						return;
					}
					//CMN.Log("onMove", fromPosition, toPosition);
					TreeViewNode hotNode = displayNodes.get(fromPosition);
					if (!hotNode.isLeaf() && hotNode.isExpand()) {
//						TreeViewNode toNode = displayNodes.get(toPosition);
//						TreeViewNode toParentNode = toNode.getParent();
//						if(true||toParentNode==hotNode) {
//							hotNode.collapse();
//						} else {
//							hotNode.getParent().removeChild(hotNode);
//							toParentNode.addChild(toNode.getParent().getChildList().indexOf(toNode)
//									, hotNode);
//						}
						// 先折叠
						hotNode.collapse();
						if(hotNode.getChildCount()>0) {
							refresh(navRoot.getChildList(), 0);
						}
					} else {
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
				}
				
				@Override
				public void onDragFinished(RecyclerView.ViewHolder viewHolder) {
					if(draggingNode != null && viewHolder!=null) {
						int toPosition = viewHolder.getLayoutPosition();
						boolean parentToBottomSame = viewHolder.itemView.getTranslationY()>0 || toPosition==0;
						NavigationNode fromNode = draggingNode;
						NavigationNode toNode = (NavigationNode) displayNodes.get(toPosition+(parentToBottomSame?1:-1));
						NavigationNode np = (NavigationNode) fromNode.getParent();
						NavigationNode mp = (NavigationNode) toNode.getParent();
						
						if(!parentToBottomSame) {
							if(!toNode.isLeaf() && toNode.isExpand()) {
								mp = toNode;
							}
							toNode = (NavigationNode) displayNodes.get(toPosition+1);
							if(mp!=toNode.getParent()) {
								toNode = null;
							}
						}
						if(toNode==footNode) {
							toNode = null;
						}
						
						//CMN.Log(np, mp);
						//CMN.Log(fromNode, toNode);
						if(mp!=fromNode && np!=fromNode) {
							if(np!=mp) {
								np.removeChild(fromNode);
								mp.insertNodeBefore(toNode, fromNode);
							} else {
								np.reorderNodeBefore(toNode, fromNode);
							}
						}
						recyclerView.post(() -> notifyItemChanged(toPosition));
						draggingNode = null;
					}
				}
				
				@Override
				public void onSwiped(int position) {
					displayNodes.remove(position);
					notifyItemRemoved(position);
				}
			};
			
			touchHelper = new ItemTouchHelper(touchHandler=new TouchSortHandler(itemTouchAdapter));
			touchHandler.mBottomScrollTol = bottomPaddding;
			touchHelper.attachToRecyclerView(recyclerView);
		}
		
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
	
	
	interface MoveSwapAdapter {
		void onMove(int fromPosition,int toPosition);
		void onSwiped(int position);
		void onDragFinished(RecyclerView.ViewHolder viewHolder);
	}
	
	class TouchSortHandler extends ItemTouchHelper.Callback {
		private MoveSwapAdapter moveSwap;
		private Drawable background = null;
		private RecyclerView.ViewHolder draggingView = null;
		private boolean isDragging=false;
		public TouchSortHandler(MoveSwapAdapter itemTouchAdapter){
			this.moveSwap = itemTouchAdapter;
		}
		@Override
		public boolean isLongPressDragEnabled() {
			return false;
		}
		@Override
		public boolean isItemViewSwipeEnabled() {
			return true;
		}
		@Override
		public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
			int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
			//int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
			if(viewHolder.getLayoutPosition()==displayNodes.size()-1) {
				dragFlags=0;
			}
			final int swipeFlags = 0;
			return makeMovementFlags(dragFlags, swipeFlags);
		}
		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
			int fromPosition = viewHolder.getAdapterPosition();//得到拖动ViewHolder的position
			int toPosition = target.getAdapterPosition();//得到目标ViewHolder的position
			moveSwap.onMove(fromPosition,toPosition);
			return true;
		}
		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
			int position = viewHolder.getAdapterPosition();
			moveSwap.onSwiped(position);
		}
		@Override
		public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
			if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
			{
				//滑动时改变Item的透明度
				final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
				viewHolder.itemView.setAlpha(alpha);
				viewHolder.itemView.setTranslationX(dX);
			} else {
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		}
		@Override
		public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
			if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && draggingView==viewHolder)
			{
				if(viewHolder!=null && background==null) {
					background = viewHolder.itemView.getBackground();
					viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
				}
			}
			super.onSelectedChanged(viewHolder, actionState);
			//CMN.Log("onSelectedChanged::", viewHolder);
			if(isDragging && viewHolder==null) {
				moveSwap.onDragFinished(draggingView);
				isDragging=false;
			}
		}
		
		private final Interpolator sDragViewScrollInterpolator = new LinearInterpolator();
		
		@Override
		public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView
				, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
			//CMN.Log("interpolateOutOfBoundsScroll", viewSize, viewSizeOutOfBounds, totalSize);
			//return super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
			int absOutOfBounds = Math.abs(viewSizeOutOfBounds);
			int direction = (int) Math.signum(viewSizeOutOfBounds);
			float outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize);
			float interpolator = sDragViewScrollInterpolator.getInterpolation(outOfBoundsRatio);
			int maxScroll = (int) (8*GlobalOptions.density);
//			if (absOutOfBounds<Math.max(Math.min(viewSize/2, 35*GlobalOptions.density), bottomPaddding/2+10*GlobalOptions.density)) {
//			} else {
//				interpolator -= 0.2;
//				maxScroll = getMaxDragScroll(recyclerView);
//			}
			return (int) (maxScroll * direction * interpolator);
		}
		
		@Override
		public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
			super.clearView(recyclerView, viewHolder);
			removeDragDecoration(viewHolder);
		}
		
		public void removeDragDecoration(RecyclerView.ViewHolder viewHolder) {
			if(viewHolder!=null && draggingView==viewHolder) {
				viewHolder.itemView.setAlpha(1.0f);
				if (background != null)
				{
					viewHolder.itemView.setBackground(background);
					background = null;
				}
			}
		}
		
		public void startDrag(ViewHolder vh) {
			draggingView = vh;
			isDragging = true;
		}
	}
}
