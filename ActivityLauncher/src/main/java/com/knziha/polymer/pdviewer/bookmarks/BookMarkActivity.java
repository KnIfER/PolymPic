package com.knziha.polymer.pdviewer.bookmarks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookMarkActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TreeViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.test_bookmark_view);
        
		rv = findViewById(R.id.rv);
	
		List<TreeViewNode> nodes = new ArrayList<>();
		TreeViewNode<BookMarkDir> app = new TreeViewNode<>(new BookMarkDir("app"));
		nodes.add(app);
		app.addChild(
				new TreeViewNode<>(new BookMarkDir("manifests"))
						.addChild(new TreeViewNode<>(new BookMarkFile("AndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifest.xml")))
		);
		
		app.addChild(
				new TreeViewNode<>(new BookMarkDir("java")).addChild(
						new TreeViewNode<>(new BookMarkDir("tellh")).addChild(
								new TreeViewNode<>(new BookMarkDir("com")).addChild(
										new TreeViewNode<>(new BookMarkDir("recyclertreeview"))
												.addChild(new TreeViewNode<>(new BookMarkFile("Dir")))
												.addChild(new TreeViewNode<>(new BookMarkFile("DirectoryNodeBinder")))
												.addChild(new TreeViewNode<>(new BookMarkFile("File")))
												.addChild(new TreeViewNode<>(new BookMarkFile("FileNodeBinder")))
												.addChild(new TreeViewNode<>(new BookMarkFile("TreeViewBinder")))
								)
						)
				)
		);
		TreeViewNode<BookMarkDir> res = new TreeViewNode<>(new BookMarkDir("res"));
		nodes.add(res);
		res.addChild(
				new TreeViewNode<>(new BookMarkDir("layout"))
						.addChild(new TreeViewNode<>(new BookMarkFile("activity_main.xml")))
						.addChild(new TreeViewNode<>(new BookMarkFile("item_dir.xml")))
						.addChild(new TreeViewNode<>(new BookMarkFile("item_file.xml")))
		);
		res.addChild(
				new TreeViewNode<>(new BookMarkDir("mipmap"))
						.addChild(new TreeViewNode<>(new BookMarkFile("ic_launcher.png")))
		);
	
		rv.setLayoutManager(new LinearLayoutManager(this));
	
		rv.setItemAnimator(null);
		
		adapter = new TreeViewAdapter(nodes, Arrays.asList(new BookMarkFile.FileNodeBinder(), new BookMarkDir.DirectoryNodeBinder()));
		// whether collapse child nodes when their parent node was close.
//        adapter.ifCollapseChildWhileCollapseParent(true);
	
		rv.addItemDecoration(new RecyclerView.ItemDecoration(){
			ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
			int mDividerHeight = 1;
			@Override
			public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
				if (mDivider != null) {
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
			}
			@Override
			public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
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
				if(holder instanceof BookMarkDir.DirectoryNodeBinder.ViewHolder) {
					
					//DirectoryNodeBinder.ViewHolder dirViewHolder = (DirectoryNodeBinder.ViewHolder) holder;
					//final ImageView ivArrow = dirViewHolder.getIvArrow();
					//int rotateDegree = isExpand ? 90 : -90;
					//ivArrow.animate().rotationBy(rotateDegree).start();
					
				}
			}
		});
		rv.setAdapter(adapter);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.id_action_close_all:
//                adapter.collapseAll();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}
