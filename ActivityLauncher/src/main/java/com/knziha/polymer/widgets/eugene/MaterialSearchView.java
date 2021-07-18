package com.knziha.polymer.widgets.eugene;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.SearchViewResultItemBinding;
import com.knziha.polymer.databinding.ViewSearchBinding;
import com.knziha.polymer.webslideshow.TouchSortHandler;
import com.knziha.polymer.webslideshow.ViewUtils;
import com.knziha.polymer.widgets.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.knziha.polymer.widgets.Utils.dpToPixel;

@SuppressWarnings("rawtypes")
public class MaterialSearchView extends CardView {
    private static final int ANIMATION_DURATION = 250;

    private boolean animateSearchView;
    private int searchButtonPosition;
    private boolean visible = false;
    
	private	final InputMethodManager imm;
    public final ViewSearchBinding UISearchBar; // PUBLIC and FINAL , yeah, there's nothing to hide from anyone.
	private SearchRecyclerAdapter adapter;
	private Animator animatorShow;
	public final EditText etSearch;
	public Dialog d;
	public PopupWindow p;
	
    ArrayList<String> searchRecords = new ArrayList<>();
    ArrayList searchResults;
    List displayData;
	private LayoutTransition transition;
	private boolean listInitializing;
	private int selection;
	
	public MaterialSearchView(@NonNull Context context) {
        this(context, null);
    }

    public MaterialSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
    }

    public MaterialSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0);
	
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		UISearchBar = DataBindingUtil.inflate(inflater, R.layout.view_search, this, true);
		
		imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		etSearch = UISearchBar.etSearch;
		animateSearchView = true;
		
		searchButtonPosition = typedArray.getInteger(R.styleable.MaterialSearchView_search_menu_position, 0);
		String searchHint = typedArray.getString(R.styleable.MaterialSearchView_search_hint);
		int searchTextColor = typedArray.getColor(R.styleable.MaterialSearchView_search_text_color, getResources().getColor(android.R.color.black));
		int searchIconColor = typedArray.getColor(R.styleable.MaterialSearchView_search_icon_color, getResources().getColor(android.R.color.black));
		UISearchBar.imgBack.setOnClickListener(mOnClickListener);
		UISearchBar.imgClear.setOnClickListener(mOnClickListener);
		etSearch.addTextChangedListener(mTextWatcher);
		etSearch.setOnEditorActionListener(mOnEditorActionListener);
		final int imeOptions = typedArray.getInt(R.styleable.MaterialSearchView_search_ime_options, -1);
		if (imeOptions != -1) {
			etSearch.setImeOptions(imeOptions);
		}
		final int inputType = typedArray.getInt(R.styleable.MaterialSearchView_search_input_type, -1);
		if (inputType != -1) {
			etSearch.setInputType(inputType);
		}
		final boolean focusable = typedArray.getBoolean(R.styleable.MaterialSearchView_search_focusable, true);
		etSearch.setFocusable(focusable);
		typedArray.recycle();
	
		etSearch.setHint(searchHint);
		etSearch.setTextColor(searchTextColor);
		setDrawableTint(UISearchBar.imgBack.getDrawable(), searchIconColor);
		setDrawableTint(UISearchBar.imgClear.getDrawable(), searchIconColor);
	
		adapter = new SearchRecyclerAdapter();
		adapter.setHasStableIds(false);
		RecyclerView recyclerView = UISearchBar.recycler;
		recyclerView.setAdapter(adapter);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
	
