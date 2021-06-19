package com.knziha.polymer.widgets.eugene;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.knziha.polymer.R;
import com.knziha.polymer.databinding.SearchViewResultItemBinding;
import com.knziha.polymer.databinding.ViewSearchBinding;
import com.knziha.polymer.webslideshow.ViewUtils;
import com.knziha.polymer.widgets.Utils;

import java.util.ArrayList;
import java.util.List;

public class MaterialSearchView extends CardView {
    static final String LOG_TAG = "MaterialSearchView";
    private static final int ANIMATION_DURATION = 250;

    private boolean animateSearchView;
    private int searchMenuPosition;
	public String searchHint;
    private int searchTextColor;
    private Integer searchIconColor;
    private CharSequence mUserQuery;
    private boolean hasAdapter = false;
    private boolean visible = false;

    public ViewSearchBinding SearchBarUIData;
	private SearchRecyclerAdapter adapter;
    //private OnVisibilityListener visibilityListener;
	
	public final EditText etSearch;
    ArrayList<String> searchRecords = new ArrayList<>();
    ArrayList searchResults;
    List displayData;
	public Dialog d;
	private LayoutTransition transitionBK;
	private LayoutTransition transition;
	
    public MaterialSearchView(@NonNull Context context) {
        this(context, null);
    }

