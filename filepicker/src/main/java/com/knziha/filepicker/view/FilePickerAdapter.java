/*
 * Copyright (C) 2019 KnIfE
 * Copyright (C) 2016 Angad Singh
 *
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

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.knziha.filepicker.R;
import com.knziha.filepicker.model.NotifyItemChecked;
import com.knziha.filepicker.model.AudioCover;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.model.MarkedItemList;
import com.knziha.filepicker.model.MyRequestListener;
import com.knziha.filepicker.model.PatternHolder;
import com.knziha.filepicker.utils.ExtensionHelper;
import com.knziha.filepicker.widget.MaterialCheckbox;
import com.knziha.filepicker.widget.OnCheckedChangeListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

/** Used to populate ListView or GridView with file info. */
public class FilePickerAdapter extends ArrayAdapter<FileListItem>{
    private final FilePickerOptions opt;
	public int colorAccent;
	public int colorPrimary;
	private ArrayList<FileListItem> listItem;
    private Context context;
    private DialogProperties properties;
    private NotifyItemChecked notifyItemChecked;
    public boolean bIsSelecting=true;
    SimpleDateFormat timemachine;
    public SparseArray<Object> PrevewsPool = new SparseArray<>();
    final private PatternHolder ph;
	private String asset_string;
	private String parent_string;

