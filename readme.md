Here is PolyiumPic, the (currently fully) opensourced part of our pro-project PolymPic :) .   

Just like the chrome browser, it is a fantastic web browser + a PDF viewer.  

### The Web Browser
- WIP 75%
- Multi-window interface that supports retoring of browser states ( the page positions and the back/forward stacks ).
- WIP 55% Annotation functionality. (still testing)



### The PDF Viewer
- WIP 90% Based on PDFium.
- Basic highlight annotatin support.
- Smooth scrolling and flinging.
- Make use of **Colordict** or **Google Translate** for easy reading.

> The PDF viewer supports content URI and will request permisison at runtime. You can still use it event when you deny it's permission requests, entering read-only mode. 

### How to invoke PolymPic and view PDFs at a specific page in your own project:
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