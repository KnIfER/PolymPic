package com.knziha.polymer.widgets.iammert;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IU;
import com.knziha.polymer.databinding.ViewItemBinding;
import com.knziha.polymer.databinding.ViewMultiSearchContainerBinding;
import com.knziha.polymer.widgets.TextWatcherAdapter;
import com.knziha.polymer.widgets.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;

public class MultiSearchContainerView extends FrameLayout {
	private final float WIDTH_RATIO = 0.85f;
	private final long DEFAULT_ANIM_DURATION = 500L;;
	
	int searchTextStyle = 0;
	
	private float searchViewWidth = 0f;
	
	private float viewWidth = 0f;
	
	private ViewMultiSearchContainerBinding binding;
	
	private int sizeRemoveIcon;
	
	private int defaultPadding;
	
	private boolean isInSearchMode = false;
	
	private ViewItemBinding selectedTab;
	
	private MultiSearchView.MultiSearchViewListener multiSearchViewListener;
	
	
	public MultiSearchContainerView(@NonNull Context context) {
		this(context, null);
	}
	
	public MultiSearchContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	ValueAnimator searchEnterScrollAnimation = ValueAnimator.ofInt().setDuration(DEFAULT_ANIM_DURATION);
	ValueAnimator searchCompleteCollapseAnimator = ValueAnimator.ofInt().setDuration(DEFAULT_ANIM_DURATION);
	ValueAnimator firstSearchTranslateAnimator = ValueAnimator.ofInt().setDuration(DEFAULT_ANIM_DURATION);
	ValueAnimator indicatorAnimator = ValueAnimator.ofInt().setDuration(DEFAULT_ANIM_DURATION);
	
	public MultiSearchContainerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		sizeRemoveIcon = context.getResources().getDimensionPixelSize(R.dimen.msv_size_remove_icon);
		defaultPadding = context.getResources().getDimensionPixelSize(R.dimen.padding_16dp);
		
