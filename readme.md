Here presents PolymiumPic, the currently fully opensourced part of the pro-project PolymPic.   

Just like chrome, it is a fantastic web browser + a PDF viewer.  

### The Web Browser
- WIP 75%
- Multi-window interface that supports retoring of browser states ( the page positions and the back/forward stacks ).
- WIP 55% Annotation functionality. (still testing)



### The PDF Viewer
- WIP 90% Based on PDFium.
- Basic highlight annotatin support.
- Smooth scrolling and flinging available even for android 4.4.
- Make use of **Colordict** and **Google Translate** for easy reading.

> The PDF viewer supports content URI and will request permisison at runtime. You can still use it if you deny it's permission requests, entering read-only mode. 

### Download

The first Alpha version has been released. Check that and all feedbacks are welcomed.

### How to Build:
This is a 'complex' project which consists of multiple gradle sub-modules. You need to download them manually from the following separate repos:
1. https://github.com/KnIfER/AppPreference
2. https://github.com/KnIfER/AweDesigner
3. https://github.com/KnIfER/AwtRecyclerView
4. https://github.com/KnIfER/AxtAppCompat
5. https://github.com/KnIfER/GlideModule
6. ( Some may be missing. )

For some reason I've modified those 3rd party libaries but don't want to include them in my repo.    

If you don't want to download them, then go and grab the current [archives](https://github.com/KnIfER/PolymPic/releases/tag/0.1.alpha1) exported directly from the Android Studio.

### How to invoke PolymPic (the App) and view PDFs at a specific page in your own project:
1. Essential
```
	Intent it = new Intent(Intent.ACTION_VIEW)
		.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		.putExtra("page", yourPage)
		.setDataAndType(yourUri, "application/pdf");
	startActivity(it);
```


2. Verbose
```
	Intent it = new Intent(Intent.ACTION_VIEW);
	it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	File f = new File("/sdcard/download/....");
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		StrictMode.setVmPolicy(new VmPolicy.Builder().build());
	}
	int pageId=8;
	it.putExtra("page", pageId);
	it.setDataAndType(Uri.fromFile(f), "application/pdf");
	startActivity(it);
```
