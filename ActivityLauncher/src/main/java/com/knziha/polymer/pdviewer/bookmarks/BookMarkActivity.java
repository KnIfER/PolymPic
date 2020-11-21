package com.knziha.polymer.pdviewer.bookmarks;

import android.os.Bundle;

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
	
		List<TreeNode> nodes = new ArrayList<>();
		TreeNode<BookMarkDir> app = new TreeNode<>(new BookMarkDir("app"));
		nodes.add(app);
		app.addChild(
				new TreeNode<>(new BookMarkDir("manifests"))
						.addChild(new TreeNode<>(new BookMarkFile("AndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifestAndroidManifest.xml")))
		);
		app.addChild(
				new TreeNode<>(new BookMarkDir("java")).addChild(
						new TreeNode<>(new BookMarkDir("tellh")).addChild(
								new TreeNode<>(new BookMarkDir("com")).addChild(
										new TreeNode<>(new BookMarkDir("recyclertreeview"))
												.addChild(new TreeNode<>(new BookMarkFile("Dir")))
												.addChild(new TreeNode<>(new BookMarkFile("DirectoryNodeBinder")))
												.addChild(new TreeNode<>(new BookMarkFile("File")))
												.addChild(new TreeNode<>(new BookMarkFile("FileNodeBinder")))
												.addChild(new TreeNode<>(new BookMarkFile("TreeViewBinder")))
								)
						)
				)
		);
		TreeNode<BookMarkDir> res = new TreeNode<>(new BookMarkDir("res"));
		nodes.add(res);
		res.addChild(
				new TreeNode<>(new BookMarkDir("layout")).lock() // lock this TreeNode
						.addChild(new TreeNode<>(new BookMarkFile("activity_main.xml")))
						.addChild(new TreeNode<>(new BookMarkFile("item_dir.xml")))
						.addChild(new TreeNode<>(new BookMarkFile("item_file.xml")))
		);
		res.addChild(
				new TreeNode<>(new BookMarkDir("mipmap"))
						.addChild(new TreeNode<>(new BookMarkFile("ic_launcher.png")))
		);
	
		rv.setLayoutManager(new LinearLayoutManager(this));
	
		rv.setItemAnimator(null);
		
		adapter = new TreeViewAdapter(nodes, Arrays.asList(new BookMarkFile.FileNodeBinder(), new BookMarkDir.DirectoryNodeBinder()));
		// whether collapse child nodes when their parent node was close.
//        adapter.ifCollapseChildWhileCollapseParent(true);
		adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
			@Override
			public boolean onClick(TreeNode node, RecyclerView.ViewHolder holder) {
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