    public MaterialSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
    }

    public MaterialSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0);
		final LayoutInflater inflater = LayoutInflater.from(context);
		SearchBarUIData = DataBindingUtil.inflate(inflater, R.layout.view_search, this, true);
		etSearch = SearchBarUIData.editText;
		animateSearchView = a.getBoolean(R.styleable.MaterialSearchView_search_animate, true);
		searchMenuPosition = a.getInteger(R.styleable.MaterialSearchView_search_menu_position, 0);
		searchHint = a.getString(R.styleable.MaterialSearchView_search_hint);
		searchTextColor = a.getColor(R.styleable.MaterialSearchView_search_text_color, getResources().getColor(android.R.color.black));
		searchIconColor = a.getColor(R.styleable.MaterialSearchView_search_icon_color, getResources().getColor(android.R.color.black));
		SearchBarUIData.imgBack.setOnClickListener(mOnClickListener);
		SearchBarUIData.imgClear.setOnClickListener(mOnClickListener);
		SearchBarUIData.editText.addTextChangedListener(mTextWatcher);
		SearchBarUIData.editText.setOnEditorActionListener(mOnEditorActionListener);
		final int imeOptions = a.getInt(R.styleable.MaterialSearchView_search_ime_options, -1);
		if (imeOptions != -1) {
			setImeOptions(imeOptions);
		}
		final int inputType = a.getInt(R.styleable.MaterialSearchView_search_input_type, -1);
		if (inputType != -1) {
			setInputType(inputType);
		}
		boolean focusable;
		focusable = a.getBoolean(R.styleable.MaterialSearchView_search_focusable, true);
		etSearch.setFocusable(focusable);
		a.recycle();
	
		etSearch.setHint(searchHint);
		etSearch.setTextColor(searchTextColor);
		setDrawableTint(SearchBarUIData.imgBack.getDrawable(), searchIconColor);
		setDrawableTint(SearchBarUIData.imgClear.getDrawable(), searchIconColor);
	
		adapter = new SearchRecyclerAdapter();
		adapter.setHasStableIds(false);
		RecyclerView recyclerView = SearchBarUIData.recycler;
		recyclerView.setAdapter(adapter);
		recyclerView.setItemAnimator(null);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
		RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
		recyclerView.setOnScrollChangedListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			hideKeyboard();
		});
		if (animator instanceof SimpleItemAnimator) {
			((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
		}
	
		searchRecords.add("HAHAHA");
		searchRecords.add("LALALA");
		displayData = searchRecords;
		checkForAdapter();
	
		transitionBK = transition = SearchBarUIData.relHolder.getLayoutTransition();
		SearchBarUIData.relHolder.setLayoutTransition(null);
	
		//etSearch.setText("草");
		etSearch.setText("");
    }
	
	public void setSearchAdapter(SearchResultsAdapter adapter) {
		mSearchResultsAdapter = adapter;
	}
	
	public void setResults(ArrayList results) {
//		SearchBarUIData.relHolder.setLayoutTransition(null);
		displayData = searchResults = results;
		adapter.notifyDataSetChanged();
//		SearchBarUIData.recycler.post(new Runnable() {
//			@Override
//			public void run() {
//				SearchBarUIData.relHolder.setLayoutTransition(transitionBK);
//			}
//		});
	}
	
	SearchResultsAdapter mSearchResultsAdapter;
	
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
					setSearchText(null);
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
					} else {
						mSearchResultsAdapter.onItemClick(searchResults.get(position), v.getId()==R.id.imgLocate);
					}
				break;
			}
        }
    };
    /**
     * Callback to watch the text field for empty/non-empty
     */
    Runnable searchRunnable = new Runnable() {
		@Override
		public void run() {
			//submitText(s);
			Editable editable = SearchBarUIData.editText.getText();
			SearchBarUIData.imgClear.setVisibility(TextUtils.isEmpty(editable) ? GONE : VISIBLE);
			//mUserQuery = s;
			//CMN.Log("onTextChanged", CMN.id(s), CMN.id(SearchBarUIData.editText.getText()));
			mUserQuery = editable;
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
            onSubmitQuery();
            return true;
        }
    };

    void onSubmitQuery() {
		SearchBarUIData.linearItemsHolder.setVisibility(GONE);
		mSearchResultsAdapter.onQueryTextSubmit(SearchBarUIData.editText.getText().toString());
    }

    public void setQuery(CharSequence query, boolean submit) {
        SearchBarUIData.editText.setText(query);
        if (query != null) {
            mUserQuery = query;
        }
        // If the query is not empty and submit is requested, submit the query
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    public void setImeOptions(int imeOptions) {
        SearchBarUIData.editText.setImeOptions(imeOptions);
    }

    public void setInputType(int inputType) {
        SearchBarUIData.editText.setInputType(inputType);
    }

    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    public void setSearchText(String queryText) {
        SearchBarUIData.editText.setText(queryText);
    }
	
	Animator animatorShow;

    public void show(boolean showKeyboard) {
    	if(!visible) {
			visible = true;
			if (transition!=null) {
				SearchBarUIData.linearItemsHolder.setVisibility(View.GONE);
				SearchBarUIData.relHolder.setLayoutTransition(transition);
				transition = null;
			}
			//transition.enableTransitionType(LayoutTransition.APPEARING);
			if (showKeyboard) {
				InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				etSearch.requestFocus();
				imm.showSoftInput(etSearch, 0);
			}
			checkForAdapter();
			setVisibility(View.VISIBLE);
			if (animateSearchView) {
				if (Utils.bigCake) {
					try {
						animatorShow = ViewAnimationUtils.createCircularReveal(
								this, // view
								getCenterX(), // center x
								(int) convertDpToPixel(23), // center y
								0, // start radius
								(float) Math.hypot(getWidth(), getHeight()) // end radius
						);
						animatorShow.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								SearchBarUIData.linearItemsHolder.setVisibility(View.VISIBLE);
								SearchBarUIData.relHolder.setLayoutTransition(null); // 由于列表长度变化时，Transition闪烁问题禁用LayoutTransition。
							}
						});
						animatorShow.start();
					} catch (Exception ignored) { }
				} else {
					if (hasAdapter) {
						SearchBarUIData.linearItemsHolder.setVisibility(View.VISIBLE);
					}
				}
			}
		}
    }

    public void hide() {
    	if (visible) {
			SearchBarUIData.relHolder.setLayoutTransition(transitionBK); // 重新启用LayoutTransition。
			if(animatorShow!=null) {
				animatorShow.removeAllListeners();
				animatorShow.cancel();
			}
			visible = false;
			post(new Runnable() {
				@Override
				public void run() {
					SearchBarUIData.linearItemsHolder.setVisibility(View.GONE);
					if (animateSearchView) {
						if (Build.VERSION.SDK_INT >= 21) {
							Animator animatorHide = ViewAnimationUtils.createCircularReveal(
									MaterialSearchView.this, // View
									getCenterX(), // center x
									(int) convertDpToPixel(23), // center y
									(float) Math.hypot(getWidth(), getHeight()), // start radius
									0// end radius
							);
							animatorHide.setStartDelay(hasAdapter ? ANIMATION_DURATION : 0);
							//animatorHide.setStartDelay(1000);
							animatorHide.addListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									//if (visibilityListener != null) visibilityListener.onClose();
									if (d!=null) {
										d.dismiss();
									}
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
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(),0);
	}
	
    public void setMenuPosition(int menuPosition) {
        this.searchMenuPosition = menuPosition;
        invalidate();
        requestFocus();
    }

    public void setSearchHint(String searchHint) {
        this.searchHint = searchHint;
        invalidate();
        requestFocus();
    }

    // text color
    public void setTextColor(int textColor) {
        this.searchTextColor = textColor;
        invalidate();
        requestFocus();
    }

    public void setSearchIconColor(int searchIconColor) {
        this.searchIconColor = searchIconColor;
        invalidate();
        requestFocus();
    }

    public int getTextColor() {
        return searchTextColor;
    }

    public ImageView getImageBack() {
        return SearchBarUIData.imgBack;
    }

    public ImageView getImageClear() {
        return SearchBarUIData.imgClear;
    }

    public RecyclerView getRecyclerView() {
        return SearchBarUIData.recycler;
    }

//    public interface OnVisibilityListener {
//        boolean onOpen();
//        boolean onClose();
//    }

    /**
     * Helpers
     */
    public void setDrawableTint(Drawable resDrawable, int resColor) {
        resDrawable.setColorFilter(new PorterDuffColorFilter(resColor, PorterDuff.Mode.SRC_ATOP));
        resDrawable.mutate();
    }

    public float convertDpToPixel(float dp) {
        return dp * (getContext().getResources().getDisplayMetrics().densityDpi / 160f);
    }

    private void checkForAdapter() {
        hasAdapter = true;
    }

    /*
    TODO not correct but close
    Need to do correct measure
     */
    private int getCenterX() {
        int icons = (int) (getWidth() - convertDpToPixel(21 * (1 + searchMenuPosition)));
        int padding = (int) convertDpToPixel(searchMenuPosition * 21);
        return icons - padding;
    }
}
