package com.shockwave.pdfium.treeview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tlh on 2016/10/1 :)
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class TreeViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements View.OnClickListener {
    private static final String KEY_IS_EXPAND = "IS_EXPAND";
    private final List<? extends TreeViewBinderInterface> viewBinders;
    protected final ArrayList<TreeViewNode> displayNodes = new ArrayList<>();
	protected int padding = 30;
    protected OnTreeNodeListener onTreeNodeListener;
    private boolean toCollapseChild;
    
	protected int lastSelectionOffset;
	protected long lastSelectionId;
	protected int lastSelectionPos;
	protected boolean findSelection;
	
	protected Object currentFilter;
	public int currentSchFlg;
	
	protected TreeViewNode rootNode;
	protected TreeViewNode footNode;
	
	protected boolean isFolderView = false;
	
	public int currentExpChannel=0x1;
	protected int normalExpChannel=0x1;
	protected int schViewExpChannel=0x1;
	
	public TreeViewAdapter(List<? extends TreeViewBinderInterface> viewBinders) {
        this(null, viewBinders);
    }

    public TreeViewAdapter(List<TreeViewNode> nodes, List<? extends TreeViewBinderInterface> viewBinders) {
		findDisplayNodes(nodes);
        this.viewBinders = viewBinders;
    }
    
	protected int addChildNodesFiltered(TreeViewNode pNode, int startIndex, int schFlag) {
		if(currentFilter==null) {
			return addChildNodes(pNode, startIndex);
		}
		int addChildCount = 0;
		List<TreeViewNode> nodes = pNode.getChildList(isFolderView);
		boolean schView=(schFlag&0x2)!=0;
		boolean shldAdd=(schFlag&0x1)!=0;
		int schViewExp = schView?schViewExpChannel:normalExpChannel;
		TreeViewNode node;
		for (int i = 0,len=nodes.size(); i < len; i++) {
			node = nodes.get(i);
			boolean added=filterNode(node);
			boolean expanded = node.isExpand(schViewExp);
			int currentIdx = startIndex+addChildCount++;
			if(shldAdd) {
				displayNodes.add(currentIdx, node);
			}
			int flagNxt=schFlag;
			if (shldAdd && !expanded) {
				flagNxt &= ~0x1;
			}
			int childAdd=addChildNodesFiltered(node, currentIdx+1, flagNxt);
			if(childAdd>0)
			{
				if (shldAdd && expanded)
				{
					addChildCount += childAdd;
				}
			} else if (!added) {
				addChildCount--;
				if(shldAdd) {
					displayNodes.remove(currentIdx);
				}
			}
		}
		return addChildCount;
	}
	
	public boolean getIsFolderView() {
		return isFolderView;
	}
	
	public interface TreeTraveller<T extends TreeViewNode> {
		boolean onNodeReached(T node);
		boolean ended();
	}
	
	public <T extends TreeViewNode> void TraverseChildTree(T pNode, TreeTraveller<T> treeTraveller) {
		List<T> nodes = pNode.getChildList(isFolderView);
		T node;
		for (int i = 0,len=nodes.size(); i < len; i++) {
			node = nodes.get(i);
			boolean removed = treeTraveller.onNodeReached(node);
			if (treeTraveller.ended()) {
				return;
			}
			if (removed) {
				--len;
				--i;
			} else if (!node.isLeaf()) {
				TraverseChildTree(node, treeTraveller);
			}
		}
	}
	
	protected boolean filterDisplayNodes(List<TreeViewNode> nodes, int schFlag) {
		boolean ret=false;
		boolean schView=(schFlag&0x2)!=0;
		boolean shldAdd=(schFlag&0x1)!=0;
		int schViewExp = schView?schViewExpChannel:normalExpChannel;
		int currentIdx, flagNxt;
		TreeViewNode node;
		if(nodes!=null) { //sanity check
			for (int i = 0,len=nodes.size(); i < len; i++) {
				node = nodes.get(i);
				boolean added=filterNode(node);
				currentIdx = displayNodes.size();
				if(shldAdd) {
					displayNodes.add(node);
				}
				flagNxt=schFlag;
				if(schView) {
					//todo add bit
					node.expand(schViewExp);
				} else if (!node.isExpand(schViewExp)) {
					flagNxt &= ~0x1;
				}
				if (filterDisplayNodes(node.getChildList(), flagNxt) || added) {
					ret = true;
				} else if(shldAdd) {
					displayNodes.remove(currentIdx);
				}
			}
		}
		return ret;
	}
	
	protected boolean filterNode(TreeViewNode nodes) {
		return false;
	}
	
	public void filterTreeView() {
		filterTreeView(currentFilter, currentSchFlg);
	}
	
	public void filterTreeView(Object pattern, int schFlag) {
		if (rootNode!=null) {
			currentSchFlg = schFlag;
			displayNodes.clear();
			if (pattern!=null) {
				currentFilter = pattern;
				filterDisplayNodes(rootNode.getChildList(isFolderView), schFlag|0x1);
			} else {
				currentFilter = null;
				findDisplayNodes(rootNode.getChildList(isFolderView));
			}
			if (footNode!=null) {
				displayNodes.add(footNode);
			}
			notifyDataSetChanged();
		}
	}
	
    /**
     * 从nodes的结点中寻找展开了的非叶结点，添加到displayNodes中。
     *
     * @param nodes 基准点
     */
    private void findDisplayNodes(List<TreeViewNode> nodes) {
    	if(nodes!=null) { //sanity check
			for (int i = 0,len=nodes.size(); i < len; i++) {
				TreeViewNode node = nodes.get(i);
				if (findSelection && lastSelectionId == System.identityHashCode(node.getContent())) {
					lastSelectionPos = displayNodes.size();
					findSelection = false;
				}
				displayNodes.add(node);
				if (node.isExpand(currentExpChannel))
					findDisplayNodes(node.getChildList(isFolderView));
			}
		}
    }
	
    private void findSetDisplayNodes(List<TreeViewNode> nodes, boolean val) {
    	if(nodes!=null) { //sanity check
			for (int i = 0,len=nodes.size(); i < len; i++) {
				TreeViewNode node = nodes.get(i);
				node.setExpanded(currentExpChannel, val);
				if (findSelection && lastSelectionId == System.identityHashCode(node.getContent())) {
					lastSelectionPos = displayNodes.size();
					findSelection = false;
				}
				displayNodes.add(node);
				if (val)
					findSetDisplayNodes(node.getChildList(isFolderView), val);
			}
		}
    }

    @Override
    public int getItemViewType(int position) {
        return ((TreeViewAdapter.LayoutItemType)displayNodes.get(position).getContent())
				.getLayoutId();
    }
	
	@Override
	public long getItemId(int position) {
		return System.identityHashCode(displayNodes.get(position).getContent());
	}
	
    @NonNull
	@Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
		VH ret=null;
        for (TreeViewBinderInterface<VH> viewBinder : viewBinders) {
            if (viewBinder.getLayoutId() == viewType) {
				ret = viewBinder.provideViewHolder(v);
				break;
			}
        }
		assert ret != null;
		ret.itemView.setOnClickListener(this);
        return ret;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) { // 存疑
            Bundle b = (Bundle) payloads.get(0);
            for (String key : b.keySet()) {
				if (KEY_IS_EXPAND.equals(key)) {
					if (onTreeNodeListener != null)
						onTreeNodeListener.onToggle(b.getBoolean(key), holder);
				}
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
		TreeViewNode nodeI = displayNodes.get(position);
		holder.itemView.setPaddingRelative(nodeI.getHeight() * padding, 3, 3, 3);
        for (TreeViewBinderInterface viewBinder : viewBinders) {
            if (viewBinder.getLayoutId() == ((TreeViewAdapter.LayoutItemType)nodeI.getContent()).getLayoutId()) {
				viewBinder.bindView(holder, position, nodeI);
				break;
			}
        }
    }

    private int addChildNodes(TreeViewNode pNode, int startIndex) {
        List<TreeViewNode> childList = pNode.getChildList(isFolderView);
        int addChildCount = 0;
        for (TreeViewNode treeViewNode : childList) {
            displayNodes.add(startIndex + addChildCount++, treeViewNode);
            if (treeViewNode.isExpand(currentExpChannel)) {
                addChildCount += addChildNodes(treeViewNode, startIndex + addChildCount);
            }
        }
        if (!pNode.isExpand(currentExpChannel))
            pNode.toggle(currentExpChannel);
        return addChildCount;
    }

    //todo check
    private int removeChildNodes(TreeViewNode pNode) {
        if (pNode.isLeaf())
            return 0;
        List<TreeViewNode> childList = pNode.getChildList();
        int removeChildCount = childList.size();
        displayNodes.removeAll(childList);
        for (TreeViewNode child : childList) {
            if (child.isExpand(currentExpChannel)) {
                if (toCollapseChild)
                    child.toggle(currentExpChannel);
                removeChildCount += removeChildNodes(child);
            }
        }
        return removeChildCount;
    }
    
    
	
	private int removeChildNodes_fast(TreeViewNode pNode) {
    	return removeChildNodes_fast(displayNodes.indexOf(pNode));
	}
	
    private int removeChildNodes_fast(int position) {
    	int ret=0;
    	if (position>=0 && position<displayNodes.size()) {
			TreeViewNode pNode = displayNodes.get(position);
			pNode.collapse(currentExpChannel);
			for (int i = position+1, len=displayNodes.size(); i < len; i++) {
				TreeViewNode node = displayNodes.get(i);
				if(isChildOf(node, pNode)) {
					displayNodes.remove(i);
					--len;
					--i;
					++ret;
				} else {
					break;
				}
			}
		}
        return ret;
    }
	
	private boolean isChildOf(TreeViewNode node, TreeViewNode pNode) {
		while((node=node.getParent())!=null) {
			if (node==pNode) {
				return true;
			}
		}
		return false;
	}
	
	@Override
    public int getItemCount() {
        return displayNodes == null ? 0 : displayNodes.size();
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void ifCollapseChildWhileCollapseParent(boolean toCollapseChild) {
        this.toCollapseChild = toCollapseChild;
    }

    public void setTreeNodeListener(OnTreeNodeListener onTreeNodeListener) {
        this.onTreeNodeListener = onTreeNodeListener;
    }
	
	@Override
	public void onClick(View view) {
		VH holder = (VH) view.getTag();
		TreeViewNode nodeI = displayNodes.get(holder.getLayoutPosition());
		//Log.e("fatal", "getLayoutPosition "+holder.getLayoutPosition()+"  "+nodeI.toString());
		if (onTreeNodeListener != null && onTreeNodeListener.onClick(nodeI, holder)
				|| nodeI.isLeaf()/* || nodeI.isLocked()*/) {
			return;
		}
		int positionStart = holder.getLayoutPosition() + 1;
		nodeI.toggle(currentExpChannel);
		if (nodeI.isExpand(currentExpChannel)) {
			notifyItemRangeInserted(positionStart
					, addChildNodesFiltered(nodeI, positionStart, 0x1));
		} else {
			notifyItemRangeRemoved(positionStart, removeChildNodes(nodeI));
		}
	}
	
	public interface OnTreeNodeListener {
        /**
         * called when TreeNodes were clicked.
         * @return weather consume the click event.
         */
        boolean onClick(TreeViewNode node, RecyclerView.ViewHolder holder);

        /**
         * called when TreeNodes were toggle.
         * @param isExpand the status of TreeNodes after being toggled.
         */
        void onToggle(boolean isExpand, RecyclerView.ViewHolder holder);
    }

    public void refresh(List<TreeViewNode> treeViewNodes, int cap) {
        displayNodes.clear();
		displayNodes.ensureCapacity(cap);
        filterTreeView();
        notifyDataSetChanged();
    }
    
    public void refresh(RecyclerView view, List<TreeViewNode> treeViewNodes, int cap, boolean val) {
		LinearLayoutManager llm = null;
    	if (view!=null) {
			llm = (LinearLayoutManager) view.getLayoutManager();
			lastSelectionId = getItemId(llm.findFirstVisibleItemPosition());
			View ca = view.getChildAt(0);
			if(ca!=null) {
				lastSelectionOffset = ca.getTop();
			}
		}
		findSelection = true;
		displayNodes.clear();
		displayNodes.ensureCapacity(cap);
        findSetDisplayNodes(treeViewNodes, val);
        notifyDataSetChanged();
        if(llm!=null && !findSelection) {
			llm.scrollToPositionWithOffset(lastSelectionPos, lastSelectionOffset);
		}
    }

    public Iterator<TreeViewNode> getDisplayNodesIterator() {
        return displayNodes.iterator();
    }

    private void notifyDiff(final List<TreeViewNode> temp) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return temp.size();
            }

            @Override
            public int getNewListSize() {
                return displayNodes.size();
            }

            // judge if the same items
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return TreeViewAdapter.this.areItemsTheSame(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
            }

            // if they are the same items, whether the contents has bean changed.
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return TreeViewAdapter.this.areContentsTheSame(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
            }

            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                return TreeViewAdapter.this.getChangePayload(temp.get(oldItemPosition), displayNodes.get(newItemPosition));
            }
        });
        diffResult.dispatchUpdatesTo(this);
    }

    private Object getChangePayload(TreeViewNode oldNode, TreeViewNode newNode) {
        Bundle diffBundle = new Bundle();
        if (newNode.isExpand(currentExpChannel) != oldNode.isExpand(currentExpChannel)) {
            diffBundle.putBoolean(KEY_IS_EXPAND, newNode.isExpand(currentExpChannel));
        }
        if (diffBundle.size() == 0)
            return null;
        return diffBundle;
    }

    // For DiffUtil, if they are the same items, whether the contents has bean changed.
    private boolean areContentsTheSame(TreeViewNode oldNode, TreeViewNode newNode) {
        return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent())
                && oldNode.isExpand(currentExpChannel) == newNode.isExpand(currentExpChannel);
    }

    // judge if the same item for DiffUtil
    private boolean areItemsTheSame(TreeViewNode oldNode, TreeViewNode newNode) {
        return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent());
    }

    /**
     * collapse all root nodes.
     */
    public void collapseAll() {
//        // Back up the nodes are displaying.
//        List<TreeViewNode> temp = backupDisplayNodes();
//        //find all root nodes.
//        List<TreeViewNode> roots = new ArrayList<>();
//        for (TreeViewNode displayNode : displayNodes) {
//            if (displayNode.isRoot())
//                roots.add(displayNode);
//        }
//        //Close all root nodes.
//        for (TreeViewNode root : roots) {
//            if (root.isExpand(currentExpChannel)) {
//				root.collapse(currentExpChannel); removeChildNodes(root);
//			}
//
//        }
//        notifyDiff(temp);
		rootNode.collapseAll(currentExpChannel);
		refreshList(true);
    }
	
	public void expandAll() {
		rootNode.expandAll(currentExpChannel);
		refreshList(true);
	}
	
	public void collapseExpandToLevel(boolean collapse, int level) {
		if (collapse) {
			rootNode.collapseLevel(currentExpChannel, level);
		} else {
			rootNode.expandLevel(currentExpChannel, level);
		}
		refreshList(true);
	}
	
	protected void refreshList(boolean findCurrentPos) {
		refresh(rootNode.getChildList(isFolderView), 0);
	}
	
    @NonNull
    private List<TreeViewNode> backupDisplayNodes() {
        List<TreeViewNode> temp = new ArrayList<>();
        for (TreeViewNode displayNode : displayNodes) {
            try {
                temp.add(displayNode.clone());
            } catch (CloneNotSupportedException e) {
                temp.add(displayNode);
            }
        }
        return temp;
    }

    public void collapseNode(TreeViewNode pNode) {
//        List<TreeViewNode> temp = backupDisplayNodes();
//		pNode.collapse(currentExpChannel); removeChildNodes(pNode);
//        notifyDiff(temp);
    }
    
    public void collapseNode(int position) {
//        List<TreeViewNode> temp = backupDisplayNodes();
//		pNode.collapse(currentExpChannel); removeChildNodes(pNode);
//        notifyDiff(temp);
		int removed = removeChildNodes_fast(position);
		if (removed>0) {
			notifyItemRangeRemoved(position+1, removed);
		}
    }

    public void collapseBrotherNode(TreeViewNode pNode) {
        List<TreeViewNode> temp = backupDisplayNodes();
        if (pNode.isRoot()) {
            List<TreeViewNode> roots = new ArrayList<>();
            for (TreeViewNode displayNode : displayNodes) {
                if (displayNode.isRoot())
                    roots.add(displayNode);
            }
            //Close all root nodes.
            for (TreeViewNode root : roots) {
                if (root.isExpand(currentExpChannel) && !root.equals(pNode)) {
					root.collapse(currentExpChannel); removeChildNodes(root);
				}
            }
        } else {
            TreeViewNode parent = pNode.getParent();
            if (parent == null)
                return;
            List<TreeViewNode> childList = parent.getChildList();
            for (TreeViewNode node : childList) {
                if (node.equals(pNode) || !node.isExpand(currentExpChannel))
                    continue;
				node.collapse(currentExpChannel);  removeChildNodes(node);
            }
        }
        notifyDiff(temp);
    }
	
//	@Override
//	public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
//		super.unregisterAdapterDataObserver(observer);
//		Log.e("fatal adapter", "unregisterAdapterDataObserver");
//	}
//
//	@Override
//	public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
//		super.onDetachedFromRecyclerView(recyclerView);
//		Log.e("fatal adapter", "onDetachedFromRecyclerView");
//	}
	
	public interface LayoutItemType {
		int getLayoutId();
	}
	
	public static abstract class TreeViewBinderInterface<VH> implements LayoutItemType {
		public abstract VH provideViewHolder(View itemView);
		
		public abstract void bindView(VH holder, int position, TreeViewNode node);
	}
}
