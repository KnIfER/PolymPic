package com.knziha.polymer.pdviewer.bookmarks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;
import com.knziha.polymer.treeview.TreeViewAdapter;
import com.knziha.polymer.treeview.TreeViewNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookMarkFragment extends Fragment {
	private RecyclerView bmRv;
	private TreeViewAdapter adapter;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(bmRv==null) {
			Context context = inflater.getContext();
			bmRv = new RecyclerView(context);
			bmRv.setId(R.id.rv);
			
			List<TreeViewNode> nodes = new ArrayList<>();
			TreeViewNode<BookMarkEntry> app = new TreeViewNode<>(new BookMarkEntry("app"));
			nodes.add(app);
			app.addChild(
					new TreeViewNode<>(new BookMarkEntry("manifests"))
							.addChild(new TreeViewNode<>(new BookMarkEntry("AndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifest.xml")))
			);
			
			app.addChild(
					new TreeViewNode<>(new BookMarkEntry("java")).addChild(
							new TreeViewNode<>(new BookMarkEntry("tellh")).addChild(
									new TreeViewNode<>(new BookMarkEntry("com")).addChild(
											new TreeViewNode<>(new BookMarkEntry("recyclertreeview"))
													.addChild(new TreeViewNode<>(new BookMarkEntry("Dir")))
													.addChild(new TreeViewNode<>(new BookMarkEntry("DirectoryNodeBinder")))
													.addChild(new TreeViewNode<>(new BookMarkEntry("File")))
													.addChild(new TreeViewNode<>(new BookMarkEntry("FileNodeBinder")))
													.addChild(new TreeViewNode<>(new BookMarkEntry("TreeViewBinder")))
									)
							)
					)
			);
			TreeViewNode<BookMarkEntry> res = new TreeViewNode<>(new BookMarkEntry("res"));
			nodes.add(res);
			res.addChild(
					new TreeViewNode<>(new BookMarkEntry("layout"))
							.addChild(new TreeViewNode<>(new BookMarkEntry("activity_main.xml")))
							.addChild(new TreeViewNode<>(new BookMarkEntry("item_dir.xml")))
							.addChild(new TreeViewNode<>(new BookMarkEntry("item_file.xml")))
			);
			res.addChild(
					new TreeViewNode<>(new BookMarkEntry("mipmap"))
							.addChild(new TreeViewNode<>(new BookMarkEntry("ic_launcher.png")))
			);
			
			bmRv.setLayoutManager(new LinearLayoutManager(context));
			
			bmRv.setItemAnimator(null);
			
			adapter = new TreeViewAdapter(nodes, Collections.singletonList(new BookMarkEntry.BookMarkEntryBinder()));
			
			bmRv.addItemDecoration(new RecyclerView.ItemDecoration(){
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
							mDivider.setBounds(0, top, width, top + mDividerHeight);
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
					return parent.getChildViewHolder(view).getBindingAdapterPosition()<adapter.getItemCount()-1;
				}
			});
			
			adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
				@Override
				public boolean onClick(TreeViewNode node, RecyclerView.ViewHolder holder) {
					if (!node.isLeaf()) {
						//Update and toggle the node.
						onToggle(!node.isExpand(), holder);
//                    if (!node.isExpand())
//                        adapter.collapseBrotherNode(node);
					}
					return false;
				}
				
				@Override
				public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
					if(holder instanceof BookMarkEntry.BookMarkEntryBinder.ViewHolder) {
						BookMarkEntry.BookMarkEntryBinder.ViewHolder viewholder = (BookMarkEntry.BookMarkEntryBinder.ViewHolder) holder;
						final ImageView ivArrow = viewholder.getIvArrow();
						ivArrow.animate().rotation(isExpand ? 90 : 0).start();
					}
				}
			});
			bmRv.setAdapter(adapter);
		}
		return bmRv;
	}
	
	
	
	
}
