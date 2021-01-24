package com.knziha.polymer.pdviewer.bookmarks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;
import com.shockwave.pdfium.bookmarks.BookMarkEntry;
import com.shockwave.pdfium.bookmarks.BookMarkNode;
import com.shockwave.pdfium.treeview.TreeViewAdapter;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class BookMarkActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TreeViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.bookmark_test_view);
        
		rv = findViewById(R.id.rv);
	
		List<TreeViewNode> nodes = new ArrayList<>();
		BookMarkNode app = new BookMarkNode(new BookMarkEntry("app", 0));
		nodes.add(app);
		app.addChild(
				new TreeViewNode<>(new BookMarkEntry("manifests", 0))
						.addChild(new TreeViewNode<>(new BookMarkEntry("AndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifest.xml", 0)))
		);
		
		app.addChild(
				new TreeViewNode<>(new BookMarkEntry("java", 0)).addChild(
						new TreeViewNode<>(new BookMarkEntry("tellh", 0)).addChild(
								new TreeViewNode<>(new BookMarkEntry("com", 0)).addChild(
										new TreeViewNode<>(new BookMarkEntry("recyclertreeview", 0))
												.addChild(new TreeViewNode<>(new BookMarkEntry("Dir", 0)))
												.addChild(new TreeViewNode<>(new BookMarkEntry("DirectoryNodeBinder", 0)))
												.addChild(new TreeViewNode<>(new BookMarkEntry("File", 0)))
												.addChild(new TreeViewNode<>(new BookMarkEntry("FileNodeBinder", 0)))
												.addChild(new TreeViewNode<>(new BookMarkEntry("TreeViewBinder", 0)))
								)
						)
				)
		);
		TreeViewNode<BookMarkEntry> res = new TreeViewNode<>(new BookMarkEntry("res", 0));
		nodes.add(res);
		res.addChild(
				new TreeViewNode<>(new BookMarkEntry("layout", 0))
						.addChild(new TreeViewNode<>(new BookMarkEntry("activity_main.xml", 0)))
						.addChild(new TreeViewNode<>(new BookMarkEntry("item_dir.xml", 0)))
						.addChild(new TreeViewNode<>(new BookMarkEntry("item_file.xml", 0)))
		);
		res.addChild(
				new TreeViewNode<>(new BookMarkEntry("mipmap", 0))
						.addChild(new TreeViewNode<>(new BookMarkEntry("ic_launcher.png", 0)))
		);
	
		rv.setLayoutManager(new LinearLayoutManager(this));
	
		rv.setItemAnimator(null);
		
		adapter = new TreeViewAdapter(nodes, Collections.singletonList(new BookMarkFragment.BookMarkEntryBinder()));
		// whether collapse child nodes when their parent node was close.
//        adapter.ifCollapseChildWhileCollapseParent(true);
	
		rv.addItemDecoration(new RecyclerView.ItemDecoration(){
			final ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
			final int mDividerHeight = 1;
			@Override
			public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
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
					onToggle(!node.isExpand(), holder);
                    if (!node.isExpand())
                        adapter.collapseBrotherNode(node);
				}
				return false;
			}
		
			@Override
			public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
				if(holder instanceof BookMarkFragment.BookMarkEntryBinder.ViewHolder) {
					BookMarkFragment.BookMarkEntryBinder.ViewHolder viewholder = (BookMarkFragment.BookMarkEntryBinder.ViewHolder) holder;
					viewholder.ivArrow.animate().rotation(isExpand ? 90 : 0).start();
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
