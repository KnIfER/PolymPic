/*
 * Copyright (C) 2016 Angad Singh Created by Angad Singh on 09-07-2016.
 * Copyright (C) 2019 KnIfER
 * evolved from github.angads25.filepicker
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.filepicker.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.knziha.filepicker.R;
import com.knziha.filepicker.commands.ExeCommand;
import com.knziha.filepicker.model.ArrayListTree;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.model.MarkedItemList;
import com.knziha.filepicker.model.PatternHolder;
import com.knziha.filepicker.model.StorageActivity;
import com.knziha.filepicker.model.ViewDissmisser;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.slideshow.SlideShowActivity;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.filepicker.utils.ExtensionFilter;
import com.knziha.filepicker.utils.FU;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.filepicker.widget.MaterialCheckbox;
import com.knziha.filepicker.widget.NumberKicker;
import com.knziha.filepicker.widget.RecyclerViewmy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import mp4meta.utils.CMN;

public class FilePickerDialog extends AlertDialog implements
             AdapterView.OnItemClickListener,
		WindowChangeHandler,
             View.OnClickListener,OnLongClickListener {
    private DialogProperties properties;
    private DialogSelectionListener callbacks;
    private View root;
    private ExtensionFilter filter;
    private FilePickerAdapter mFileListAdapter;
    private View inter_sel;
    private View new_folder;
    private MaterialCheckbox toggle_all;
    private Snackbar snackbar;
    private View HeaderView;
    private ViewGroup FooterView;
    private ViewGroup bottombar;
    private ViewGroup bottombar2;

    ArrayList<String> favorList;
    HashSet<String> checker = new HashSet<>();
    private ArrayList<FileListItem> internalList;
    public static final byte[] tailing = new byte[] {0x0d,0x0a};
    private String positiveBtnNameStr = null;
    private String negativeBtnNameStr = null;
    public boolean bDontAttach;

    public long FirstFlagStamp;
    private GridViewmy listView;
    public TextView dir_path;
    public TextView title;
    public Button favorite;
    public Button select;
    public View star;
    public View folderCover;
    Animation mShake;
    public int BKMKOff;

    FilePickerOptions opt;
    private RecyclerView.Adapter mBMAdapter;
    private RecyclerViewmy bmlv;

    boolean bIsDeletingFavor;
	private boolean isDirty;
	private LinearLayoutManager layoutManager;
    private File currLocation;

    private int currentStage=-1;
    SpannableStringBuilder spanned = new SpannableStringBuilder();
    SparseArray<MemoClickableSpan> MCSPool = new SparseArray<>();
    private ImageView wiget1,wiget2,wiget3,wiget4,wiget5;
    private AdapterView.OnItemClickListener menu_clicker;
    private boolean old_use_regex;
    private PatternHolder ph = new PatternHolder();
    private View oldSelectedViewMode;
    private View oldSelectedSortMode;
    private Dialog d;
    private ListPopupWindow menupopup;
	private View ComfyView;
	private NumberKicker np1;
	private AppCompatCheckBox ckList;
	private OnDismissListener comfy_dissmiss_l;
	private int colorAccent;


	public FilePickerDialog(Activity context){
    	super(context);    
    	init(context, new DialogProperties());
    }
    
    public FilePickerDialog(Activity context, DialogProperties properties) {
    	super(context);    
    	init(context, properties);
    }

    public FilePickerDialog(Activity context, DialogProperties properties, int themeResId) {
        super(context, themeResId);
        init(context, properties);
    }

    public static void clearMemory(Context context) {
        Glide.get(context.getApplicationContext()).clearMemory();
    }

    private void init(Activity context, DialogProperties properties) {
	    this.properties = properties;
	    filter = new ExtensionFilter(properties);
	    internalList = new ArrayList<>();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public View getView() {
        return root;
    }
    
    public View init() {
    	onCreate(null);
    	onStart();
		return root;
    }

    //@SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//CMNF.Log("oncreate");
        opt = new FilePickerOptions(getContext());
        FileListItem.comparation_method=opt.getSortMode();
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        if(win==null || bDontAttach) {
        	root = getLayoutInflater().inflate(R.layout.dialog_main,null);
        }else {
			//win.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,  WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
			//if(FU.bKindButComplexSdcardAvailable)
        	    win.setBackgroundDrawableResource(properties.isDark? R.drawable.popup_shadow_d: R.drawable.popup_shadow_s);
        	//else win.getDecorView().setBackgroundColor(properties.isDark?0xff666666:0xffcfcfcf);
            win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        	setContentView(R.layout.dialog_main);
        	root = win.getDecorView();//findViewById(R.id.root);
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            win.setLayout((int) (dm.widthPixels-1.8*18*dm.density), -1);
		}
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        listView = root.findViewById(R.id.fileList);
        bmlv = root.findViewById(R.id.favorList);
        FooterView = findViewById(R.id.footer);
        bottombar = FooterView.findViewById(R.id.bottombar);
        bottombar2 = FooterView.findViewById(R.id.bottombar2);
        //bottombar2.setVisibility(View.GONE);
        wiget1=bottombar2.findViewById(R.id.browser_widget7);
        wiget1.setOnClickListener(this);
        wiget2=bottombar2.findViewById(R.id.browser_widget8);
        wiget2.setOnClickListener(this);
        wiget3=bottombar2.findViewById(R.id.browser_widget10);
        wiget3.setOnClickListener(this);
        wiget4=bottombar2.findViewById(R.id.browser_widget11);
        wiget4.setOnClickListener(this);
        wiget5 = bottombar2.findViewById(R.id.browser_widget12);
        wiget5.setOnClickListener(this);
        wiget5.setOnLongClickListener(this);

		boolean isRTL=bottombar2.getResources().getConfiguration().getLayoutDirection()==View.LAYOUT_DIRECTION_RTL;
		if(isRTL){
			bottombar2.removeView(wiget1);
			bottombar2.addView(wiget1, 1);
		}

		//long time = System.currentTimeMillis();
        favorList = new ArrayList<>();
        File def = new File(properties.opt_dir,"favorite_folders.txt");
        if(def.exists()) {
			FileInputStream inputstream;
			try {
	        	BufferedReader in = new BufferedReader(new InputStreamReader(inputstream=new FileInputStream(def), StandardCharsets.UTF_8));
		        String line;
		        int targetSize=12+2;
		        byte[] headerbuffer = new byte[targetSize];//long int1 0d 0a
		        
	        	inputstream.mark(targetSize);
		        
	        	boolean success;
	        	int size = inputstream.read(headerbuffer);
	        	if(size!=targetSize) {
	        		success=false;
	        	}else {
	        		if(compareByteArrayIsPara(headerbuffer, targetSize-2, tailing)) {
	        			opt.FirstFlag = toLong(headerbuffer,0);
	        			BKMKOff = toInt(headerbuffer,8);
	        			success=true;
	        		}else
	        			success=false;
	        	}
	        	
	        	if(!success)
	        		inputstream.reset();
		        if(success) {
		        	//CMNF.Log("配置加载成功",def);
		        }
		        while((line = in.readLine())!=null) {
		        	if(checker.contains(line))
		            	isDirty=true;
		            else{
		            	checker.add(line);
		            	favorList.add(line);
		            }
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        FirstFlagStamp = opt.FirstFlag;
        if(favorList.size()==0) {// true||
        	List<String> toAdd = Arrays.asList(getContext().getResources().getStringArray(R.array.internal_favor_dirs));
            favorList.addAll(0,toAdd);
            checker.addAll(toAdd);
        }
    	//CMNF.Log("配置加载总时间："+(System.currentTimeMillis()-time));

		bmlv.setLayoutManager(layoutManager=new LinearLayoutManager(getContext()));
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mShake = AnimationUtils.loadAnimation(getContext(), R.anim.anim_decoration);
        mShake.setRepeatCount(-1);
        
        bmlv.setLayoutManager(layoutManager);
        
        bmlv.setAdapter(mBMAdapter = new RecyclerView.Adapter() {
			@Override
			public int getItemCount() {
				return favorList.size();
			}

			@Override
			public void onBindViewHolder(RecyclerView.ViewHolder vh, int pos) {
				String PathName =  favorList.get(pos);
				SimpleViewHolder svh =((SimpleViewHolder)vh);
				svh.tv.setText(new File(PathName).getName());
				svh.itemView.setTag(PathName);
				if(bIsDeletingFavor) {
				    Animation mShake = AnimationUtils.loadAnimation(getContext(), R.anim.anim_decoration);
				    mShake.setRepeatCount(-1);
                    svh.tv.startAnimation(mShake);
					svh.tv.setTag(false);
					svh.dv.setVisibility(View.VISIBLE);
				}else {
					vh.itemView.clearAnimation();
					svh.tv.setTag(null);
					svh.dv.setVisibility(View.GONE);
				}
			}

			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup v, int arg1) {
				return new SimpleViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.favor_list_item, v, false));
			}});
        
        new_folder = root.findViewById(R.id.new_folder);
        inter_sel = root.findViewById(R.id.inter_sel);
        toggle_all = root.findViewById(R.id.toggle_all);
        toggle_all.bgInner = 0xFFFDFDFE;
        star = root.findViewById(R.id.star);
        select = root.findViewById(R.id.select);
        favorite = root.findViewById(R.id.favorite);

        listView.setOnItemLongClickListener((parent, view, i, id) -> {
			if(!mFileListAdapter.bIsSelecting && MarkedItemList.getFileCount()>0){
				mFileListAdapter.bIsSelecting=true;
				mFileListAdapter.notifyDataSetChanged();
				decorate_bottom_bar();
			}else {
				MaterialCheckbox cb = view.findViewById(R.id.file_mark);
				if (!cb.isChecked())
					cb.performClick();
			}
            return true;
        });

        new_folder.setOnClickListener(this);
        inter_sel.setOnClickListener(this);
        root.findViewById(R.id.etSearch).setOnClickListener(this);
        //root.findViewById(R.id.etSearch).setVisibility(View.GONE);
		if(properties.selection_mode== DialogConfigs.SINGLE_MODE){
			toggle_all.setVisibility(View.GONE);
			inter_sel.setVisibility(View.GONE);
		}
        toggle_all.setInflated(true);
        toggle_all.setOnCheckedChangedListener((checkbox, isChecked, isRisingEdge) -> {
            int old_count = MarkedItemList.getFileCount();
            boolean isAllExpected=false;
            if(checkbox.isChecked()) {//全选
                MarkedItemList.addALLFile(internalList);
                isAllExpected=true;
            }else {//清空当前文件夹下内容
                MarkedItemList.removeAll(internalList);
            }
            int new_count = MarkedItemList.getFileCount();
            if(old_count==new_count){
                if(!isAllExpected) {
                    if (new_count == 0)
                        toggle_all.setChecked(false);
                    else
                        toggle_all.setHalfChecked();
                }
            }else {
				mFileListAdapter.bIsSelecting = new_count != 0;
                mFileListAdapter.notifyDataSetChanged();
                invalidateSelectBtn(MarkedItemList.getFileCount());
                decorate_bottom_bar();
            }
        });


        folderCover = root.findViewById(R.id.toolbar_action1);
        folderCover.setOnClickListener(this);
        folderCover.setOnLongClickListener(this);
        HeaderView = findViewById(R.id.header);

        if(properties.selection_mode!= DialogConfigs.SINGLE_MODE)
        	toggle_all.setVisibility(View.VISIBLE);

		mFileListAdapter = new FilePickerAdapter(internalList, getContext(), properties, ph, opt);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mFileListAdapter.colorAccent = colorAccent = getContext().getResources().getColor(R.color.colorAccent, getContext().getTheme());
			mFileListAdapter.colorPrimary = getContext().getResources().getColor(R.color.colorPrimary, getContext().getTheme());
		} else {
			mFileListAdapter.colorAccent = colorAccent = getContext().getResources().getColor(R.color.colorAccent);
			mFileListAdapter.colorPrimary = getContext().getResources().getColor(R.color.colorPrimary);
		}

		invalidateSelectBtnColor(MarkedItemList.getFileCount());

        title = root.findViewById(R.id.title);
        title.setOnClickListener(this);
        dir_path = root.findViewById(R.id.dir_path);
        dir_path.setMovementMethod(LinkMovementMethod.getInstance());
        Button abort = root.findViewById(R.id.abort);
        if (negativeBtnNameStr != null) {
			abort.setText(negativeBtnNameStr);
        }
        select.setOnClickListener(this);
        favorite.setOnClickListener(this);
		abort.setOnClickListener(this);

        mFileListAdapter.setNotifyItemCheckedListener((item, isChecked) -> {
            if(!item.isDirectory() && !isChecked)
                profaneSelction();

            decorate_bottom_bar();

            invalidateSelectBtn(MarkedItemList.getFileCount());

			if(properties.selection_mode == DialogConfigs.MULTI_MODE || !properties.locked) {//多选模式，或者解锁
			} else {
				for (int i = 0; i < listView.getChildCount(); i++) {
					View cv = listView.getChildAt(i);
					if(cv!=null && cv.getTag() instanceof FilePickerAdapter.ViewHolder){
						FilePickerAdapter.ViewHolder holder = (FilePickerAdapter.ViewHolder) cv.getTag();
						MaterialCheckbox mark = holder.fmark;
						if (mark.getVisibility() == View.VISIBLE){
							if(mark.isChecked() && mark.getTag() instanceof Integer){
        						int position = (int) mark.getTag();
								if(!MarkedItemList.hasItem(internalList.get(position).location))
									mark.setChecked(false);
							}
						}
					}
				}
			}
        });

        listView.setAdapter(mFileListAdapter);
        if(properties.title_id!=0)
        	title.setText(properties.title_id);
        else
        	title.setText(R.string.appname);

        bmlv.setVisibility(View.GONE);
        dir_path.post(new Runnable() {
			@Override
			public void run() {
				adapt_bkmk_size();
				//layoutManager.scrollHorizontallyBy(BKMKOff, bmlv, new State());
				//bmlv.scrollTo(BKMKOff, 0);
				//layoutManager.scrollHorizontallyBy(BKMKOff, mBMAdapter, new State());
				//layoutManager.offsetChildrenHorizontal(BKMKOff);
				//bmlv.smoothScrollToPosition(BKMKOff);
				layoutManager.scrollToPositionWithOffset(BKMKOff, 10);
				//layoutManager.scrollToPosition(BKMKOff);
				//bmlv.scrollToPosition(BKMKOff);

				//CMN.Log("scroll loaded "+BKMKOff);
		}});
        bmlv.setScrollViewListener((scrollView, x, y, oldx, oldy) -> {
			//CMN.Log("onScrollChange "+scrollX);
			//BKMKOff = scrollX;
			if(bmlv.getTag()!=null)
				isDirty=true;
			bmlv.setTag(false);
		});
		if(opt.getBkmkShown())
			bmlv.setVisibility(View.VISIBLE);
		else
			bmlv.setVisibility(View.GONE);
        if(opt.getBottombarShown()) {
            bottombar2.setVisibility(View.VISIBLE);
        }else
            bottombar2.setVisibility(View.GONE);
        decorate_bottom_bar();
	}


	protected void decorate_bottom_bar(){
        int padding8 = (int) (getContext().getResources().getDisplayMetrics().density*8);
        int padding6 = (int) (getContext().getResources().getDisplayMetrics().density*6);
        if(mFileListAdapter.bIsSelecting){
            if(bottombar.getTag()==null){
                wiget1.setImageResource(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                wiget2.setImageResource(R.drawable.abc_ic_menu_cut_mtrl_alpha);
                wiget1.setPadding(padding8,padding8,padding8,padding8);
                wiget2.setPadding(padding8,padding8,padding8,padding8);
                wiget3.setPadding(padding6,padding6,padding6,padding6);
                wiget3.setImageResource(R.drawable.ic_delete);wiget3.getDrawable().mutate();wiget3.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                wiget4.setImageResource(R.drawable.ic_tcursor);
                bottombar.setTag(false);
            }
        }else{//此为钦定先天态。
            if(bottombar.getTag()!=null){
                wiget1.setImageResource(R.drawable.chevron_left);
                wiget2.setImageResource(R.drawable.chevron_right);
                wiget1.setPadding(0,0,0,0);
                wiget2.setPadding(0,0,0,0);
                wiget3.setPadding(padding8,padding8,padding8,padding8);
                wiget3.setImageResource(R.drawable.ic_sync_black_24dp);
                wiget4.setImageResource(R.drawable.ic_view_comfy_black_24dp);
                bottombar.setTag(null);
            }
        }
    }



    @Override
    protected void onStart() {
        //CMNF.Log("onStart");
        super.onStart();
        positiveBtnNameStr = getContext().getResources().getString(R.string.fp_select);
        if (FU.checkStorageAccessPermissions(getContext())) {
            File currLoc;
            internalList.clear();
            if (properties.offset.getPath().equals("/ASSET") || properties.offset.isDirectory() && validateOffsetPath()) {
                currLoc = new File(properties.offset.getAbsolutePath());
            } else {
                currLoc = new File(properties.error_dir.getAbsolutePath());
            }


            if(currLoc.getParentFile()!=null) {
                FileListItem parent = new FileListItem(currLoc.getParentFile().getAbsolutePath());
                parent.setFilename(getContext().getString(R.string.label_parent_dir));
                parent.setDirectory(true);
                parent.setTime(currLoc.lastModified());
                internalList.add(parent);
            }

            click_dir_at(currLoc);

            if(properties.dedicatedTarget!=null){
                for (int i = 0; i < internalList.size(); i++) {
                    if(internalList.get(i).filename.equals(properties.dedicatedTarget)){
                        listView.setSelectionFromTop(i,0);
                        break;
                    }
                }
            }

            mFileListAdapter.notifyDataSetChanged();
            listView.setOnItemClickListener(this);
        }

        if(opt.getSlideShowMode()) EnterSlideShowMode(100);

		Window win = getWindow();
		if(win==null) return;
		win.setWindowAnimations(R.style.fp_dialog_animation);
    }

	@Override
	public void OnWindowChange(DisplayMetrics dm) {
		Window win = getWindow();
		if(win!=null)
			win.setLayout((int) (dm.widthPixels-2*18*dm.density), -1);
		if(bmlv.getVisibility()==View.VISIBLE) {
			adapt_bkmk_size();
		}
	}

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();

    }

    @Override
    public void onDetachedFromWindow() {
        //CMN.Log("onDetachedFromWindow");
        super.onDetachedFromWindow();
    }


    @Override
    public void dismiss() {
        //here
        MarkedItemList.clearSelectionList();
        internalList.clear();
        if(isDirty || FirstFlagStamp!=opt.FirstFlag)
            dumpSettings();
        CMNF.AssetMap=null;
        filter=null;
        properties=null;
        if(callbacks!=null)callbacks.onExitSlideShow();
        if(callbacks!=null)
        	callbacks.onDismiss();
        super.dismiss();
    }

    private void dumpSettings() {
        try {
            File def = new File(properties.opt_dir,"favorite_folders.txt");
            if(!def.exists())
                def.createNewFile();
            if(def.exists()) {
                FileOutputStream fo;
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(fo=new FileOutputStream(def), StandardCharsets.UTF_8));

                int targetSize=12;
                byte[] headerbuffer = new byte[targetSize];//int1 int2 int2 0d 0a
                for(int i=0;i<headerbuffer.length;i++) {
                    headerbuffer[i]=0;
                }
                headerbuffer[7] = (byte) (opt.FirstFlag & 0xff);
                headerbuffer[6] = (byte) (opt.FirstFlag >> 8 & 0xff);
                headerbuffer[5] = (byte) (opt.FirstFlag >> 16 & 0xff);
                headerbuffer[4] = (byte) (opt.FirstFlag >> 24 & 0xff);
                BKMKOff =  layoutManager.findFirstCompletelyVisibleItemPosition();
                if(BKMKOff<0)
                    BKMKOff=layoutManager.findFirstVisibleItemPosition();
                //CMN.Log("scroll saved "+BKMKOff);
                headerbuffer[11] = (byte) (BKMKOff & 0xff);
                headerbuffer[10] = (byte) (BKMKOff >> 8 & 0xff);
                headerbuffer[9] = (byte) (BKMKOff >> 16 & 0xff);
                headerbuffer[8] = (byte) (BKMKOff >> 24 & 0xff);

                fo.write(headerbuffer);
                fo.write(tailing);

                for(String PI:favorList) {
                    output.write(PI);
                    output.write("\n");
                }
                output.close();
            }
            FirstFlagStamp=opt.FirstFlag;
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(bIsDeletingFavor){
                bIsDeletingFavor=false;
                mBMAdapter.notifyDataSetChanged();
                return true;
            }else if(MarkedItemList.getFileCount()!=0 || mFileListAdapter.bIsSelecting){
            	if(MarkedItemList.getFileCount()>0){
                	MarkedItemList.clearSelectionList();
                }
            	if(mFileListAdapter.bIsSelecting){
					mFileListAdapter.bIsSelecting = false;
					mFileListAdapter.notifyDataSetChanged();
					decorate_bottom_bar();
				}
                toggle_all.setChecked(false);
                invalidateSelectBtn(0);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean validateOffsetPath() {
        String offset_path = properties.offset.getAbsolutePath();
        String root_path = properties.root.getAbsolutePath();
        return !offset_path.equals(root_path) && offset_path.contains(root_path);
    }

    private void ChangeToDir(File currLoc) {
        if(currLoc==null && currLocation==null){
            return;
        }
        currLoc = currLoc==null?currLocation:currLoc;
        if(!currLoc.equals(currLocation)){
            mFileListAdapter.lastCheckedPos[0]=mFileListAdapter.lastCheckedPos[1]=-1;
        }
        currLocation = currLoc;
        if(currLocation==null) return;
        internalList.clear();//清空
        profaneSelction();

        //if (!currLoc.getName().equals(properties.root.getName()) && currLoc.getParentFile()!=null) {//扫入
        if (currLocation.getParentFile()!=null) {//扫入
            FileListItem parent = new FileListItem(currLocation.getParentFile().getAbsolutePath());
            parent.setFilename(getContext().getString(R.string.label_parent_dir));
            parent.setDirectory(3);
            parent.setTime(-1);
            internalList.add(parent);
        }else if("/".equals(currLocation.getAbsolutePath())) {
        	FileListItem parent = new FileListItem();
            parent.setFilename(getContext().getString(R.string.label_parent_dir));
            parent.setDirectory(3);
            parent.setTime(-2);
            internalList.add(parent);
        }
        //long stst = System.currentTimeMillis();
        FU.prepareFileListEntries(getContext(), internalList, currLocation, filter);
        //CMNF.Log("读取文件列表时间", System.currentTimeMillis()-stst);
        mFileListAdapter.notifyDataSetChanged();
        if(checker.contains(currLocation.getAbsolutePath()))
            star.setVisibility(View.VISIBLE);
        else
            star.setVisibility(View.INVISIBLE);
	}

	//地址栏
	private void setDirText(String newPath) {
		if(currentStage>0) {
			MemoClickableSpan last = MCSPool.get(currentStage);
			if(last!=null) {
				last.position = listView.getFirstVisiblePosition();
				if(listView.getChildCount()>0) last.offset = listView.getChildAt(0).getTop();
				//CMN.Log("list position saved "+last.position +" for " + currentStage);
			}
		}

		boolean doAppending=false;
		int now = 0;
		int id=-1;
		if(spanned.length()<newPath.length()) {
			if(FU.startsWith(newPath, spanned, 0)) {//追加
				//spanned.delete(truncator, spanned.length());
				//dir_path.setText(spanned);
				doAppending=true;
				if(spanned.length()>1)
					now = spanned.length();// jump ahead and that's where the seeking will start
				if(currentStage>0) id = currentStage-1;
				spanned.append(newPath,spanned.length(),newPath.length());
				//sn("add ");
			}
		}else if(spanned.length()>newPath.length()){
			if(FU.startsWith(spanned, newPath, 0)) {//后退， 相当于点击
				try {
					MemoClickableSpan[] lastspan = spanned.getSpans(newPath.length()-1, newPath.length(), MemoClickableSpan.class);
					//CMN.Log("MemoClickableSpan"+lastspan);
					//spanned.delete(newPath.length(), spanned.length());
					//dir_path.setText(spanned);
					//sn("deleted"+lastspan);
					lastspan[0].onClick(dir_path);
					return;
				} catch (Exception ignored) {}
			}
		}else if(FU.startsWith(spanned, newPath, 0)) {
			return;
		}

		if(!doAppending) {
			spanned.clear();
			spanned.append(newPath);
		}

		int lastStart=-1;
		boolean proceed=true;
		MemoClickableSpan span = null;
		boolean bNoMoreCompare = false;
		while(proceed) {
			id++;
			if((now = newPath.indexOf("/", lastStart==-1?now:now+1))!=-1) {}
			else {
				proceed=false;
				if(lastStart!=-1)
					now = newPath.length();
				else
					break;
			}
			if(lastStart >= now) break;
			if(lastStart!=-1) {
				span = obtainMCS(now, id);
				CharSequence nowToken = spanned.subSequence(lastStart+1, now);
				if(bNoMoreCompare || !nowToken.equals(span.token)) {
					span.token = nowToken;
					span.position=0;
					span.offset=0;
					bNoMoreCompare=true;
				}else {//若合符
				}

				spanned.setSpan(span, lastStart+1, now, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			lastStart=now;
		}
		currentStage=id;

		dir_path.setText(spanned);

		if(span!=null) {
			listView.setSelectionFromTop(span.position, span.offset);
		}

	}

	public MemoClickableSpan obtainMCS(int truncator,int stage) {
		MemoClickableSpan ret = MCSPool.get(stage);
		if(ret==null) {
			MCSPool.put(stage, ret = new MemoClickableSpan(stage));
		}
		ret.truncator=truncator;
		return ret;
	}

	public class MemoClickableSpan extends ClickableSpan{
		private int truncator;
		private CharSequence token;
		private int position;
		public int offset;
		private final int stage;

		MemoClickableSpan(int st){
			stage = st;
		}

		@Override
		public void onClick(View widget) {
			if(currentStage!=-1) {//save last postion!
				MemoClickableSpan last = MCSPool.get(currentStage);
				if(last!=null) {
					last.position = listView.getFirstVisiblePosition();
					if(listView.getChildCount()>0) last.offset = listView.getChildAt(0).getTop();
				}
			}
			currentStage=stage;
			//sn(((TextView) widget).getText().toString().substring(0,truncator));
			if(truncator<spanned.length()) {
				spanned.delete(truncator, spanned.length());
				dir_path.setText(spanned);
				//if(dir_path.canScrollVertically(-1)) dir_path.post(LineScroller);
				ChangeToDir(new File(spanned.toString()));
			}
			listView.setSelectionFromTop(position, offset);
			//sn(position+"");
		}

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.WHITE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(d==null || !d.isShowing())
        if (internalList.size() > position) {
            FileListItem fItem = internalList.get(position);
            if (fItem.isDirectory()) {
                if(position==0 && fItem.getTime()==-2)
                    return;
                Drawable selector = ((AbsListView) adapterView).getSelector();
                if(selector!=null) selector.jumpToCurrentState();
                click_dir_at(fItem);
            } else {
                if(mFileListAdapter.bIsSelecting) {
                    MaterialCheckbox fmark = view.findViewById(R.id.file_mark);
                    fmark.performClick();
                    profaneSelction();
                }else{
                    if(!opt.getSlideShowMode()){
						if(!properties.locked) {
							snack_lock();
							return;
						}
                        if (callbacks != null) {
                            callbacks.onSelectedFilePaths(new String[] {fItem.getLocation()}, currLocation.getAbsolutePath());
							callbacks=null;
                        }
                        dismiss();
                    }else{//点击进入幻灯片
                        int cc = 0;
                        for (; cc < internalList.size(); cc++) {
                            if(!internalList.get(cc).isDirectory()) break;
                        }
                        if(CMNF.UniversalHashMap==null) CMNF.UniversalHashMap=new HashMap<>(2);
                        ViewPagerLaunchTally="vp:"+System.currentTimeMillis();
                        CMNF.UniversalHashMap.put(ViewPagerLaunchTally,position-cc);
                        CMNF.UniversalObject = internalList.subList(cc, internalList.size());
                        Intent it = new Intent();
                        it.setClass(getContext(), SlideShowActivity.class);
                        it.putExtra(SlideShowActivity.TAYYL,ViewPagerLaunchTally);
                        getContext().startActivity(it);
                    }
                }
            }
        }
    }

    String ViewPagerLaunchTally = null;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(CMNF.UniversalHashMap!=null && CMNF.UniversalHashMap.size()>0){
                if(ViewPagerLaunchTally!=null && internalList!=null){//check slide show progress!;
                    Object val = CMNF.UniversalHashMap.remove(ViewPagerLaunchTally);
                    ViewPagerLaunchTally=null;
                    CMN.Log("ViewPagerLaunchTally",val);
                    if(val instanceof String){
                        int targetPos = -1;
                        for (int i = 0; i < internalList.size(); i++) {
                            if(internalList.get(i).getLocation().equals(val)){if(listView!=null)targetPos=i;break;}
                        }
                        if(targetPos!=-1){
                            int fvp = listView.getFirstVisiblePosition();
                            if(targetPos<fvp || targetPos>listView.getLastVisiblePosition()){
                                listView.setSelection(targetPos);
                            }
                        }
                    }
                }
            }
        }
    }

    private void click_dir_at(FileListItem fItem) {
        if(fItem==null)
            return;
        if (!fItem.isDirectory())
            return;
        if(fItem.getLocation()==null)
            return;

        File currLoc = new File(fItem.getLocation());

        click_dir_at(currLoc);
    }

    private void click_dir_at(File currLoc) {
        internalList.clear();//清空
        profaneSelction();

        ChangeToDir(currLoc);

        setDirText(currLoc.getAbsolutePath());
    }

    public void setProperties(DialogProperties properties) {
        this.properties = properties;
        //if(properties.opt_dir==null)
		//	properties.opt_dir=getContext().getExternalFilesDir("favorite_dirs");
        filter = new ExtensionFilter(properties);
    }

    public DialogProperties getProperties() {return properties;}

    public void setDialogSelectionListener(DialogSelectionListener callbacks) {
        this.callbacks = callbacks;
    }

    public void setPositiveBtnName(CharSequence positiveBtnNameStr) {
        if (positiveBtnNameStr != null) {
            this.positiveBtnNameStr = positiveBtnNameStr.toString();
        } else {
            this.positiveBtnNameStr = null;
        }
    }

    public void setNegativeBtnName(CharSequence negativeBtnNameStr) {
        if (negativeBtnNameStr != null) {
            this.negativeBtnNameStr = negativeBtnNameStr.toString();
        } else {
            this.negativeBtnNameStr = null;
        }
    }

    public void markFiles(List<String> paths) {
        if (paths != null && paths.size() > 0) {
            if (properties.selection_mode == DialogConfigs.SINGLE_MODE) {
                File temp = new File(paths.get(0));
                switch (properties.selection_type) {
                    case DialogConfigs.DIR_SELECT:
                        if (temp.exists() && temp.isDirectory()) {
                            FileListItem item = new FileListItem(temp.getAbsolutePath());
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;

                    case DialogConfigs.FILE_SELECT:
                        if (temp.exists() && temp.isFile()) {
                            FileListItem item = new FileListItem(temp.getAbsolutePath());
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;

                    case DialogConfigs.FILE_AND_DIR_SELECT:
                        if (temp.exists()) {
                            FileListItem item = new FileListItem(temp.getAbsolutePath());
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;
                }
            } else {
                for (String path : paths) {
                    switch (properties.selection_type) {
                        case DialogConfigs.DIR_SELECT:
                            File temp = new File(path);
                            if (temp.exists() && temp.isDirectory()) {
                                FileListItem item = new FileListItem(temp.getAbsolutePath());
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;

                        case DialogConfigs.FILE_SELECT:
                            temp = new File(path);
                            if (temp.exists() && temp.isFile()) {
                                FileListItem item = new FileListItem(temp.getAbsolutePath());
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;

                        case DialogConfigs.FILE_AND_DIR_SELECT:
                            temp = new File(path);
                            if (temp.exists() && (temp.isFile() || temp.isDirectory())) {
                                FileListItem item = new FileListItem(temp.getAbsolutePath());
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;
                    }
                }
            }
        }
    }

	@Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        if(id  == R.id.lvitems) {
            bIsDeletingFavor=true;
            mBMAdapter.notifyDataSetChanged();
            return true;

        }else if(id  == R.id.toolbar_action1) {
            boolean val;
            if(bottombar2.getVisibility()!=View.VISIBLE) {
                bottombar2.setVisibility(View.VISIBLE);
                val=true;
            }else {
                bottombar2.setVisibility(View.GONE);
                val=false;
            }
            opt.setBottombarShown(val);
            return true;
        }
        else if(id== R.id.browser_widget12){
            int fvp = listView.getFirstVisiblePosition();
            if(fvp>=0) listView.smoothScrollToPositionFromTop(fvp, listView.getChildAt(0).getTop());
            for (int i = 0; i < internalList.size(); i++) {
                if(!internalList.get(i).isDirectory()){
                    View cv = listView.getChildAt(0);
                    listView.setSelectionFromTop(i, (int) (cv==null?0:cv.getHeight()*5.4f/6));
                    break;
                }
            }
            return true;
        }
        return false;
    }

	CompoundButton.OnCheckedChangeListener checkclicker = (compoundButton, b) -> {
		int id1 = compoundButton.getId();
		if(id1 == R.id.enable_list){
			if(true)if(b==false){Toast.makeText(getContext(),"暂不支持网格显示",Toast.LENGTH_SHORT).show();ckList.setChecked(true);return;}
			opt.setEnableList(b);
			np1.setOnValueChangedListener(null);
			TextView vm1 = ComfyView.findViewById(R.id.viewmode1);
			TextView vm2 = ComfyView.findViewById(R.id.viewmode2);
			TextView vm3 = ComfyView.findViewById(R.id.viewmode3);
			if(b) {
				//ComfyView.findViewById(R.id.numberpickerp).setVisibility(View.GONE);
				vm1.setText(R.string.small);
				vm2.setText(R.string.medium);
				vm3.setText(R.string.large);
				((TextView)ComfyView.findViewById(R.id.numberpickerh)).setText("列表图屏占比(__/16): ");
				np1.setMinValue(1);
				np1.setMaxValue(16);
				np1.setValue(opt.getListIconSize());
				np1.setOnValueChangedListener((numberPicker, i, i1) -> {
					opt.setListIconSize(i1);
				});
			}else {
				//ComfyView.findViewById(R.id.numberpickerp).setVisibility(View.VISIBLE);
				vm1.setText(R.string._1column);
				vm2.setText(R.string._2column);
				vm3.setText(R.string._3column);
				((TextView)ComfyView.findViewById(R.id.numberpickerh)).setText(R.string.custom_columns_number);
				np1.setMinValue(1);
				np1.setMaxValue(64);
				np1.setValue(opt.getGridSize());
				np1.setOnValueChangedListener((numberPicker, i, i1) -> {
					opt.setGridSize(i1);
				});
			}
		}
		else if(id1== R.id.enable_thumbs){
			opt.setEnableTumbnails(b);
		}
		else if(id1== R.id.crop_thumbs){
			mFileListAdapter.myreqL2.setCrop(b);
			opt.setCropTumbnails(b);
		}
		else if(id1== R.id.auto_height){
			opt.setAutoThumbsHeight(b);
		}
	};

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.browser_widget7){//复制::后退
			Dialog d=new androidx.appcompat.app.AlertDialog.Builder(getContext())
					.setMessage(getContext().getResources().getString(R.string.fp_warn_copy, MarkedItemList.getFileCount(),dir_path.getText()))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.yes, new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Set<Map.Entry<String, FileListItem>> x = MarkedItemList.entrySet();
							Iterator<Map.Entry<String, FileListItem>> it = x.iterator();
							ArrayList<String> l = new ArrayList<>(MarkedItemList.getFileCount());
							while (it.hasNext()) {
								Map.Entry<String, FileListItem> entry = it.next();
								FileListItem val = entry.getValue();
								File from = new File(val.getLocation());
								File to = new File(dir_path.getText().toString(),from.getName());
								CMNF.Log("XXX-taskLog",from.getAbsolutePath()+" -> "+to.getAbsolutePath());
								if(from.equals(to)) l.add(val.getLocation());
								if(FU.exsists(getContext(), to)) {
									sn("目标存在，跳过");
									CMNF.Log("XXX-exsists",to.length()+":"+to.getAbsolutePath()+"from: "+val.getLocation());
									continue;
								}
								int cc=0;
								if (try_copy_file(from,to)) {
									l.add(val.getLocation());
									val.setLocation(to.getAbsolutePath());
									mFileListAdapter.add(val);
									listView.setSelection(mFileListAdapter.getCount()-1);
									//Toast.makeText(getContext(), val.getLocation()+" -> "+to.getAbsolutePath(), Toast.LENGTH_SHORT).show();
									cc++;
								}
							}
							MarkedItemList.removeAllByLoc(l);
							int count = MarkedItemList.getFileCount();
							invalidateSelectBtn(count);
							if(count!=0)
								toggle_all.setHalfChecked();
							else {
								toggle_all.setChecked(false);
								mFileListAdapter.bIsSelecting=false;
								mFileListAdapter.notifyDataSetChanged();
								decorate_bottom_bar();
							}
						}
					}).show();
			stylize_simple_message_dialog(d, getContext());
		}
		else if (id== R.id.browser_widget8){// TODO 移动::前进
			Dialog d=new androidx.appcompat.app.AlertDialog.Builder(getContext())
					.setMessage(getContext().getResources().getString(R.string.fp_warn_move, MarkedItemList.getFileCount(),"dname.getText()"))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.yes, new OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Set<Map.Entry<String, FileListItem>> x = MarkedItemList.entrySet();
							Iterator<Map.Entry<String, FileListItem>> it = x.iterator();
							ArrayList<String> l = new ArrayList<>();
							while (it.hasNext()) {
								Map.Entry<String, FileListItem> entry = it.next();
								FileListItem val = entry.getValue();
								File from = new File(val.getLocation());
								File to = new File(dir_path.getText().toString(),from.getName());
								CMNF.Log("XXX-taskLog",from.getAbsolutePath()+" -> "+to.getAbsolutePath());
								if(from.equals(to)) l.add(val.getLocation());
								if(FU.exsists(getContext(), to)) {
									sn("目标存在，跳过");
									CMNF.Log("XXX-exsists",to.length()+":"+to.getAbsolutePath()+"from: "+val.getLocation());
									continue;
								}
								long stst = System.currentTimeMillis();
								int ret= FU.move3(getContext(), from, to);
								//CMNF.Log("移动时间", System.currentTimeMillis()-stst);
								if (ret==0||to.exists()&&!from.exists()) {
									l.add(val.getLocation());
									val.setLocation(to.getAbsolutePath());
									mFileListAdapter.add(val);
									listView.setSelection(mFileListAdapter.getCount()-1);
									//Toast.makeText(getContext(), val.getLocation(), Toast.LENGTH_SHORT).show();
								}else {
									if(ret==-1) AskPermissionSnack(null);
									else sn("发生未知错误5:"+ret);
								}
							}
							MarkedItemList.removeAllByLoc(l);
							int count = MarkedItemList.getFileCount();
							invalidateSelectBtn(count);
							if(count!=0)
								toggle_all.setHalfChecked();
							else {
								toggle_all.setChecked(false);
								if(mFileListAdapter.bIsSelecting){
									mFileListAdapter.bIsSelecting=false;
									mFileListAdapter.notifyDataSetChanged();
								}
								decorate_bottom_bar();
							}
						}
					}).show();
			stylize_simple_message_dialog(d, getContext());
		}
		else if (id == R.id.browser_widget10) {//删除::刷新
            if(mFileListAdapter.bIsSelecting) {
                Dialog d = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setMessage(getContext().getResources().getString(R.string.fp_warn_delete, MarkedItemList.getFileCount()))
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
							Set<Map.Entry<String, FileListItem>> x = MarkedItemList.entrySet();
							Iterator<Map.Entry<String, FileListItem>> it = x.iterator();
							ArrayList<FileListItem> l = new ArrayList<>(MarkedItemList.getFileCount());
							while (it.hasNext()) {
								Map.Entry<String, FileListItem> entry = it.next();
								FileListItem val = entry.getValue();
								File toDel = new File(val.getLocation());
								int ret = FU.delete3(getContext(), toDel);
								if (ret == 0 || !toDel.exists()) {
									mFileListAdapter.remove(val);
									l.add(val);
								} else {
									if (ret == -1) AskPermissionSnack(null);
									else sn("发生未知错误3:" + ret);
								}
							}
							MarkedItemList.removeAll(l);
							if(MarkedItemList.getFileCount()==0){
								mFileListAdapter.bIsSelecting=false;
								mFileListAdapter.notifyDataSetChanged();
								toggle_all.setChecked(false);
								decorate_bottom_bar();
							}else
								toggle_all.setHalfChecked();
							invalidateSelectBtn(MarkedItemList.getFileCount());
						}).show();
				stylize_simple_message_dialog(d, getContext());
            }else{
                ChangeToDir(currLocation);
            }
        }
        else if(id== R.id.browser_widget11){//重命名::视图排序
			if(mFileListAdapter.bIsSelecting) {
				if(MarkedItemList.getFileCount()>0){
					Set<Map.Entry<String, FileListItem>> x = MarkedItemList.entrySet();
					Iterator<Map.Entry<String, FileListItem>> it = x.iterator();
					File file_to_rename=new File(it.next().getKey());
					final ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_edittext, null);
					final EditText etNew = dv.findViewById(R.id.edt_input);
					final View btn_Done = dv.findViewById(R.id.done);
					dv.findViewById(R.id.toolbar_action1).setVisibility(View.GONE);

					etNew.setText(file_to_rename.getName());
					etNew.setSingleLine(false); etNew.setGravity(GravityCompat.START);
					etNew.setPadding(0,10,0,10);

					final Dialog dd = new GoodKeyboardDialog(getContext());
					dd.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dd.setContentView(dv);
					btn_Done.setOnClickListener(v1 -> {
						if(etNew.getText().length()==0) {
							Toast.makeText(getContext(), "长度不能为零", Toast.LENGTH_SHORT).show();
							return;
						}
						String currentDirName = dir_path.getText().toString();
						File new_Folder = new File(currentDirName+"/"+etNew.getText().toString().replace("/",""));
						if(new_Folder.exists()) {
							Toast.makeText(getContext(), "已存在", Toast.LENGTH_SHORT).show();
							int id13 = internalList.indexOf(new FileListItem(new_Folder,false));
							if(id13 !=-1)
								listView.setSelection(id13);
							return;
						}
						int ret= FU.checkSdcardPermission(getContext(), new_Folder);
						if(ret!=0) {
							if(ret==-1) AskPermissionSnack(etNew);
							else Toast.makeText(getContext(), "未知错误1:"+ret, Toast.LENGTH_LONG).show();
							return;
						}
						ret= FU.rename5(getContext(),file_to_rename ,new_Folder);
						if(ret>=0) {
							FileListItem fviewitem = MarkedItemList.removeSelectedItem(file_to_rename.getPath());
							if(fviewitem!=null){
								fviewitem.setLocation(new_Folder.getAbsolutePath());
								fviewitem.setFilename(new_Folder.getName());
							}
							dd.dismiss();
						}else {
							if(ret==-1)
								Toast.makeText(getContext(), "请赋予sd卡读写权限。", Toast.LENGTH_LONG).show();
							else
								Toast.makeText(getContext(), "未知错误2:"+ret, Toast.LENGTH_LONG).show();
						}
						if(MarkedItemList.getFileCount()==0)
							exit_selection_due_to_zero();
					});
					etNew.setOnEditorActionListener((v16, actionId, event) -> {
						if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
							btn_Done.performClick();
							return true;
						}
						return false;
					});

					dd.getWindow().setGravity(Gravity.TOP);
					dd.getWindow().getAttributes().verticalMargin=0.01f;
					dd.getWindow().setAttributes(dd.getWindow().getAttributes());
					dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					dd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					dd.show();
					if(true){
						dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
						etNew.requestFocus();
					}
				}
			}
			else show_views_dialog();
        }
        else if(id== R.id.browser_widget12){//菜单::菜单
            ListPopupWindow popup1 = new ListPopupWindow(getContext());
            TextView textmp = new TextView(getContext());
            textmp.setTextSize(15.5f);
            TextPaint painter = textmp.getPaint();
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            final int pad = (int) (10*dm.density);
            final float fontHeight = painter.getFontMetrics().bottom - painter.getFontMetrics().top;
            MenuAdapter mada;
            popup1.setAdapter(mada=new MenuAdapter(mFileListAdapter, getContext()));
            popup1.setAnchorView(v);
            popup1.setModal(true);
            popup1.scrolltoend=true;
            popup1.cliptoscreen=false;
            popup1.setEnterTransition(null);
            popup1.setExitTransition(null);
            int maxchar=0;String LongestItem="";
            String[] curritems = mFileListAdapter.bIsSelecting?mada.menu_selecting:mada.menu_common;
            for(String sI:curritems){
                if(sI.length()>maxchar){maxchar=sI.length();LongestItem=sI;}
            }
            popup1.setWidth((int) (painter.measureText("  "+LongestItem)+fontHeight+(Build.VERSION.SDK_INT<=19?4:2)*pad));
            //popup1.mMarginTop = HeaderView.getHeight()+dir_path.getHeight()+pad;
            popup1.setVerticalOffset(-pad/2);
            popup1.setDropDownGravity(Gravity.RIGHT);
            popup1.setHorizontalOffset((int) (  -3*dm.density));
            if (menu_clicker == null) {
                menu_clicker= (parent, view, position, id12) -> {
                    int id_true = ((MenuAdapter) parent.getAdapter()).getId(position);
                    if(id_true== R.drawable.fp_lock){
						if(menupopup!=null)menupopup.dismiss();
                        properties.locked=!properties.locked;
                        if(properties.locked) {
                            sn(R.string.fp_lock);
                            lockit();//上锁复原
                        }else {//解锁
                            toggle_all.setVisibility(View.VISIBLE);
                            sn(R.string.fp_unlock);
                        }
                        ChangeToDir(currLocation);
                    }
					else if(id_true== R.drawable.ic_view_comfy_black_24dp){//视图
						if(menupopup!=null)menupopup.dismiss();
						show_views_dialog();
					}
					else if(id_true== R.drawable.tools_filepicker){//设置
						try {
							if(menupopup!=null)menupopup.dismiss();
							Intent intent = new Intent();
							intent.putExtra("realm", 3);
							intent.setClass(getContext(), Class.forName(CMNF.settings_class!=null? CMNF.settings_class:"com.knziha.settings.SettingsActivity"));
							getContext().startActivity(intent);
						} catch (Exception e) { Toast.makeText(getContext(), "Cannot find settings activity : "+e, Toast.LENGTH_SHORT).show();}
					}
                    else if(id_true== R.drawable.ic_stop_black_24dp){//中断选择
                        exit_selection_due_to_zero();
                    }
                    else if(id_true== R.drawable.ic_viewpager_carousel){//切换幻灯片模式
                        if(menupopup!=null)menupopup.dismiss();
                        if(opt.setSlideShowMode(!opt.getSlideShowMode())){
                            EnterSlideShowMode(0);
                        }else{
                            int ch = getContext().getResources().getColor(R.color.colorHeader);
                            FilePickerDialog.this.getWindow().setDimAmount(0.2f);
                            callbacks.onExitSlideShow();
                            HeaderView.setBackgroundColor(ch);
                            dir_path.setBackgroundColor  (ch);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                getView().getBackground().setTint(Color.WHITE);
                        }
                    }
                };
            }
            menupopup=popup1;
            popup1.setOnItemClickListener(menu_clicker);
            popup1.setOnDismissListener(() -> menupopup=null);
            popup1.show();
            //popup1.mDropDownList.setScrollbarFadingEnabled(false);
        }
        else if(id == R.id.toolbar_action1) {//folderCover
        	boolean val = opt.getBkmkShown();
            //sn(v);
        	if(val) {
        		bmlv.setVisibility(View.GONE);
        	}else{
        		adapt_bkmk_size();
        		bmlv.setVisibility(View.VISIBLE);
        	}
            opt.setBkmkShown(!val);
        	dumpSettings();
    		isDirty=false;
        	v.setTag(false);
        }
        else if(id == R.id.lvitems) {
			if(bIsDeletingFavor) {
				bIsDeletingFavor=false;
				mBMAdapter.notifyDataSetChanged();
			}else {
				String fn = (String) v.getTag();
				click_dir_at(new File(fn));
			}
        }
        else if(id == R.id.close) {
        	isDirty=true;
			String fn = (String) ((View) v.getParent()).getTag();
            int idx = favorList.indexOf(fn);
        	int lastSize=favorList.size();
        	if(idx!=-1) {
            	favorList.remove(fn);
            	checker.remove(fn);
                if(fn.equals(dir_path.getText().toString())) star.setVisibility(View.INVISIBLE);
                if(favorList.size()!=lastSize-1)
                	mBMAdapter.notifyDataSetChanged();
                else
                	mBMAdapter.notifyItemRemoved(idx);
        	}
        	if(favorList.size()==0)  bIsDeletingFavor=false;
		}
        else if(id == R.id.select) {
        	if(!properties.locked) {
        		snack_lock();
        		return;
        	}
        	if(callbacks==null)
        	    return;
        	ArrayListTree<String> paths;
            if(MarkedItemList.getFileCount()==0 && properties.selection_type!= DialogConfigs.FILE_SELECT){
                paths = new ArrayListTree<>();
                paths.add(String.valueOf(dir_path.getText()));
            }else
                paths = MarkedItemList.getSelectedPaths();
            //NullPointerException fixed in v1.0.2

            if(properties.selection_type== DialogConfigs.FILE_SELECT) {
            	for(String f:paths.getList())
            		if(new File(f).isDirectory()) {
            			final ArrayListTree<String> fs = paths;
            			final View dv = getLayoutInflater().inflate(R.layout.fp_dialog_progress,null);
            			final SeekBar sk = dv.findViewById(R.id.seekbar);
            			final TextView tv = dv.findViewById(R.id.label);
            			tv.setText(getContext().getResources().getString(R.string.depth,1));
            			final AlertDialog d = new Builder(getContext())
            					.setView(dv)
            					.setTitle(R.string.ask_scanner)
            					.create();
            			View.OnClickListener o_clicker = new View.OnClickListener(){
							@Override
							public void onClick(View v) {
								int id = v.getId();
								if(id== R.id.btn1) {
									d.cancel();
								}else if(id== R.id.btn2) {
									if (callbacks != null) {
					                    callbacks.onSelectedFilePaths(fs.getList().toArray(new String[] {}),currLocation.getAbsolutePath() );
										callbacks=null;
									}
									d.cancel();
									FilePickerDialog.this.dismiss();
								}else if(id== R.id.btn3) {
									int depth=sk.getProgress();
									ArrayList<String> scannerLes = new ArrayList<>();
									for(String f:fs.getList())
										if(new File(f).isDirectory()) {
											scannerLes.add(f);
										}
									scannedDirs.clear();
									for(String f:scannerLes)
										scanFiles(f,fs,depth);
									callbacks.onSelectedFilePaths(fs.getList().toArray(new String[] {}),currLocation.getAbsolutePath());
									callbacks=null;
									d.cancel();
									FilePickerDialog.this.dismiss();
								}
							}
							HashSet<File> scannedDirs = new HashSet<>();
							private void scanFiles(String f, ArrayListTree<String> fs, int depth) {
								File p=new File(f);
								if(!p.isDirectory())
									return;
								File[] list1=null;
								if(!scannedDirs.contains(p))
									list1 = p.listFiles(filter);
								scannedDirs.add(p);
								if(depth>0) {
									File[] list2 = p.listFiles(new FileFilter() {
										@Override
										public boolean accept(File pathname) {
											return pathname.isDirectory();
										}});
									for(File pI:list2)
										scanFiles(pI.getAbsolutePath(),fs,--depth);
								}
								if(list1!=null)
								for(File pI:list1)
									fs.insert(pI.getAbsolutePath());
							}};
            			dv.findViewById(R.id.btn1).setOnClickListener(o_clicker);
            			dv.findViewById(R.id.btn2).setOnClickListener(o_clicker);
            			dv.findViewById(R.id.btn3).setOnClickListener(o_clicker);
            			sk.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							    if(progress<1)
                                    sk.setProgress(1);
								if(fromUser)
									tv.setText(getContext().getResources().getString(R.string.depth,progress));
							}
							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {}});
        				d.show();
            			return;
            		}
            }
            if (callbacks != null) {
                callbacks.onSelectedFilePaths(paths.getList().toArray(new String[] {}),currLocation.getAbsolutePath());
				callbacks=null;
            }
            dismiss();
        }
        else if(id == R.id.favorite) {
            //String fn = properties.opt_dir.getAbsolutePath()+"/"+dir_path.getText().toString().replace("/","OVIHCS");
        	String fn = new File(dir_path.getText().toString()).getAbsolutePath();
            if(!checker.contains(fn)) {//添加收藏
            	isDirty=true;
            	favorList.add(fn);
            	star.setVisibility(View.VISIBLE);
            	checker.add(fn);
	            mBMAdapter.notifyItemInserted(favorList.size()-1);
        		bmlv.smoothScrollToPosition(favorList.size()-1);
            }else{//删除收藏
            	isDirty=true;
            	int idx = favorList.indexOf(fn);
            	int lastSize=favorList.size();
            	if(idx!=-1) {
            		//folderBookMarks.smoothScrollToPosition(id);
                	favorList.remove(fn);
                	checker.remove(fn);
                    star.setVisibility(View.INVISIBLE);
                    if(favorList.size()!=lastSize-1)
                    	mBMAdapter.notifyDataSetChanged();
                    else
                    	mBMAdapter.notifyItemRemoved(idx);
            	}
            	if(favorList.size()==0)  bIsDeletingFavor=false;
            }

        }
        else if(id == R.id.abort) {
        	cancel();
        }
        else if(id== R.id.title){
            cancel();
        }
        else if(id == R.id.inter_sel) {//间隔选择
            //CMNF.Log("间隔选择",mFileListAdapter.lastCheckedPosIdx, mFileListAdapter.lastCheckedPos[0], mFileListAdapter.lastCheckedPos[1]);
            if(mFileListAdapter.lastCheckedPosIdx!=-1){
                int fvp = listView.getFirstVisiblePosition();
                int start = mFileListAdapter.lastCheckedPos[0];
                int end = mFileListAdapter.lastCheckedPos[1];
                if(start>end){// enssure [start, end]>-1
                    start=end;
                    end=mFileListAdapter.lastCheckedPos[0];
                }
                if(start==-1 || end==-1){
                    return;
                }else if(fvp>=0)
                    listView.smoothScrollToPositionFromTop(fvp, listView.getChildAt(0).getTop());
                boolean bNeedInval=false;
                for (int pos = start; pos <= end; pos++) {
                    final FileListItem item = mFileListAdapter.getItem(pos);
                    if(MarkedItemList.addSelectedItem(item)!=null)
                        bNeedInval=true;
                }
                if(bNeedInval){
                    mFileListAdapter.notifyDataSetChanged();
                    invalidateSelectBtn(MarkedItemList.getFileCount());
                }
                if(end<listView.getFirstVisiblePosition()) listView.setSelection(start);
                else if(start>listView.getLastVisiblePosition()) listView.setSelection(start);
            }
        }
        else if(id == R.id.new_folder) {//新建
          	final ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_edittext, null);
          	final EditText etNew = dv.findViewById(R.id.edt_input);
          	final View btn_Done = dv.findViewById(R.id.done);
          	final ImageView btn_SwicthFolderCreation = dv.findViewById(R.id.toolbar_action1);
        	if(opt.getCreatingFile())
				etNew.setHint(R.string.fp_cf);
        	else
				btn_SwicthFolderCreation.setColorFilter(Color.GRAY);
        	final Dialog dd = new GoodKeyboardDialog(getContext());
        	dd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        	dd.setContentView(dv);
        	btn_SwicthFolderCreation.setOnClickListener(v17 -> {
				if(opt.setCreatingFile(!opt.getCreatingFile())) {
					btn_SwicthFolderCreation.setColorFilter(null);//本色毕露
					etNew.setHint(R.string.fp_cf);
				}else {
					btn_SwicthFolderCreation.setColorFilter(Color.GRAY);
					etNew.setHint(R.string.fp_cff);
				}
			});
        	btn_Done.setOnClickListener(v1 -> {
                if(etNew.getText().length()==0) {
                    Toast.makeText(getContext(), "长度不能为零", Toast.LENGTH_SHORT).show();
                    return;
                }
                String currentDirName = dir_path.getText().toString();
                File new_Folder = new File(currentDirName+"/"+etNew.getText());
                if(new_Folder.exists()) {
                	//if(new_Folder.isDirectory() ^ opt.getCreatingFile()){
						Toast.makeText(getContext(), "已存在", Toast.LENGTH_SHORT).show();
						int id13 = internalList.indexOf(new FileListItem(new_Folder,false));
						if(id13 !=-1)
							listView.setSelection(id13);
						return;
					//}
                }
                int ret= FU.checkSdcardPermission(getContext(),new_Folder);
                if(ret!=0) {
                    if(ret==-1) AskPermissionSnack(etNew);
                else Toast.makeText(getContext(), "未知错误1:"+ret, Toast.LENGTH_LONG).show();
                    return;
                }
                ret= FU.mkdir5(getContext(),new_Folder,opt.getCreatingFile());
                if(ret>=0) {
                    internalList.add(new FileListItem(new_Folder, true));
                    mFileListAdapter.notifyDataSetChanged();
                    dd.dismiss();
                }else {
                    if(ret==-1)
                        Toast.makeText(getContext(), "请赋予sd卡读写权限。", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getContext(), "未知错误2:"+ret, Toast.LENGTH_LONG).show();
                }
            });
        	etNew.setOnEditorActionListener(new OnEditorActionListener(){

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
						btn_Done.performClick();
						return true;
					}
					return false;
				}});

			//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        	//imm.showSoftInput(etNew, InputMethodManager.SHOW_FORCED);
        	dd.getWindow().setGravity(Gravity.TOP);
        	dd.getWindow().getAttributes().verticalMargin=0.01f;
        	dd.getWindow().setAttributes(dd.getWindow().getAttributes());
        	dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        	//dd.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_s);
        	dd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dd.show();
            if(true){
                dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                etNew.requestFocus();
            }
        }
        else if(id == R.id.etSearch) {
          	final ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_editsearch, null);
          	final EditText etNew = dv.findViewById(R.id.edt_input);
          	final View btn_Next = dv.findViewById(R.id.toxia);
          	final View btn_Last = dv.findViewById(R.id.toshn);
          	final ImageView btn_SwicthFolderCreation = dv.findViewById(R.id.toolbar_action1);
        	if(!opt.getRegexSearch()) {
        		btn_SwicthFolderCreation.setColorFilter(Color.GRAY);
        	}else
    			etNew.setHint(R.string.regex);

        	if(ph.text!=null){
				String phrase=ph.text;
				if(!opt.getRegexSearch() && phrase.startsWith("^")) phrase=phrase.substring(1);
				etNew.setText(phrase);
			}

        	final Dialog dd = new GoodKeyboardDialog(getContext());
        	dd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        	dd.setContentView(dv);
			btn_SwicthFolderCreation.setOnClickListener(v15 -> {
				if(opt.setRegexSearch(!opt.getRegexSearch())) {
					btn_SwicthFolderCreation.setColorFilter(null);//本色毕露
					etNew.setHint(R.string.regex);
				}else {
					btn_SwicthFolderCreation.setColorFilter(Color.GRAY);
					etNew.setHint(null);
				}
			});
            View.OnClickListener shangxiaClicker= v12 -> {
				if(etNew.getText().length()==0) {
					Toast.makeText(getContext(), "长度不能为零", Toast.LENGTH_SHORT).show();
					return;
				}
				String text = etNew.getText().toString();
				boolean bNeedInvaildate=false;
				if(!text.equals(ph.text) || opt.getRegexSearch()!=old_use_regex){
					bNeedInvaildate=true;
					ph.pattern=null;
					if(opt.getRegexSearch()) try {
						ph.pattern = Pattern.compile(text,Pattern.CASE_INSENSITIVE);
					} catch (Exception e) { ph.pattern = Pattern.compile(etNew.getText().toString(),Pattern.LITERAL); }
					ph.text =text;
					old_use_regex=opt.getRegexSearch();
				}
				if(bNeedInvaildate)mFileListAdapter.notifyDataSetChanged();
				int fvp = listView.getFirstVisiblePosition();
				boolean backward = v12.getId()== R.id.toshn;
				int i = fvp+1,max=mFileListAdapter.getCount(),delta=1;
				if(backward){i=fvp-1;delta=-1;}
				for (; backward?i>0:i<max ; i+=delta) {
					String tofind = mFileListAdapter.getItem(i).filename;
					if(ph.pattern==null?tofind.toLowerCase().startsWith(ph.text):ph.pattern.matcher(tofind).find()){
						listView.setSelection(i);
						break;
					}
				}
			};
            OnLongClickListener shangxiaLongClicker = v13 -> {
				boolean backward = v13.getId()== R.id.toshn;
				listView.setSelection(backward?0:mFileListAdapter.getCount()-1);
				return true;
			};
            btn_Next.setOnClickListener(shangxiaClicker);
            btn_Last.setOnClickListener(shangxiaClicker);
            btn_Next.setOnLongClickListener(shangxiaLongClicker);
            btn_Last.setOnLongClickListener(shangxiaLongClicker);
        	etNew.setOnEditorActionListener((v14, actionId, event) -> {
				if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
					btn_Next.performClick();
					return true;
				}
				return false;
			});
        	Window win=dd.getWindow();
			if (win != null) {
				win.setGravity(Gravity.TOP);
				win.setDimAmount(0);
				win.getAttributes().verticalMargin=0.01f;
				win.setAttributes(dd.getWindow().getAttributes());
				win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			}
            dd.setOnDismissListener(new ViewDissmisser(FooterView));
        	dd.show();
            FooterView.setVisibility(View.GONE);
            if(true){
                dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                etNew.requestFocus();
            }
        }
    }

	private void snack_lock() {
		snackbar = Snackbar.make(listView, R.string.fp_warn_lock, Snackbar.LENGTH_LONG)
				.setAction("Re-lock", new
						View.OnClickListener(){
							@Override
							public void onClick(View v) {
								lockit();
								ChangeToDir(currLocation);
							}
						});
		snackbar.show();
	}

	private void exit_selection_due_to_zero() {
		if(menupopup!=null)menupopup.dismiss();
		mFileListAdapter.bIsSelecting=false;
		mFileListAdapter.notifyDataSetChanged();
		decorate_bottom_bar();
	}

	private void show_views_dialog() {
		long mFlagLocalStamp = opt.getFlag();
		if(d!=null) return;
		if(ComfyView==null){
			ComfyView = getLayoutInflater().inflate(R.layout.dialog_change_sorting_l, null);
			ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{-android.R.attr.state_checked},new int[]{android.R.attr.state_checked}},
					new int[]{Color.BLACK,Color.BLACK}
			);
			if(Build.VERSION.SDK_INT<23){
				TextView vm1 = ComfyView.findViewById(R.id.viewmode1);
				TextView vm2 = ComfyView.findViewById(R.id.viewmode2);
				TextView vm3 = ComfyView.findViewById(R.id.viewmode3);
				vm1.getCompoundDrawables()[1].setColorFilter(0xff666666, PorterDuff.Mode.SRC_IN);
				vm2.getCompoundDrawables()[1].setColorFilter(0xff666666, PorterDuff.Mode.SRC_IN);
				vm3.getCompoundDrawables()[1].mutate().setColorFilter(0xff666666, PorterDuff.Mode.SRC_IN);
			}
			ckList = ComfyView.findViewById(R.id.enable_list);
			AppCompatCheckBox ckThumb = ComfyView.findViewById(R.id.enable_thumbs);
			AppCompatCheckBox ckThumbCrop = ComfyView.findViewById(R.id.crop_thumbs);
			AppCompatCheckBox ckThumbAutoHeight = ComfyView.findViewById(R.id.auto_height);
			ckList.setSupportButtonTintList(colorStateList);
			ckThumb.setSupportButtonTintList(colorStateList);
			ckThumbCrop.setSupportButtonTintList(colorStateList);
			ckThumbAutoHeight.setSupportButtonTintList(colorStateList);
			np1 = ComfyView.findViewById(R.id.numberpicker); ckList.setChecked(!opt.getEnableList());
			ckThumb.setChecked(opt.getEnableTumbnails());  ckThumbCrop.setChecked(opt.getCropTumbnails());
			ckThumbAutoHeight.setChecked(opt.getAutoThumbsHeight());

			ckList.setOnCheckedChangeListener(checkclicker); ckList.setChecked(opt.getEnableList());
			ckThumb.setOnCheckedChangeListener(checkclicker);
			ckThumbCrop.setOnCheckedChangeListener(checkclicker);
			ckThumbAutoHeight.setOnCheckedChangeListener(checkclicker);
		}
		androidx.appcompat.app.AlertDialog.Builder dialog_builder
				= new  androidx.appcompat.app.AlertDialog.Builder(getContext());

		if(opt.getSortMode()%2==0){
			SortModeCommon(((ViewGroup)ComfyView.findViewById(R.id.viewmode4).getParent()).getChildAt(FileListItem.comparation_method/2),-1);
		}else{
			SortModeCommon(((ViewGroup)ComfyView.findViewById(R.id.viewmode5).getParent()).getChildAt((FileListItem.comparation_method-1)/2),-1);
		}
		if(ComfyView.getParent()!=null)
			((ViewGroup)ComfyView.getParent()).removeView(ComfyView);
		dialog_builder.setView(ComfyView);
		androidx.appcompat.app.AlertDialog dTmp = dialog_builder.create();
		dTmp.show();
		d=dTmp;
		Window window = dTmp.getWindow();
		window.setWindowAnimations(R.style.fp_dialog_animation);
		MinifyViews(dTmp.findViewById(Resources.getSystem().getIdentifier("title","id", "android"))
				,dTmp.findViewById(Resources.getSystem().getIdentifier("titleDivider","id", "android")) );
		window.getDecorView().setBackgroundResource(R.drawable.popup_shadow_l);
		window.getDecorView().getBackground().setAlpha(238);
		window.setDimAmount(0.3f);
		AlertDialogLayout tv =  window.findViewById(R.id.parentPanel);
		tv.addView(getLayoutInflater().inflate(R.layout.circle_checker_item_menu_titilebar2,null),0);
		tv.addView(getLayoutInflater().inflate(R.layout.checker1,null));
		final CircleCheckBox ck = tv.getChildAt(tv.getChildCount()-1).findViewById(R.id.check1);
		//ck.drawInnerForEmptyState = true;
		ck.setBorderColor(ck.getCheckedColor());
		ck.setChecked(opt.getPinSortDialog());
		ck.setOnClickListener(vv -> {
			ck.toggle(false);
			opt.setPinSortDialog(ck.isChecked());
		});
		TextView titlebar = ((TextView) ((ViewGroup) tv.getChildAt(0)).getChildAt(0));
		titlebar.setGravity(Gravity.START);
		float density = getContext().getResources().getDisplayMetrics().density;
		titlebar.setPadding((int) (13*density), (int) (13*density),0,0);
		titlebar.setText(R.string.view);
		titlebar.setTextColor(Color.BLACK);
		dTmp.setOnDismissListener(comfy_dissmiss_l==null?(comfy_dissmiss_l=dialogInterface -> {
			if(opt.getSortMode()!= FileListItem.comparation_method){
				FileListItem.comparation_method=opt.getSortMode();
				ChangeToDir(currLocation);
			}else if(opt.getCropTumbnails(mFlagLocalStamp)!=opt.getCropTumbnails()
					||opt.getEnableTumbnails(mFlagLocalStamp)!=opt.getEnableTumbnails()
					||opt.getEnableList(mFlagLocalStamp)!=opt.getEnableList()
					||opt.getListIconSize(mFlagLocalStamp)!=opt.getListIconSize()
					||opt.getAutoThumbsHeight(mFlagLocalStamp)!=opt.getAutoThumbsHeight()
			)
				mFileListAdapter.notifyDataSetChanged();
			d=null;
			if(ComfyView.getParent()!=null)
				((ViewGroup)ComfyView.getParent()).removeView(ComfyView);
		}):comfy_dissmiss_l);
	}

	public void SelectGridMode(View v) {
		if(oldSelectedViewMode!=null)oldSelectedViewMode.setSelected(false);
		oldSelectedViewMode=v;
		v.setSelected(true);
	}


	public static void stylize_simple_message_dialog(Dialog d, Context context) {
		d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
		MinifyViews(d.findViewById(Resources.getSystem().getIdentifier("title","id", "android"))
				,d.findViewById(Resources.getSystem().getIdentifier("titleDivider","id", "android")) );
		SetViewHeight(d.findViewById(R.id.textSpacerNoTitle), (int) (30*context.getResources().getDisplayMetrics().density));
	}

	private void AskPermissionSnack(View snv) {
        if(snv==null) snv=listView;
        snackbar = Snackbar.make(snv, "请赋予sd卡读写权限。", Snackbar.LENGTH_SHORT);
        snackbar.setAction("赋予", view -> {
            getContext().startActivity(new Intent(Intent.ACTION_MAIN).setClass(getContext(), StorageActivity.class));
        });
        snackbar.show();
    }

    private void EnterSlideShowMode(int delay) {
        int ch = getContext().getResources().getColor(R.color.colorHeader);
        int nc = ColorUtils.blendARGB(ch, Color.TRANSPARENT, 0.1f);
        //FilePickerDialog.this.getWindow().setDimAmount(1);
        callbacks.onEnterSlideShow(getWindow(), delay);
        HeaderView.setBackgroundColor(nc);
        dir_path.setBackgroundColor  (nc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getView().getBackground().setTint(0xf0ffffff);
    }

    public void SortModeCommon(View v, int new_mode) {
        if(v!=null) {
            if (oldSelectedSortMode != null) oldSelectedSortMode.setSelected(false);
            v.setSelected(true);
            oldSelectedSortMode = v;
        }
        if(new_mode>=0) {
            opt.setSortMode(new_mode);
            if (!opt.getPinSortDialog()) {
                d.dismiss();
            }
        }
    }

    protected void invalidateSelectBtn(int size) {
		invalidateSelectBtnColor(size);
		if (size == 0) {
			select.setText(positiveBtnNameStr);
		} else {
			select.setText(positiveBtnNameStr + " (" + size + ") ");
		}
    }

    protected void invalidateSelectBtnColor(int size) {
		boolean hasValidSelection = properties.selection_type== DialogConfigs.DIR_SELECT  || size != 0;
		select.setEnabled(hasValidSelection);
		select.setTextColor(hasValidSelection?colorAccent:Color.argb(128, Color.red(colorAccent), Color.green(colorAccent), Color.blue(colorAccent)));
    }

    private void profaneSelction() {
        if(toggle_all.isChecked()) {
            toggle_all.setHalfChecked();
            toggle_all.invalidate();
        }
    }

    private void adapt_bkmk_size() {
		//bmlv.getLayoutParams().width = (int) (title.getWidth()-3*getContext().getResources().getDisplayMetrics().density);
		bmlv.getLayoutParams().width = (int) (title.getWidth()-3*getContext().getResources().getDisplayMetrics().density);
		//bmlv.getLayoutParams().width = HeaderView.getWidth() - ;
		bmlv.getLayoutParams().height = (int) (title.getHeight()-3*getContext().getResources().getDisplayMetrics().density);
		bmlv.setLayoutParams(bmlv.getLayoutParams());
	}

	private void sn(int id) {
		snackbar = Snackbar.make(listView, id, Snackbar.LENGTH_SHORT);
		snackbar.show();
        //CMNF.recurseLogCascade(snackbar.getView());
        //CMNF.recurseLogCascade(listView);
		//fuckSnackbarfuck CoordinatorLayout CAO NI MEI MEI
        //Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout)snackbar.getView();
        //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)layout.getLayoutParams();
        //params.width = CoordinatorLayout.LayoutParams.MATCH_PARENT;
	}

	private void sn(Object text) {
		snackbar = Snackbar.make(listView, String.valueOf(text), Snackbar.LENGTH_SHORT);
		snackbar.show();
		//fuckSnackbarfuck CoordinatorLayout CAO NI MEI MEI
        //Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout)snackbar.getView();
        //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)layout.getLayoutParams();
        //params.width = CoordinatorLayout.LayoutParams.MATCH_PARENT;
	}
	
	private void lockit() {
		//click_dir_at(null);
        if(!(properties.extensions==null && properties.selection_mode == DialogConfigs.MULTI_MODE && properties.selection_type== DialogConfigs.FILE_AND_DIR_SELECT))
        {//TODO instead of zero-out pick valid ones.
        	MarkedItemList.clearSelectionList();
        	invalidateSelectBtn(0);
        	toggle_all.setChecked(false);
        }
        if(properties.selection_mode == DialogConfigs.SINGLE_MODE)
        	toggle_all.setVisibility(View.GONE);
        mFileListAdapter.notifyDataSetChanged();
        properties.locked=true;
	}
	
    private boolean try_copy_file(File source, File dest) {
        try {
            FileChannel inputChannel = new FileInputStream(source).getChannel();
            FileChannel outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();
            dest.setLastModified(source.lastModified());
            //Files.copy(source.toPath(), dest.toPath());
            //Process p = getRuntime().exec(new String[]{"cp",source.getAbsolutePath(),dest.getAbsolutePath()});
            //Thread t=new Thread(new InputStreamRunnable(p.getErrorStream(),"ErrorStream"));
            //t.run();
            return true;
        } catch (Exception e){
			if(!FU.hasRootPermission()) {
			}else{//调用 shell 命令移动文件
				try {
					ExeCommand cmd = new ExeCommand().disPosable().root();
					int ret=cmd.run("cp "+"\""+source.getAbsolutePath()+"\" \""+dest.getAbsolutePath()+"\"\n"
							,"exit\n"
							,"exit\n"
					);
					if(FU.exsists(getContext(), dest)) return true;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
        }
        return false;
    }


    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        public TextView tv;
        public View dv;
        public SimpleViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(android.R.id.text1);
            dv = itemView.findViewById(R.id.close);
            //itemView.setId();
            itemView.setOnClickListener(FilePickerDialog.this);
            dv.setOnClickListener(FilePickerDialog.this);
            itemView.setOnLongClickListener(FilePickerDialog.this);
        }
    }

    final static boolean compareByteArrayIsPara(byte[] A,int offA,byte[] B){
        if(offA+B.length>A.length)
            return false;
        for(int i=0;i<B.length;i++){
            if(A[offA+i]!=B[i])
                return false;
        }
        return true;
    }

    public static int toInt(byte[] buffer,int offset) {
        int  values = 0;
        for (int i = 0; i < 4; i++) {
            values <<= 8; values|= (buffer[offset+i] & 0xff);
        }
        return values;
    }


    public static long toLong(byte[] buffer,int offset) {
        long  values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8; values|= (buffer[offset+i] & 0xff);
        }
        return values;
    }


    public static void MinifyViews(View...views){
    	for(View vI:views){
    		if(vI!=null){
				vI.setBackground(null);
				vI.setVisibility(View.GONE);
			}
		}
	}

	public static void SetViewHeight(View view, int h) {
		if(view!=null){
			view.getLayoutParams().height=h;
			view.setLayoutParams(view.getLayoutParams());
		}
	}

	@NonNull @Override
	public Bundle onSaveInstanceState() {
		dismiss();
		return super.onSaveInstanceState();
	}

	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		show();
	}
}