	public FilePickerAdapter(ArrayList<FileListItem> listItem, Context context, DialogProperties properties, PatternHolder inph, FilePickerOptions inopt) {
        super(context,R.layout.dialog_file_list_item,R.id.fname,listItem);
        this.listItem = listItem;
        this.context = context;
        this.properties = properties;
        if(properties.selection_mode== DialogConfigs.SINGLE_MULTI_MODE) {
            properties.selection_mode = DialogConfigs.MULTI_MODE;
            bIsSelecting =false;
        }
		asset_string = context.getString(R.string.asset);
		parent_string = context.getString(R.string.label_parent_directory);
        timemachine = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        ph=inph;
        opt = inopt;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public FileListItem getItem(int i) {
        return listItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
	@Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        ViewHolder holder = view == null?new ViewHolder(context, viewGroup):(ViewHolder)view.getTag();
        if(i<0) return null;
        final FileListItem item = listItem.get(i);
        holder.fmark.setTag(i);
		holder.fmark.setInflated(bIsSelecting);
		ImageView type_icon = holder.type_icon;

		int imageResource=R.mipmap.ic_type_folder;
		int colorfilter=colorPrimary;
		switch (item.directory){
			case 3://上级目录
				if(i==0) {
					if (item.time == -2) {
						holder.type.subText = null;
						holder.type.setText(R.string.label_root_directory);
					} else if (item.time == -1) {
						holder.type.subText = (getCount()-1)+" 项";
						String parentName = item.getLocation().substring(item.getLocation().lastIndexOf("/") + 1);
						holder.type.setText(parent_string+(parentName.length()==0?parentName:(" - " + parentName)));
					}
					holder.fmark.setVisibility(View.INVISIBLE);
				}
			break;
			case 2://根目录
				holder.type.subText = null;
				imageResource=R.drawable.ic_sd_storage_black_24dp;
				colorfilter=0xFF2b4381;
				holder.fmark.setVisibility(properties.selection_type == DialogConfigs.DIR_SELECT && properties.locked?
						View.INVISIBLE:View.VISIBLE);
				holder.type.setText(R.string.local_storage);
			break;
			case 1://目录
				imageResource=R.mipmap.ic_type_folder;
				holder.type.subText = timemachine.format(new Date(item.getTime()));
				if(item.size==-1) {
					String[] list = new File(item.location).list();
					item.size=list==null?-2:list.length;
				}
				holder.fmark.setVisibility(properties.selection_type == DialogConfigs.FILE_SELECT && !properties.locked?
						View.INVISIBLE:View.VISIBLE);
				holder.type.setText(item.size<0?null:item.size+" 项");
			break;
			case 0://文件
			case -1://资料
				imageResource=R.mipmap.ic_type_file;
				if(item.size==-1) {
					File currentFile = new File(item.location);
					item.size=currentFile.length();
				}
				if(item.directory==-1){
					colorfilter=0xFF3F51B5;
					holder.type.subText = asset_string;
				}else{
					colorfilter=colorAccent;
					holder.type.subText = timemachine.format(new Date(item.getTime()));
				}
				holder.fmark.setVisibility(properties.selection_type == DialogConfigs.DIR_SELECT && properties.locked?
						View.INVISIBLE:View.VISIBLE);
				holder.type.setText(mp4meta.utils.CMN.formatSize(item.size));
			break;
		}

		type_icon.setImageResource(imageResource);
        type_icon.setColorFilter(colorfilter);
        holder.name.setText(item.filename);

		holder.name.setTextColor(properties.isDark?Color.WHITE:Color.BLACK);
		holder.type.setTextColor(properties.isDark?0x8aFFFFFF:0x8a000000);

		if (i>0 && holder.fmark.getVisibility() == View.VISIBLE)
			holder.fmark.setChecked((MarkedItemList.hasItem(item.location)));

        decorate_by_keys(holder.name);

		boolean b2 = false;
		String file_suffix=null;
		int suffix_idx;
		if(!item.isDirectory())
			if((suffix_idx=item.filename.lastIndexOf("."))!=-1){file_suffix=item.filename.substring(suffix_idx).toLowerCase();}

        if(opt.getEnableTumbnails() && file_suffix!=null && (ExtensionHelper.FOOTAGE.contains(file_suffix)||ExtensionHelper.PHOTO.contains(file_suffix) || (b2=ExtensionHelper.SOUNDS.contains(file_suffix)))) {
			DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
			int targetDimension = (int) (opt.getListIconSize() * 1.f / 16 * Math.min(dm.widthPixels, dm.heightPixels));
			decorate_by_dimensions(type_icon, targetDimension,
					opt.getAutoThumbsHeight() ? LayoutParams.WRAP_CONTENT : targetDimension);
			//decorate_by_dimensions(type_icon, targetDimension, LayoutParams.MATCH_PARENT);
			Priority priority = Priority.HIGH;
			RequestOptions options = new RequestOptions()
					.signature(new ObjectKey(item.time))//+"|"+item.size
					.format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
					.priority(priority)
					.skipMemoryCache(false)
					.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
					//.onlyRetrieveFromCache(true)
					.fitCenter()
					.override(360, Target.SIZE_ORIGINAL);
			type_icon.setColorFilter(null);
			type_icon.setTag(R.id.home, false);
			RequestManager IncanOpen = Glide.with(getContext().getApplicationContext());
			(b2?IncanOpen.load(new AudioCover(item.location)):
					IncanOpen.load(item.location))
					.apply(options)
					.format(DecodeFormat.PREFER_RGB_565)
					.listener(myreqL2.setCrop(opt.getCropTumbnails()))
					.into(holder.type_icon)
			;
        }
        else {
			type_icon.setTag(R.id.home, null);
			decorate_by_dimensions(type_icon, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			type_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		}
        return holder.itemView;
    }

    private void decorate_by_dimensions(View v, int w, int h) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        boolean needSet=false;
        if(lp.width!=w){lp.width = w;needSet=true;}
        if(lp.height!=h){lp.height = h;needSet=true;}
        //if(needSet)v.setLayoutParams(lp);
    }

    MyRequestListener myreqL2 = new MyRequestListener<Drawable>();

    private void decorate_by_keys(TextView name) {
        if(ph.pattern!=null){
            Matcher m = ph.pattern.matcher(name.getText().toString());
            SpannableStringBuilder spannable = null;
            while(m.find()){
                if(spannable==null)spannable = new SpannableStringBuilder(name.getText());
                spannable.setSpan(new ForegroundColorSpan(Color.RED),m.start(), m.end(),SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if(spannable!=null) name.setText(spannable);
        }else if(ph.text !=null){
            String tofind = name.getText().toString();
            int index=-ph.text.length();
            SpannableStringBuilder spannable = null;
            while((index=tofind.indexOf(ph.text, index+ph.text.length()))!=-1){
                if(spannable==null)spannable = new SpannableStringBuilder(name.getText());
                spannable.setSpan(new ForegroundColorSpan(Color.RED),index, index+ph.text.length(),SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if(spannable!=null) name.setText(spannable);
        }
    }

    public int[] lastCheckedPos = new int[]{-1,-1};
    public int lastCheckedPosIdx=-1;
    private OnCheckedChangeListener mItemCheckedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(MaterialCheckbox checkbox, boolean isChecked, boolean isRisingEdge) {
            //CMNF.Log("onCheckedChangedonCheckedChanged", isChecked, isRisingEdge);
            int position = (Integer) checkbox.getTag();
            if(lastCheckedPosIdx==-1 || lastCheckedPos[lastCheckedPosIdx]!=position){
                lastCheckedPosIdx=(lastCheckedPosIdx+1)%2;
                lastCheckedPos[lastCheckedPosIdx] = position;
            }
            final FileListItem item = listItem.get(position);
            item.setMarked(isChecked);
            if (isChecked) {
                if(properties.selection_mode == DialogConfigs.MULTI_MODE || !properties.locked) {//多选模式，或者解锁
                    MarkedItemList.addSelectedItem(item);
                } else {
                    MarkedItemList.addSingleFile(item);
                }
            } else {
                MarkedItemList.removeSelectedItem(item.getLocation());
            }
            if(isRisingEdge){
                bIsSelecting = true;
                notifyDataSetChanged();
            }
            notifyItemChecked.notifyCheckBoxIsClicked(item, isChecked);
        }
    };

    class ViewHolder
    {
		private final View itemView;
		ImageView type_icon;
        TextView name;
        FileInfoTextView type;
        MaterialCheckbox fmark;

        ViewHolder(Context context, ViewGroup viewGroup) {
			itemView=LayoutInflater.from(context).inflate(R.layout.dialog_file_list_item, viewGroup, false);
            name= itemView.findViewById(R.id.fname);
            type= itemView.findViewById(R.id.ftype);
            type_icon= itemView.findViewById(R.id.image_type);
            fmark= itemView.findViewById(R.id.file_mark);
			itemView.setTag(this);
			fmark.setOnCheckedChangedListener(mItemCheckedListener);
        }
    }

    public void setNotifyItemCheckedListener(NotifyItemChecked notifyItemChecked) {
        this.notifyItemChecked = notifyItemChecked;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if(!bIsSelecting){
            lastCheckedPos[0]=lastCheckedPos[1]=-1;
        }
    }
}
