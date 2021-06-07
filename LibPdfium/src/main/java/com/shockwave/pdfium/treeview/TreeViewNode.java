package com.shockwave.pdfium.treeview;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tlh on 2016/10/1 :)
 */

@SuppressWarnings({"rawtypes"})
public class TreeViewNode<T> implements Cloneable {
	protected final T content;
    protected TreeViewNode parent;
    protected ArrayList<TreeViewNode> childList;
	protected boolean foldListValid;
	protected List<TreeViewNode> foldList;
    protected int isExpand;
    //private boolean isLocked;
    //the tree height
	protected int height = UNDEFINE;
	
	protected static final int UNDEFINE = -1;

    public TreeViewNode(@NonNull T content) {
        this.content = content;
        this.childList = new ArrayList<>();
    }

    public int getHeight() {
        if (isRoot())
            height = 0;
        //else if (height == UNDEFINE)
		else if(parent!=null)
            height = parent.getHeight() + 1;
        return height;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return childList == null || childList.isEmpty();
    }

//    public void setContent(T content) {
//        this.content = content;
//    }

    public T getContent() {
        return content;
    }

    public List<TreeViewNode> getChildList() {
        return childList;
    }
	
	public List<TreeViewNode> getChildList(boolean isFolderView) {
    	if (isFolderView) {
    		if (!foldListValid) {
				foldList.clear();
				for (int i = 0, len=childList.size(); i < len; i++) {
					if (!childList.get(i).isLeaf()) {
						foldList.add(childList.get(i));
					}
				}
				foldListValid = true;
			}
			return foldList;
		}
    	return childList;
	}

    public void setChildList(List<TreeViewNode> childList) {
        this.childList.clear();
        for (TreeViewNode treeViewNode : childList) {
            addChild(treeViewNode);
        }
    }

    public TreeViewNode addChild(TreeViewNode node) {
        if (childList == null)
            childList = new ArrayList<>();
        childList.add(node);
        node.parent = this;
        return this;
    }

    public void toggle(int channelMask) {
		boolean val = (isExpand & channelMask)==0;
		isExpand &= ~channelMask;
		if (val) {
			isExpand |= channelMask;
		}
	}

    public void collapse(int channelMask) {
		isExpand &= ~channelMask;
    }

    public void collapseAll(int channelMask) {
		collapse(channelMask);
        if (childList == null || childList.isEmpty()) {
            return;
        }
        for (TreeViewNode child : this.childList) {
            child.collapseAll(channelMask);
        }
    }

    public void collapseLevel(int channelMask, int level) {
        if (childList == null) {
            return;
        }
		int lv = getHeight();
		if (lv==level) {
			collapse(channelMask);
		}
		if (lv<level) {
			for (TreeViewNode child : this.childList) {
				child.collapseLevel(channelMask, level);
			}
		}
    }

    public void expand(int channelMask) {
		isExpand |= channelMask;
    }

    public void expandAll(int channelMask) {
        expand(channelMask);
        if (childList == null || childList.isEmpty()) {
            return;
        }
        for (TreeViewNode child : this.childList) {
            child.expandAll(channelMask);
        }
    }
    
	public void expandLevel(int channelMask, int level) {
		if (childList == null) {
			return;
		}
		int lv = getHeight();
		if (lv==level) {
			expand(channelMask);
		}
		if (lv<level) {
			for (TreeViewNode child : this.childList) {
				child.expandLevel(channelMask, level);
			}
		}
	}
	
	public boolean isExpand() {
		return isExpand(0x1);
	}
	
    public boolean isExpand(int channelMask) {
        return (isExpand & channelMask)!=0;
    }

    public void setParent(TreeViewNode parent) {
        this.parent = parent;
    }

    public TreeViewNode getParent() {
        return parent;
    }

    @NonNull
	@Override
    public String toString() {
        return "TreeNode{" +
                "content=" + this.content +
                ", parent=" + (parent == null ? "null" : parent.getContent().toString()) +
                ", childList=" + (childList == null ? "null" : childList.toString()) +
                ", isExpand=" + isExpand +
                '}';
    }

    @NonNull
	@Override
    protected TreeViewNode<T> clone() throws CloneNotSupportedException {
        TreeViewNode<T> clone = new TreeViewNode<>(this.content);
        clone.isExpand = this.isExpand;
        return clone;
    }
	
	public void setExpanded(int channelMask, boolean val) {
		isExpand &= ~channelMask;
		if (val) {
			isExpand |= channelMask;
		}
	}
	
	public int getChildCount() {
		return childList.size();
	}
}
