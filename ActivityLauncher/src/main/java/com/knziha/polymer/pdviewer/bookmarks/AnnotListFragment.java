package com.knziha.polymer.pdviewer.bookmarks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.pdviewer.PDFPageParms;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.pdviewer.bookdata.PDocBookInfo;
import com.knziha.polymer.widgets.Utils;

public class AnnotListFragment extends Fragment {
	RecyclerView annotRv;
	final AnnotListAdapter adapter = new AnnotListAdapter();
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		CMN.Log("onCreateView1");
		if(annotRv ==null) {
			Context context = inflater.getContext();
			RecyclerView recyclerView = new RecyclerView(context);
			recyclerView.setId(R.id.rv);
			recyclerView.setLayoutManager(new LinearLayoutManager(context));
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
			
			//adapter.setHasStableIds(true);
			recyclerView.setAdapter(adapter);
			annotRv = recyclerView;
		} else {
			Utils.removeIfParentBeOrNotBe(annotRv, null, false);
		}
		return annotRv;
	}
	
	public void setDocument(PDocument doc) {
		PDocBookInfo info = doc.bookInfo;
		String tableName = info.connectPagesTableIfExists(LexicalDBHelper.getInstance());
		adapter.resetCursor(tableName);
	}
	
	public class AnnotListAdapter extends RecyclerView.Adapter<AnnotListAdapter.ViewHolder> implements View.OnClickListener {
		Cursor cursor = Utils.EmptyCursor;
		
		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ViewHolder ret = new ViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.bookmark_annots_item, parent, false));
			ret.itemView.setOnClickListener(this);
			return ret;
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			cursor.moveToPosition(position);
			holder.position = position;
			holder.tvName.setText(cursor.getString(4));
			holder.page = cursor.getInt(1);
			holder.tvPage.setText(Integer.toString(holder.page+1));
		}
		
		@Override
		public int getItemCount() {
			return cursor.getCount();
		}
		
		public void resetCursor(String tableName) {
			if(tableName!=null) {
				if(cursor !=null) {
					cursor.close();
				}
				final String sql = "select * from "+tableName;
				try {
					cursor = LexicalDBHelper.getInstancedDb().rawQuery(sql, null);
				} catch (Exception e) {
					CMN.Log(e);
					cursor = Utils.EmptyCursor;
				}
				notifyDataSetChanged();
			}
		}
		
		@Override
		public void onClick(View v) {
			PDocViewerActivity a = (PDocViewerActivity) getActivity();
			if(a!=null) {
				ViewHolder vh = (ViewHolder) v.getTag();
				//a.currentViewer.goToPageCentered(vh);
				cursor.moveToPosition(vh.position);
				String parm = cursor.getString(5);
				if(parm!=null) {
					a.currentViewer.navigateTo(new PDFPageParms(parm), true);
				} else {
					a.currentViewer.goToPageCentered(vh.page, false);
				}
				((DialogFragment)getParentFragment()).dismiss();
			}
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public final TextView tvName;
			public final TextView tvPage;
			
			public int page;
			public int position;
			
			public ViewHolder(View rootView) {
				super(rootView);
				this.tvName = rootView.findViewById(R.id.tv_name);
				this.tvPage = rootView.findViewById(R.id.tv_page);
				itemView.setTag(this);
			}
		}
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CMN.Log("oac1", CMN.id(this));
	}
}
