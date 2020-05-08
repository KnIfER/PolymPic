package com.knziha.filepicker.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.knziha.filepicker.R;
import com.knziha.filepicker.utils.CMNF;

public class MenuAdapter extends BaseAdapter {
    final String[] menu_common;
    final String[] menu_selecting;
    private final int[] icons_common;
    private final int[] icons_selecting;
    private final int count_common;
    private final int count_selecting;
    FilePickerAdapter mWrappedFileListAdapter;
    Context cc;
    
    MenuAdapter(FilePickerAdapter inWrappedFileListAdapter, Context c){
        super();
        cc=c;
        mWrappedFileListAdapter=inWrappedFileListAdapter;
        menu_common = c.getResources().getStringArray(R.array.menu_common);
        menu_selecting = c.getResources().getStringArray(R.array.menu_selecting);
        //icons_common = c.getResources().getIntArray(R.array.icons_common);
        //icons_selecting = c.getResources().getIntArray(R.array.icons_selecting);
        icons_common = new int[]{
                R.drawable.favorcover,
                R.drawable.ic_arrow_forward_black_24dp,
                R.drawable.tools_filepicker,
                R.drawable.fp_lock,
                R.drawable.ic_viewpager_carousel,
                R.drawable.info,
        };
        icons_selecting = new int[]{
                R.drawable.ic_view_comfy_black_24dp,
                R.drawable.ic_stop_black_24dp,
                R.drawable.ic_arrow_forward_black_24dp,
                R.drawable.fp_lock,
                R.drawable.info,
                R.drawable.ic_send_black_24dp,
                R.drawable.ic_share_filepicker_24dp,
                R.drawable.fp_move,
                R.drawable.fp_copy,
        };
        count_common = Math.min(icons_common.length, menu_common.length);
        count_selecting = Math.min(icons_selecting.length, menu_selecting.length);
    }
    
    @Override
    public int getCount() {
        return mWrappedFileListAdapter.bIsSelecting?count_selecting:count_common;
    }

    @Override
    public String getItem(int position) {
        return mWrappedFileListAdapter.bIsSelecting?menu_selecting[position]:menu_common[position];
    }

    @Override
    public long getItemId(int position) {
        return getId(position);
    }

    public int getId(int position) {
        return mWrappedFileListAdapter.bIsSelecting?icons_selecting[position]:icons_common[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if(convertView instanceof TextView){
            tv= (TextView) convertView;
        }else{
            Context context;
            if(parent!=null) {cc=null;context=parent.getContext();}
            else context=cc;
            tv = new TextView(context);
            DisplayMetrics dm = parent.getContext().getResources().getDisplayMetrics();
            final int pad = (int) (10*dm.density);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.listviewselector3);
            tv.setPadding(pad,pad,pad,pad);
            tv.setSingleLine(true);
            tv.setTextSize(15.5f);
        }
        tv.setText(getItem(position));
        tv.setText("  "+tv.getText());
        TextPaint painter = tv.getPaint();
        final float fontHeight = painter.getFontMetrics().bottom - painter.getFontMetrics().top;
        Drawable d = parent.getContext().getResources().getDrawable(getId(position));
        d.setBounds(0,0, (int) fontHeight, (int) fontHeight);
        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        tv.setCompoundDrawables(d,null,null,null);
        return tv;
    }
}
