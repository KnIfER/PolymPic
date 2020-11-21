package com.knziha.polymer.pdviewer.bookmarks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tlh on 2016/10/1 :)
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class TreeViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final String KEY_IS_EXPAND = "IS_EXPAND";
    private final List<? extends TreeViewBinderInterface> viewBinders;
    private List<TreeViewNode> displayNodes;
    private int padding = 30;
    private OnTreeNodeListener onTreeNodeListener;
    private boolean toCollapseChild;

    public TreeViewAdapter(List<? extends TreeViewBinderInterface> viewBinders) {
        this(null, viewBinders);
    }

    public TreeViewAdapter(List<TreeViewNode> nodes, List<? extends TreeViewBinderInterface> viewBinders) {
        displayNodes = new ArrayList<>();
		findDisplayNodes(nodes);
        this.viewBinders = viewBinders;
    }

    /**
     * 从nodes的结点中寻找展开了的非叶结点，添加到displayNodes中。
     *
     * @param nodes 基准点
     */
    private void findDisplayNodes(List<TreeViewNode> nodes) {
    	if(nodes!=null) { //sanity check
			for (TreeViewNode node : nodes) {
				displayNodes.add(node);
				if (node.isExpand())
					findDisplayNodes(node.getChildList());
			}
		}
    }

    @Override
    public int getItemViewType(int position) {
        return displayNodes.get(position).getContent().getLayoutId();
    }

    @NonNull
	@Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
		RecyclerView.ViewHolder ret=null;
        for (TreeViewBinderInterface viewBinder : viewBinders) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		TreeViewNode nodeI = displayNodes.get(position);
		holder.itemView.setPaddingRelative(nodeI.getHeight() * padding, 3, 3, 3);
        for (TreeViewBinderInterface viewBinder : viewBinders) {
            if (viewBinder.getLayoutId() == nodeI.getContent().getLayoutId()) {
				viewBinder.bindView(holder, position, nodeI);
				break;
			}
        }
    }

    private int addChildNodes(TreeViewNode pNode, int startIndex) {
        List<TreeViewNode> childList = pNode.getChildList();
        int addChildCount = 0;
        for (TreeViewNode treeViewNode : childList) {
            displayNodes.add(startIndex + addChildCount++, treeViewNode);
            if (treeViewNode.isExpand()) {
                addChildCount += addChildNodes(treeViewNode, startIndex + addChildCount);
            }
        }
        if (!pNode.isExpand())
            pNode.toggle();
        return addChildCount;
    }

    private int removeChildNodes(TreeViewNode pNode) {
        return removeChildNodes(pNode, true);
    }

    private int removeChildNodes(TreeViewNode pNode, boolean shouldToggle) {
        if (pNode.isLeaf())
            return 0;
        List<TreeViewNode> childList = pNode.getChildList();
        int removeChildCount = childList.size();
        displayNodes.removeAll(childList);
        for (TreeViewNode child : childList) {
            if (child.isExpand()) {
                if (toCollapseChild)
                    child.toggle();
                removeChildCount += removeChildNodes(child, false);
            }
        }
        if (shouldToggle)
            pNode.toggle();
        return removeChildCount;
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

    public void setOnTreeNodeListener(OnTreeNodeListener onTreeNodeListener) {
        this.onTreeNodeListener = onTreeNodeListener;
    }
	
	@Override
	public void onClick(View view) {
		RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) view.getTag();
		TreeViewNode nodeI = displayNodes.get(holder.getLayoutPosition());
		if (onTreeNodeListener != null && onTreeNodeListener.onClick(nodeI, holder)
				|| nodeI.isLeaf()/* || nodeI.isLocked()*/) {
			return;
		}
		int positionStart = holder.getBindingAdapterPosition() + 1;
		if (!nodeI.isExpand()) {
			notifyItemRangeInserted(positionStart, addChildNodes(nodeI, positionStart));
		} else {
			notifyItemRangeRemoved(positionStart, removeChildNodes(nodeI, true));
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

    public void refresh(List<TreeViewNode> treeViewNodes) {
        displayNodes.clear();
        findDisplayNodes(treeViewNodes);
        notifyDataSetChanged();
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
        if (newNode.isExpand() != oldNode.isExpand()) {
            diffBundle.putBoolean(KEY_IS_EXPAND, newNode.isExpand());
        }
        if (diffBundle.size() == 0)
            return null;
        return diffBundle;
    }

    // For DiffUtil, if they are the same items, whether the contents has bean changed.
    private boolean areContentsTheSame(TreeViewNode oldNode, TreeViewNode newNode) {
        return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent())
                && oldNode.isExpand() == newNode.isExpand();
    }

    // judge if the same item for DiffUtil
    private boolean areItemsTheSame(TreeViewNode oldNode, TreeViewNode newNode) {
        return oldNode.getContent() != null && oldNode.getContent().equals(newNode.getContent());
    }

    /**
     * collapse all root nodes.
     */
    public void collapseAll() {
        // Back up the nodes are displaying.
        List<TreeViewNode> temp = backupDisplayNodes();
        //find all root nodes.
        List<TreeViewNode> roots = new ArrayList<>();
        for (TreeViewNode displayNode : displayNodes) {
            if (displayNode.isRoot())
                roots.add(displayNode);
        }
        //Close all root nodes.
        for (TreeViewNode root : roots) {
            if (root.isExpand())
                removeChildNodes(root);
        }
        notifyDiff(temp);
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
        List<TreeViewNode> temp = backupDisplayNodes();
        removeChildNodes(pNode);
        notifyDiff(temp);
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
                if (root.isExpand() && !root.equals(pNode))
                    removeChildNodes(root);
            }
        } else {
            TreeViewNode parent = pNode.getParent();
            if (parent == null)
                return;
            List<TreeViewNode> childList = parent.getChildList();
            for (TreeViewNode node : childList) {
                if (node.equals(pNode) || !node.isExpand())
                    continue;
                removeChildNodes(node);
            }
        }
        notifyDiff(temp);
    }
	
	
	public interface LayoutItemType {
		int getLayoutId();
	}
	
	public static abstract class TreeViewBinderInterface<VH extends RecyclerView.ViewHolder> implements LayoutItemType {
		public abstract VH provideViewHolder(View itemView);
		
		public abstract void bindView(VH holder, int position, TreeViewNode node);
		
		public static class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View rootView) {
				super(rootView);
			}
		}
	}
}