		binding = ViewMultiSearchContainerBinding.inflate(LayoutInflater.from(context), this, true);
		LayoutTransition trans = new LayoutTransition();
		trans.disableTransitionType(LayoutTransition.APPEARING);
		trans.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
		binding.layoutItemContainer.setLayoutTransition(trans);
		
		
		searchEnterScrollAnimation.setInterpolator(new LinearOutSlowInInterpolator());
		searchEnterScrollAnimation.addUpdateListener(it ->
				binding.horizontalScrollView.smoothScrollTo((int)it.getAnimatedValue(), 0));
		searchEnterScrollAnimation.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if (selectedTab!=null) {
							selectedTab.editTextSearch.requestFocus();
						}
						//showKeyboard(context)
					}
				});
				
		searchCompleteCollapseAnimator.setInterpolator(new LinearOutSlowInInterpolator());
		searchCompleteCollapseAnimator.addUpdateListener(it -> {
			if (selectedTab!=null) {
				selectedTab.getRoot().getLayoutParams().width = (int) it.getAnimatedValue();
			}
			//selectedTab.getRoot().requestLayout();
		});
		
		firstSearchTranslateAnimator.setInterpolator(new LinearOutSlowInInterpolator());
		firstSearchTranslateAnimator.addUpdateListener(it ->
				binding.horizontalScrollView.setTranslationX((float)it.getAnimatedValue()));
		firstSearchTranslateAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if (selectedTab!=null) {
							selectedTab.editTextSearch.requestFocus();
						}
						//showKeyboard(context)
					}
				});
		
		indicatorAnimator.setInterpolator(new LinearOutSlowInInterpolator());
		indicatorAnimator.addUpdateListener(it -> binding.viewIndicator.setX((float)it.getAnimatedValue()));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		viewWidth = getMeasuredWidth();
		searchViewWidth = viewWidth * WIDTH_RATIO;
	}
	
	public void setSearchViewListener(MultiSearchView.MultiSearchViewListener multiSearchViewListener) {
		this.multiSearchViewListener = multiSearchViewListener;
	}
	
	void search() {
		if (isInSearchMode) {
			return;
		}
		
		if (selectedTab!=null) {
			deselectTab(selectedTab);
		}
		
		isInSearchMode = true;
		selectedTab = createNewSearchView();
		if (selectedTab!=null) {
			binding.layoutItemContainer.addView(selectedTab.getRoot());
			View v = selectedTab.getRoot();
			v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (v.getMeasuredWidth() > 0 && v.getMeasuredHeight() > 0) {
						v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						
						int widthWithoutCurrentSearch = widthWithoutCurrentSearch();
						
						if (widthWithoutCurrentSearch == 0) {
							firstSearchTranslateAnimator.setFloatValues(viewWidth, 0f);
							firstSearchTranslateAnimator.start();
						} else if (widthWithoutCurrentSearch < viewWidth) {
							int scrollEnterStartValue = 0;
							int scrollEnterEndValue = (int) (binding.layoutItemContainer.getMeasuredWidth() - viewWidth);
							searchEnterScrollAnimation.setIntValues(scrollEnterStartValue, scrollEnterEndValue);
							searchEnterScrollAnimation.start();
						} else {
							int scrollEnterStartValue = (int) (widthWithoutCurrentSearch - viewWidth);
							int scrollEnterEndValue = (int) (widthWithoutCurrentSearch - viewWidth + searchViewWidth);
							searchEnterScrollAnimation.setIntValues(scrollEnterStartValue, scrollEnterEndValue);
							searchEnterScrollAnimation.start();
						}
					}
				}
			});
		}
		
	}
	
	void completeSearch() {
		if (!isInSearchMode) {
			return;
		}
		
		isInSearchMode = false;
		//hideKeyboard(context)
		ViewItemBinding it = selectedTab;
		if (it!=null) {
			if (it.editTextSearch.getText().length() < 3) {
				removeTab(it);
			}
			it.editTextSearch.setFocusable(false);
			it.editTextSearch.setFocusableInTouchMode(false);
			it.editTextSearch.clearFocus();
			
			int startWidthValue = it.getRoot().getMeasuredWidth();
			int endWidthValue = it.editTextSearch.getMeasuredWidth() + sizeRemoveIcon + defaultPadding;
			searchCompleteCollapseAnimator.setIntValues(startWidthValue, endWidthValue);
			searchCompleteCollapseAnimator.start();
			if (multiSearchViewListener!=null) {
				multiSearchViewListener.onSearchComplete(
						binding.layoutItemContainer.getChildCount() - 1,
						it.editTextSearch.getText()
				);
			}
			
			selectTab(it);
		}
	}
	
	public boolean isInSearchMode() {
		return isInSearchMode;
	}
	
	private static class EditTextAttributes {
		int padding = -1;
		int textSize = -1;
		int maxLength = -1;
		int inputType = -1;
		int imeOptions = -1;
		int layoutWidth = -1;
		int layoutHeight = -1;
		int textColorHighlight = -1;
		int paddingTop = 0;
		int paddingLeft = 0;
		int paddingRight = 0;
		int paddingBottom = 0;
		int textStyle = Typeface.NORMAL;
		String fontFamily = "";
		boolean enabled = true;
		boolean focusable = true;
		boolean textAllCaps = false;
		boolean focusableInTouchMode = true;
		String hint=null;
		ColorStateList textColorHint=null;
		ColorStateList textColor=null;
		ColorStateList textColorLink=null;
		
		final static int attrLayoutWidth = android.R.attr.layout_width;
		final static int attrLayoutHeight = android.R.attr.layout_height;
		final static int attrFocusable = android.R.attr.focusable;
		final static int attrFocusableInTouchMode = android.R.attr.focusableInTouchMode;
		final static int attrEnabled = android.R.attr.enabled;
		final static int attrHint = android.R.attr.hint;
		final static int attrImeOptions = android.R.attr.imeOptions;
		final static int attrMaxLength = android.R.attr.maxLength;
		final static int attrTextSize = android.R.attr.textSize;
		final static int attrInputType = android.R.attr.inputType;
		final static int attrTextColorHint = android.R.attr.textColorHint;
		final static int attrTextColor = android.R.attr.textColor;
		final static int attrAllCaps = android.R.attr.textAllCaps;
		final static int attrPadding = android.R.attr.padding;
		final static int attrPaddingTop = android.R.attr.paddingTop;
		final static int attrPaddingBottom = android.R.attr.paddingBottom;
		final static int attrPaddingRight = android.R.attr.paddingRight;
		final static int attrPaddingLeft = android.R.attr.paddingLeft;
		final static int attrTextColorHighlight = android.R.attr.textColorHighlight;
		final static int attrTextColorLink = android.R.attr.text;
		final static int attrTextStyle = android.R.attr.textStyle;
		final static int attrFontFamily = android.R.attr.fontFamily;
		
		public static int[] getAttributesList() {
			int[] arr = new int[]{attrLayoutWidth,
					attrLayoutHeight,
					attrFocusable,
					attrFocusableInTouchMode,
					attrEnabled,
					attrPadding,
					attrHint,
					attrPaddingTop,
					attrPaddingBottom,
					attrPaddingRight,
					attrPaddingLeft,
					attrImeOptions,
					attrMaxLength,
					attrTextSize,
					attrTextStyle,
					attrFontFamily,
					attrInputType,
					attrTextColorHint,
					attrTextColorHighlight,
					attrTextColor,
					attrTextColorLink,
					attrAllCaps};
			Arrays.sort(arr);
			return arr;
		}
	}
	private void applyStyle(EditText editText, EditTextAttributes editTextAttributes) {
		ArrayList<InputFilter> editTextFilters = new ArrayList<>();
		if (editTextAttributes.layoutWidth != -1) {
			editText.getLayoutParams().width = editTextAttributes.layoutWidth;
		}
		if (editTextAttributes.layoutHeight != -1) {
			editText.getLayoutParams().height = editTextAttributes.layoutHeight;
		}
		if (!editTextAttributes.focusable) {
			editText.setFocusable(editTextAttributes.focusable);
		}
		if (!editTextAttributes.enabled) {
			editText.setEnabled(editTextAttributes.enabled);
		}
		if (editTextAttributes.imeOptions != -1) {
			editText.setImeOptions(editTextAttributes.imeOptions);
		}
		if (editTextAttributes.maxLength != -1) {
			editTextFilters.add(new InputFilter.LengthFilter(editTextAttributes.maxLength));
		}
		if (editTextAttributes.textSize != -1) {
			editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextAttributes.textSize);
		}
		if (editTextAttributes.inputType != -1) {
			editText.setInputType(editTextAttributes.inputType);
		}
		if (editTextAttributes.textAllCaps) {
			editTextFilters.add(new InputFilter.AllCaps());
		}
		if (editTextAttributes.textColorHighlight != -1) {
			editText.setHighlightColor(editTextAttributes.textColorHighlight);
		}
		if (editTextAttributes.padding != -1) {
			editText.setPaddingRelative(
					editTextAttributes.padding,
					editTextAttributes.padding,
					editTextAttributes.padding,
					editTextAttributes.padding
			);
		} else {
			editText.setPaddingRelative(
					editTextAttributes.paddingLeft,
					editTextAttributes.paddingTop,
					editTextAttributes.paddingRight,
					editTextAttributes.paddingBottom
			);
		}
		if (editTextAttributes.textColorHint != null) {
			editText.setHintTextColor(editTextAttributes.textColorHint);
		}
		if (editTextAttributes.textColor != null) {
			editText.setTextColor(editTextAttributes.textColor);
		}
		if (editTextAttributes.textColorLink != null) {
			editText.setLinkTextColor(editTextAttributes.textColorLink);
		}
		if (!editTextAttributes.focusableInTouchMode) {
			editText.setFocusableInTouchMode(editTextAttributes.focusableInTouchMode);
		}
		if (!TextUtils.isEmpty(editTextAttributes.hint)) {
			editText.setHint(editTextAttributes.hint);
		}
		if (editTextFilters.size() > 0) {
			editText.setFilters(editTextFilters.toArray(new InputFilter[]{}));
		}
		Typeface customTypeface = Typeface.create(editTextAttributes.fontFamily, editTextAttributes.textStyle);
		editText.setTypeface(customTypeface);
	}
	
	private void readStyle(EditText editTextSearch, TypedArray typedArray, int[] attributes) {
		EditTextAttributes editTextAttributes = new EditTextAttributes();
		int typedArraySize = typedArray.length() - 1;
		int attribute;
		for (int index = 0; index < typedArraySize; index++) {
			attribute = attributes[index];
			if (attribute== EditTextAttributes.attrLayoutWidth) {
				editTextAttributes.layoutWidth = typedArray.getLayoutDimension(index, -1);
			} else if (attribute== EditTextAttributes.attrLayoutHeight) {
				editTextAttributes.layoutHeight = typedArray.getLayoutDimension(index, -1);
			} else if (attribute== EditTextAttributes.attrFocusable) {
				editTextAttributes.focusable = typedArray.getBoolean(index, true);
			} else if (attribute== EditTextAttributes.attrFocusableInTouchMode) {
				editTextAttributes.focusableInTouchMode = typedArray.getBoolean(index, true);
			} else if (attribute== EditTextAttributes.attrEnabled) {
				editTextAttributes.enabled = typedArray.getBoolean(index, true);
			} else if (attribute== EditTextAttributes.attrHint) {
				editTextAttributes.hint = typedArray.getString(index);
			} else if (attribute== EditTextAttributes.attrImeOptions) {
				editTextAttributes.imeOptions = typedArray.getInt(index, -1);
			} else if (attribute== EditTextAttributes.attrMaxLength) {
				editTextAttributes.maxLength = typedArray.getInt(index, -1);
			} else if (attribute== EditTextAttributes.attrTextSize) {
				editTextAttributes.textSize = typedArray.getDimensionPixelSize(index, -1);
			}  else if (attribute== EditTextAttributes.attrTextStyle) {
				editTextAttributes.textStyle = typedArray.getInt(index, Typeface.NORMAL);
			} else if (attribute== EditTextAttributes.attrInputType) {
				editTextAttributes.inputType = typedArray.getInt(index, EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
			} else if (attribute== EditTextAttributes.attrTextColorHint) {
				editTextAttributes.textColorHint = typedArray.getColorStateList(index);
			} else if (attribute== EditTextAttributes.attrTextColor) {
				editTextAttributes.textColor = typedArray.getColorStateList(index);
			} else if (attribute== EditTextAttributes.attrFontFamily) {
				String ff = typedArray.getString(index);
				if (ff!=null) {
					editTextAttributes.fontFamily = ff;
				}
			} else if (attribute== EditTextAttributes.attrTextColorLink) {
				editTextAttributes.textColorLink = typedArray.getColorStateList(index);
			} else if (attribute== EditTextAttributes.attrTextColorHighlight) {
				editTextAttributes.textColorHighlight = typedArray.getColor(index, -1);
			} else if (attribute== EditTextAttributes.attrAllCaps) {
				editTextAttributes.textAllCaps = typedArray.getBoolean(index, false);
			} else if (attribute== EditTextAttributes.attrPadding) {
				editTextAttributes.padding = typedArray.getLayoutDimension(index, -1);
			} else if (attribute== EditTextAttributes.attrPaddingTop) {
				editTextAttributes.paddingTop = typedArray.getLayoutDimension(index, 0);
			} else if (attribute== EditTextAttributes.attrPaddingBottom) {
				editTextAttributes.paddingBottom = typedArray.getLayoutDimension(index, 0);
			} else if (attribute== EditTextAttributes.attrPaddingLeft) {
				editTextAttributes.paddingLeft = typedArray.getLayoutDimension(index, 0);
			} else if (attribute== EditTextAttributes.attrPaddingRight) {
				editTextAttributes.paddingRight = typedArray.getLayoutDimension(index, 0);
			}
		}
		applyStyle(editTextSearch, editTextAttributes);
		typedArray.recycle();
	}
	
	private ViewItemBinding createNewSearchView() {
		ViewItemBinding viewItem = ViewItemBinding.inflate(LayoutInflater.from(getContext()));
		//viewItem.editTextSearch.setStyle(getContext(), searchTextStyle);
		int[] attributes = EditTextAttributes.getAttributesList();
		TypedArray typedArray = getContext().obtainStyledAttributes(searchTextStyle, attributes);
		readStyle(viewItem.editTextSearch, typedArray, attributes);
		
		viewItem.getRoot().setLayoutParams(new LinearLayout.LayoutParams((int) searchViewWidth, WRAP_CONTENT));
		
		viewItem.getRoot().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (viewItem != selectedTab) {
					if (multiSearchViewListener!=null) {
						multiSearchViewListener.onItemSelected(
								binding.layoutItemContainer.indexOfChild(viewItem.getRoot()),
								viewItem.editTextSearch.getText());
					}
					changeSelectedTab(viewItem);
				}
			}
		});
		
		viewItem.editTextSearch.setOnClickListener(v -> {
			if (viewItem != selectedTab) {
				if (multiSearchViewListener!=null) {
					multiSearchViewListener.onItemSelected(
							binding.layoutItemContainer.indexOfChild(viewItem.getRoot()),
							viewItem.editTextSearch.getText()
					);
				}
				changeSelectedTab(viewItem);
			}
		});
		
		viewItem.editTextSearch.addTextChangedListener(new TextWatcherAdapter() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (multiSearchViewListener!=null) {
					multiSearchViewListener.onTextChanged(binding.layoutItemContainer.getChildCount() - 1, s);
				}
			}
		});
		
		viewItem.imageViewRemove.setOnClickListener(v -> removeTab(selectedTab));
		
		viewItem.editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				if (isInSearchMode) {
					completeSearch();
				}
				return true;
			}
			return false;
		});
		return viewItem;
	}
	
	private int widthWithoutCurrentSearch() {
		int cc=binding.layoutItemContainer.getChildCount();
		if (cc>1) {
			int totalWith = 0;
			for (int i = 0; i < cc; i++) {
				totalWith += binding.layoutItemContainer.getChildAt(i).getMeasuredWidth();
			}
			return totalWith;
		}
		return 0;
	}
	
	private void removeTab(ViewItemBinding viewItemBinding) {
		int removeIndex = binding.layoutItemContainer.indexOfChild(viewItemBinding.getRoot());
		int currentChildCount = binding.layoutItemContainer.getChildCount();
		if (currentChildCount==1) {
			binding.viewIndicator.setVisibility(View.INVISIBLE);
			binding.layoutItemContainer.removeView(viewItemBinding.getRoot());
		}
		if (removeIndex==currentChildCount - 1) {
			View newSelectedView = binding.layoutItemContainer.getChildAt(removeIndex - 1);
			ViewItemBinding newSelectedViewBinding = null;
			if(newSelectedView!=null) {
				newSelectedViewBinding = DataBindingUtil.bind(newSelectedView);
				selectTab(newSelectedViewBinding);
			}
			Utils.removeView(viewItemBinding.getRoot());
			selectedTab = newSelectedViewBinding;
		} else {
			View newSelectedTabView = binding.layoutItemContainer.getChildAt(removeIndex + 1);
			ViewItemBinding newSelectedViewBinding = null;
			if(newSelectedTabView!=null) {
				newSelectedViewBinding = DataBindingUtil.bind(newSelectedTabView);
				selectTab(newSelectedViewBinding);
			}
			Utils.removeView(viewItemBinding.getRoot());
			selectedTab = newSelectedViewBinding;
		}
		if (multiSearchViewListener!=null) {
			multiSearchViewListener.onSearchItemRemoved(removeIndex);
		}
	}
	
	private void selectTab(ViewItemBinding viewItemBinding) {
		float indicatorCurrentXPosition = binding.viewIndicator.getX();
		float indicatorTargetXPosition = viewItemBinding.getRoot().getX();
		indicatorAnimator.setFloatValues(indicatorCurrentXPosition, indicatorTargetXPosition);
		indicatorAnimator.start();
		
		binding.viewIndicator.setVisibility(View.VISIBLE);
		viewItemBinding.imageViewRemove.setVisibility(View.VISIBLE);
		viewItemBinding.editTextSearch.setAlpha(1f);
		viewItemBinding.editTextSearch.requestFocus();
		viewItemBinding.editTextSearch.setFocusable(true);
		viewItemBinding.editTextSearch.setFocusableInTouchMode(true);
	}
	
	private void deselectTab(ViewItemBinding viewItemBinding) {
		binding.viewIndicator.setVisibility(View.INVISIBLE);
		viewItemBinding.imageViewRemove.setVisibility(View.GONE);
		viewItemBinding.editTextSearch.setAlpha(0.5f);
		viewItemBinding.editTextSearch.setFocusable(false);
	}
	
	private void changeSelectedTab(ViewItemBinding newSelectedTabItem) {
		CMN.Log("changeSelectedTab::"+newSelectedTabItem.editTextSearch.getText());
		deselectTab(selectedTab);
		selectedTab = newSelectedTabItem;
		selectTab(selectedTab);
	}
}
