package com.knaiver.polymer.widgets;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.knaiver.polymer.Toastable_Activity;
import com.knaiver.polymer.Utils.CMN;

public class ShareActivity extends Toastable_Activity implements View.OnClickListener {
	private com.knaiver.polymer.widgets.AppIconsAdapter  ada;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		FrameLayout layout = new FrameLayout(ShareActivity.this);
        Button button = new Button(ShareActivity.this);
        button.setText("测试分享");
		button.setTextColor(Color.WHITE);
        layout.addView(button);
		ViewGroup.LayoutParams lp = button.getLayoutParams();
		lp.width=-1;
		button.getLayoutParams().height=-2;
        setContentView(layout);
		button.setOnClickListener(this);
		
		//button.performClick();
	}
    
	@Override
	public void onClick(View v) {
		CMN.rt("分享链接……");
		if(ada==null) {
			ada = new com.knaiver.polymer.widgets.AppIconsAdapter (this);
		}
		CMN.pt("新建耗时：");
		ada.pullAvailableApps(this, "http://www.baidu.com", null);
		CMN.pt("拉取耗时：");
	}
	
//
//	@SuppressWarnings("rawtypes")
//	public class AppIconsAdapter extends RecyclerView.Adapter<AppIconsAdapter.ViewHolder> {
//		public final View bottomSheet;
//		private final BottomSheetDialog shareDialog;
//		private final FlowTextView indicator;
//		private TextPaint textPainter;
//		private ArrayList<AppBean> list = new ArrayList<>();
//		private View.OnClickListener itemClicker;
//		private Intent intent;
//		private PackageManager pm;
//
//		public AppIconsAdapter(Toastable_Activity a) {
//			textPainter = DescriptiveImageView.createTextPainter();
//			shareDialog = new BottomSheetDialog(a);
//			itemClicker = v1 -> {
//				ViewHolder vh = (ViewHolder) v1.getTag();
//				AppBean appBean = list.get(vh.position);
//				Intent shareIntent = new Intent(intent);
//				shareIntent.setComponent(new ComponentName(appBean.pkgName, appBean.appLauncherClassName));
//				shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				a.startActivity(shareIntent);
//				shareDialog.dismiss();
//			};
//			bottomSheet = View.inflate(a, R.layout.share_bottom_dialog, null);
//			indicator = bottomSheet.findViewById(R.id.indicator);
//			RecyclerView recyclerView = bottomSheet.findViewById(R.id.recycler);
//			recyclerView.setLayoutManager(new GridLayoutManager(a, 4));
//			recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
//			recyclerView.setHasFixedSize(true);
//			recyclerView.setAdapter(this);
//
//			shareDialog.setContentView(bottomSheet);
//		}
//
//		public void show(Toastable_Activity a) {
//			shareDialog.show();
//
//			Window win = shareDialog.getWindow();
//			if(win!=null) {
//				int maxWidth = (int) (GlobalOptions.density*480);
//				WindowManager.LayoutParams attr = win.getAttributes();
//				int targetW=a.dm.widthPixels>maxWidth?maxWidth:ViewGroup.LayoutParams.MATCH_PARENT;
//				if(targetW!=attr.width){
//					attr.width = targetW;
//					win.setAttributes(attr);
//				}
//			}
//			boolean landScape = a.mConfiguration.orientation==Configuration.ORIENTATION_LANDSCAPE;
//			BottomSheetBehavior beh = shareDialog.getBehavior();
//			beh.setState(landScape?BottomSheetBehavior.STATE_EXPANDED:BottomSheetBehavior.STATE_COLLAPSED);
//			beh.setSkipCollapsed(landScape);
//			int target = GlobalOptions.isDark?Color.WHITE:Color.BLACK;
//			textPainter.setColor(target);
//			indicator.setTextColor(target);
//			indicator.setText(a.getResources().getString(R.string.share_link));
//			bottomSheet.setBackground(GlobalOptions.isDark?GrayBG:null);
//		}
//
//		/* 重新拉取图标 */
//		public void pullAvailableApps(Toastable_Activity a, String url, String text) {
//			if(text==null) {
//				intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//			} else {
//				intent=new Intent(Intent.ACTION_SEND);
//				intent.putExtra(Intent.EXTRA_TEXT, text);
//				intent.setType("text/plain");
//			}
//			pm = a.getPackageManager();
//			List<ResolveInfo> resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
//			list.clear();
//			PackageManager packageManager = a.getPackageManager();
//			for (ResolveInfo RinfoI : resolved) {
//				AppBean appBean = new AppBean();
//				appBean.data = RinfoI;
//				appBean.pkgName = RinfoI.activityInfo.packageName;
//				appBean.appLauncherClassName = RinfoI.activityInfo.name;
//				list.add(appBean);
//			}
//			notifyDataSetChanged();
//			show(a);
//		}
//
//		@Override
//		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//			//CMN.Log("AppIconsAdapter::onCreateViewHolder");
//			ViewHolder ret = new ViewHolder(LayoutInflater.from(parent.getContext())
//					.inflate(R.layout.share_recycler_item, parent, false));
//			ret.itemView.setOnClickListener(itemClicker);
//			ret.textImageView.textPainter=textPainter;
//			ret.textImageView.bNeedShadow=true;
//			return ret;
//		}
//
//		@Override
//		public void onBindViewHolder(ViewHolder holder, final int position) {
//			holder.position = position;
//			AppBean app = list.get(position);
//			app.load();
//			DescriptiveImageView iv = holder.textImageView;
//			iv.setImageDrawable(app.icon);
//			iv.setText(app.appName);
//		}
//
//		@Override
//		public int getItemCount() {
//			return list.size();
//		}
//
//		public class ViewHolder extends RecyclerView.ViewHolder {
//			public int position;
//			public DescriptiveImageView textImageView;
//			public ViewHolder(View itemView) {
//				super(itemView);
//				textImageView = itemView.findViewById(R.id.app_icon_iv);
//				itemView.setTag(this);
//			}
//		}
//
//		public class AppBean {
//			public ResolveInfo data;
//			public Drawable icon;
//			public String appName;
//			public String pkgName;
//			public String appLauncherClassName;
//			public boolean loaded;
//
//			public void load() {
//				if(!loaded) {
//					appName = data.loadLabel(pm).toString();
//					icon = data.loadIcon(pm);
//					loaded=true;
//				}
//			}
//		}
//	}
}
