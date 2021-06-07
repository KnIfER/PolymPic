package com.knziha.polymer.widgets;


import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knziha.polymer.Utils.CMN;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.util.ArrayList;
import java.util.Collections;

class NavigationNode extends TreeViewNode<JSONObject>
{
	public static int cc;
	JSONArray children;
	public NavigationNode(@NonNull JSONObject itemData) {
		super(itemData);
		cc++;
		JSONArray children = content.getJSONArray("children");
		if (children!=null) {
			this.children = children;
			ensureTree();
			JSONObject child;
			int i = 0, len=children.size();
			childList.ensureCapacity(len+16);
			for (; i < len; i++) {
				child = children.getJSONObject(i);
				if (child!=null) {
					NavigationNode node = new NavigationNode(child);
					childList.add(node);
					node.parent=this;
					if (!node.isLeaf()) {
						foldList.add(node);
					}
				}
			}
		}
	}
	
	public static void removeChild(NavigationNode hotNode) {
		NavigationNode np = (NavigationNode) hotNode.getParent();
		if (np!=null) {
			np.removeChild_internal(hotNode);
		}
	}
	
	private void removeChild_internal(NavigationNode hotNode) {
		int index = childList.indexOf(hotNode);
		int index1 = fastIndexOf(hotNode.content, index);
		if(index>=0) {
			childList.remove(index);
			if(foldListValid && !hotNode.isLeaf()) {
				foldListValid = false;
			}
		}
		if(index1>=0) {
			children.remove(index1);
		}
		if(index<0 || index1<0) {
			CMN.Log("failed to remove", hotNode, this);
		}
		hotNode.parent = null;
	}
	
	public int fastIndexOf(NavigationNode node, int index) {
		if(index>=0 && index<childList.size() && childList.get(index)==node) {
			return index;
		}
		return childList.indexOf(node);
	}
	
	private int fastIndexOf(JSONObject content, int index) {
		if(index>=0 && index<children.size() && children.get(index)==content) {
			return index;
		}
		return children.indexOf(content);
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
		if(foldListValid && !node.isLeaf()) {
			foldListValid = false;
		}
		return this;
	}
	
	public void insertNode(int idx, NavigationNode node) {
		ensureTree();
		idx = Math.max(0, Math.min(idx, Math.min(childList.size(), children.size())));
		childList.add(idx, node);
		children.add(idx, node.content);
		node.parent = this;
		node.height=UNDEFINE;
		if(foldListValid && !node.isLeaf()) {
			foldListValid = false;
		}
	}
	
	@Override
	public boolean isLeaf() {
		return children==null;
	}
	
	public void reorderNodeBefore(NavigationNode toNode, NavigationNode node) {
		if(toNode==null) {
			removeChild(node);
			insertNodeBefore(null, node);
		} else {
			int fromPosition = childList.indexOf(node);
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
			fromPosition = fastIndexOf(node.content, fromPosition);
			toPosition = fastIndexOf(toNode.content, toPosition);
			try {
				if (fromPosition < toPosition) {
					for (int i = fromPosition; i < toPosition-1; i++) {
						Collections.swap(children, i, i + 1);
					}
				} else {
					for (int i = fromPosition; i > toPosition; i--) {
						Collections.swap(children, i, i - 1);
					}
				}
			} catch (Exception e) {
				CMN.Log(node, childList.indexOf(node));
				CMN.Log(toNode, childList.indexOf(toNode));
				CMN.Log(fromPosition, "childList1.size="+node.childList.size(), "content1.size="+node.content.size());
				CMN.Log(fromPosition, "childList1.size="+toNode.childList.size(), "content1.size="+toNode.content.size());
				throw e;
			}
		}
		if(foldListValid && !node.isLeaf()) {
			foldListValid = false;
		}
	}
	
	public String getName() {
		return content.getString("name");
	}
	
	public String getUrl() {
		return content.getString("url");
	}
	
	public void setUrlAndName(String new_url, String new_name) {
		content.put("url", new_url);
		content.put("name", new_name);
	}
	
	public NavigationNode addNewChild(String url, String name, int index) {
		ensureTree();
		int cc = Math.min(children.size(), childList.size()); // sanity check
		if(index<0) {
			index = cc+index;
			if(index<0) {
				index = 0;
			}
		} else if(index>cc) {
			index = cc;
		}
		JSONObject child =  new JSONObject();
		child.put("url", url);
		child.put("name", name);
		if(url==null) {
			child.put("children", new JSONArray());
		}
		NavigationNode childNode = new NavigationNode(child);
		if(url==null) {
			foldListValid = false;
			childNode.ensureTree();
		}
		childList.add(index, childNode);
		children.add(index, child);
		childNode.parent = this;
		return childNode;
	}
	
	private void ensureTree() {
		if (children==null) {
			children = new JSONArray();
		}
		if (childList==null) {
			childList = new ArrayList<>();
		}
		if (foldList ==null) {
			foldList = new ArrayList<>();
		}
	}
	
	@Override
	public String toString() {
		return "NavNode{" +
				"name=" + content.getString("name") +
				(isLeaf()?"":(", childSz1=" + (childList==null?0:childList.size()) +
						", childSz2=" + (children==null?0:children.size())))+
				'}';
	}
	
	public boolean checkHarmony() {
		return childList.size()==children.size();
	}
}