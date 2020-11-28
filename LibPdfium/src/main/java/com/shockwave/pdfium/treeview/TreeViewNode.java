package com.shockwave.pdfium.treeview;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tlh on 2016/10/1 :)
 */

@SuppressWarnings({"rawtypes"})
public class TreeViewNode<T extends TreeViewAdapter.LayoutItemType> implements Cloneable {
    private T content;
    private TreeViewNode parent;
    private List<TreeViewNode> childList;
    private boolean isExpand;
    //private boolean isLocked;
    //the tree height
    private int height = UNDEFINE;

    private static final int UNDEFINE = -1;

    public TreeViewNode(@NonNull T content) {
        this.content = content;
        this.childList = new ArrayList<>();
    }

    public int getHeight() {
        if (isRoot())
            height = 0;
        else if (height == UNDEFINE)
            height = parent.getHeight() + 1;
        return height;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return childList == null || childList.isEmpty();
    }

    public void setContent(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public List<TreeViewNode> getChildList() {
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

    public void toggle() {
        isExpand = !isExpand;
	}

    public void collapse() {
        if (isExpand) {
            isExpand = false;
        }
    }

    public void collapseAll() {
        if (childList == null || childList.isEmpty()) {
            return;
        }
        for (TreeViewNode child : this.childList) {
            child.collapseAll();
        }
    }

    public void expand() {
        if (!isExpand) {
            isExpand = true;
        }
    }

    public void expandAll() {
        expand();
        if (childList == null || childList.isEmpty()) {
            return;
        }
        for (TreeViewNode child : this.childList) {
            child.expandAll();
        }
    }

    public boolean isExpand() {
        return isExpand;
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
	
	public void setExpanded(boolean val) {
		isExpand = val;
	}
}