//		recyclerView.setItemAnimator(null);
		RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
		if (animator instanceof SimpleItemAnimator) {
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		}
		
		recyclerView.setLayoutManager(new LinearLayoutManager(context){
			@Override
			public int scrollVerticallyBy ( int dx, RecyclerView.Recycler recycler, RecyclerView.State state ) {
				int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
				if(dx!=scrollRange) {
					hideKeyboard();
				}
				return scrollRange;
			}
		});
		recyclerView.setOnScrollChangedListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			if(recyclerView.getScrollState()==RecyclerView.SCROLL_STATE_DRAGGING) {
				hideKeyboard();
			}
		});
		recyclerView.addItemDecoration(new RecyclerView.ItemDecoration(){
			final ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
			final int mDividerHeight = 3;
			@Override
			public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
				final int childCount = parent.getChildCount();
				final int width = parent.getWidth();
				for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
					final ViewGroup view = (ViewGroup) parent.getChildAt(childViewIndex);
					if (parent.getChildViewHolder(view).getLayoutPosition()==selection) {
						int top = (int) view.getY() + view.getHeight();
						int color =  0xff4F7FDF;//Color.GRAY;
						mDivider.setColor(color);
						int pad = (int) (GlobalOptions.density*12);
						mDivider.setBounds(pad, top, width-pad, top + mDividerHeight);
						mDivider.draw(c);
					}
				}
			}
		});
		TouchSortHandler touchHandler = new TouchSortHandler(new TouchSortHandler.MoveSwapAdapter() {
			@Override public void onMove(int fromPosition, int toPosition) {}
			@Override public void onDragFinished(RecyclerView.ViewHolder viewHolder) { }
			@Override
			public void onSwiped(int position) {
				if (position<selection) {
					selection--;
				} else if(position==selection) {
					selection=-1;
				}
				displayData.remove(position);
				adapter.notifyItemRemoved(position);
			}
		}, 0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
		touchHandler.alterAlpha = false;
		ItemTouchHelper touchHelper=new ItemTouchHelper(touchHandler);
		touchHelper.attachToRecyclerView(recyclerView);
	
		searchRecords.add("HAHAHA");
		searchRecords.add("LALALA");
		for (int i = 0; i < 8; i++) {
			searchRecords.add("LALALA");
		}
		displayData = searchRecords;
	
		transition = UISearchBar.resultPanel.getLayoutTransition();
		UISearchBar.resultPanel.setLayoutTransition(null);
	
		setResultInvisible();
		//etSearch.setText("草");
		etSearch.setText("");
		listInitializing = true;
    }
	
	public void setSearchAdapter(SearchResultsAdapter adapter) {
		mSearchResultsAdapter = adapter;
	}
	
	public void setResults(ArrayList results, int selection) {
		CMN.Log("setResults::", results.size(), selection);
		this.selection = selection;
		displayData = searchResults = results;
		adapter.notifyDataSetChanged();
	}
	
	SearchResultsAdapter mSearchResultsAdapter;
	
	public void refreshSearch() {
		if (mSearchResultsAdapter!=null) {
			mSearchResultsAdapter.onQueryTextChange(etSearch.getText());
		}
	}
	
	class SearchRecyclerAdapter extends RecyclerView.Adapter<ViewUtils.ViewDataHolder<SearchViewResultItemBinding>> {
		SearchRecyclerAdapter() {
		}
		@Override
		public ViewUtils.ViewDataHolder<SearchViewResultItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ViewUtils.ViewDataHolder<SearchViewResultItemBinding> ret = new ViewUtils.ViewDataHolder<>(SearchViewResultItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
			Utils.setOnClickListenersOneDepth((ViewGroup) ret.itemView, mOnClickListener, 999, null);
			return ret;
		}
		@Override
		public void onBindViewHolder(@NonNull ViewUtils.ViewDataHolder<SearchViewResultItemBinding> holder, int position) {
			Object itemObj = displayData.get(position);
			String text;
			if (itemObj instanceof CharSequence) {
				text = itemObj.toString();
			} else {
				text = mSearchResultsAdapter.getText(itemObj);
			}
			holder.data.imgLocate.setVisibility(View.GONE);
			holder.data.setText(text);
		}
		@Override
		public int getItemCount() {
			return displayData.size();
		}
		@Override
		public long getItemId(int position) {
			//return CMN.id(displayData.get(position));
			return position;
		}
	}
	
	public Object getDisplayItem(int position) {
		return displayData.get(position);
	}

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	switch (v.getId()) {
				case R.id.imgClear:
					etSearch.setText(null);
				break;
				case R.id.imgBack:
					hide();
				break;
				case R.id.imgLocate:
				case R.id.itemHolder:
					RecyclerView.ViewHolder viewDataHolder = Utils.getViewHolderInParents(v);
					int position = viewDataHolder.getLayoutPosition();
					if (displayData==searchRecords) {
						etSearch.setText(searchRecords.get(position));
					} else if(mSearchResultsAdapter.onItemClick(searchResults.get(position), v.getId()==R.id.imgLocate)){
						selection = position;
					}
				break;
			}
        }
    };

    Runnable searchRunnable = new Runnable() {
		@Override
		public void run() {
			Editable editable = etSearch.getText();
			//SearchBarUI.imgClear.setVisibility(TextUtils.isEmpty(editable) ? GONE : VISIBLE);
			mSearchResultsAdapter.onQueryTextChange(editable);
		}
	};
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int before, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int after) { }
        @Override public void afterTextChanged(Editable s) {
//			searchRunnable.run();
			removeCallbacks(searchRunnable);
			postDelayed(searchRunnable, 100);
        }
    };

    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			mSearchResultsAdapter.onQueryTextSubmit(etSearch.getText().toString());
            return true;
        }
    };

    public void show(ViewGroup tmpView, boolean showKeyboard) {
    	if(!visible) {
			visible = true;
			RecyclerView resultList = UISearchBar.recycler;
			LinearLayoutManager lm = (LinearLayoutManager) resultList.getLayoutManager();
			int cc = resultList.getChildCount();
			int scrollOffset = (int)(1*48*GlobalOptions.density);
			if (d!=null && d.getWindow().getDecorView().getHeight()<scrollOffset) {
				scrollOffset = 0;
			}
			if (listInitializing) {
				if (tmpView!=null) {
					Utils.addViewToParent(UISearchBar.linearItemsHolder, tmpView);
					setResultVisible();
					UISearchBar.linearItemsHolder.setVisibility(View.INVISIBLE);
				}
				UISearchBar.resultPanel.setLayoutTransition(transition);
				//setResultInvisible();
				listInitializing = false;
				int finalScrollOffset = scrollOffset;
				postDelayed(() -> lm.scrollToPositionWithOffset(selection, finalScrollOffset), 120);
			} else if (selection!=-1) {
				//int fvp = selection-lm.findFirstCompletelyVisibleItemPosition();
				//if (fvp<0 || fvp>cc) {
				//	lm.scrollToPositionWithOffset(selection, scrollOffset);
				//}
				lm.scrollToPositionWithOffset(selection, scrollOffset);
			}
			if (showKeyboard) {
				etSearch.requestFocus();
				imm.showSoftInput(etSearch, 0);
			}
			setVisibility(View.VISIBLE);
			if (animateSearchView) {
				if (Utils.bigCake) {
					try {
						animatorShow = ViewAnimationUtils.createCircularReveal(
								this, // view
								getCenterX(), // center x
								dpToPixel(23), // center y
								0, // start radius
								(float) Math.hypot(getWidth(), getHeight()) // end radius
						);
						animatorShow.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								Utils.addViewToParent(UISearchBar.linearItemsHolder, UISearchBar.resultPanel);
								setResultVisible();
								UISearchBar.resultPanel.setLayoutTransition(null); // 由于列表长度变化时，Transition闪烁问题禁用LayoutTransition。
								animation.removeAllListeners();
								animatorShow = null;
							}
						});
						animatorShow.start();
					} catch (Exception ignored) { }
				} else {
					setResultVisible();
				}
			}
		}
    }
	
	private void setResultVisible() {
		UISearchBar.linearItemsHolder.setVisibility(View.VISIBLE);
		//SearchBarUIData.linearItemsHolder.getLayoutParams().height = -2;
		//SearchBarUIData.linearItemsHolder.requestLayout();
	}
	
	private void setResultInvisible() {
		UISearchBar.linearItemsHolder.setVisibility(View.GONE);
		//SearchBarUIData.linearItemsHolder.getLayoutParams().height = 0;
	}
	
	public void hide() {
    	if (visible) {
			UISearchBar.resultPanel.setLayoutTransition(transition); // 重新启用LayoutTransition。
			if(animatorShow!=null) {
				animatorShow.removeAllListeners();
				animatorShow.cancel();
			}
			visible = false;
			post(new Runnable() {
				@Override
				public void run() {
					setResultInvisible();
					if (animateSearchView) {
						if (Build.VERSION.SDK_INT >= 21) {
							Animator animatorHide = ViewAnimationUtils.createCircularReveal(
									MaterialSearchView.this, // View
									getCenterX(), // center x
									dpToPixel(23), // center y
									(float) Math.hypot(getWidth(), getHeight()), // start radius
									0// end radius
							);
							animatorHide.setStartDelay(true ? ANIMATION_DURATION : 0);
							//animatorHide.setStartDelay(1000);
							animatorHide.addListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									//if (visibilityListener != null) visibilityListener.onClose();
									if (d!=null) d.dismiss();
									if (p!=null) p.dismiss();
									animation.removeAllListeners();
									//setVisibility(GONE);
								}
							});
							animatorHide.start();
						} else {
							setVisibility(GONE);
						}
					}
					// 如不延时，发现收起临界闪烁
					postDelayed(MaterialSearchView.this::hideKeyboard, 100);
				}
			});
		}
    }
    
	private void hideKeyboard() {
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(),0);
	}

    public void setDrawableTint(Drawable resDrawable, int resColor) {
        resDrawable.setColorFilter(new PorterDuffColorFilter(resColor, PorterDuff.Mode.SRC_ATOP));
        resDrawable.mutate();
    }

    private int getCenterX() {
        int icons = getWidth() - dpToPixel(21 * (1 + searchButtonPosition));
        int padding = dpToPixel(searchButtonPosition * 21);
        return icons - padding;
    }
}
