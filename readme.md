This is PolyiumPic, the (currently fully) opensource part of the professional project PolymPic 

:)  

Just like the chrome browser, it is a fantastic web browser + a PDF viewer.  

### The Web Browser
- WIP 75%
- Multi-window interface supporting retoring of browse states ( the page position and the back/forward stack ).
- WIP Annotation functionality. (still testing)



### The PDF Viewer
- WIP 90%
- Basic highlight annotatin support.
- Smooth scroll and fling handling.
- Invoke **Colordict** or **Google Translate** for easy reading.

> The PDF Viewer Support content URI and request permisison at runtime. You can still use it event when you deny the permission request, entering read-only mode. 

### How to invoke PolymPic to view PDFs at a specific page in your own project:
1. Essential
```
	Intent it = new Intent(Intent.ACTION_VIEW);
	it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	it.putExtra("page", yourPage);
	it.setDataAndType(yourUri, "application/pdf");
	startActivity(it);
```


2. Verbose
```
	Intent it = new Intent(Intent.ACTION_VIEW);
	it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	File f = new File("/sdcard/download/....");
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		//StrictMode.setVmPolicy(StrictMode.getVmPolicy());
		StrictMode.setVmPolicy(new VmPolicy.Builder().build());
	}
	it.putExtra("page", pageId);
	it.setDataAndType(Uri.fromFile(f), "application/pdf");
	startActivity(it);
```