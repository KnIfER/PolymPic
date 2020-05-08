package com.knziha.filepicker.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.knziha.filepicker.R;
import com.knziha.filepicker.commands.ExeCommand;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.view.FileListItem;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**<p>
 * Created by Angad Singh on 11-07-2016.
 * </p>
 */
public class FU {


	/**
     * Post Lollipop Devices require permissions on Runtime (Risky Ones), even though it has been
     * specified in the uses-permission tag of manifest. checkStorageAccessPermissions
     * method checks whether the READ EXTERNAL STORAGE permission has been granted to
     * the Application.
     * @return a boolean value notifying whether the permission is granted or not.
     */
    public static boolean checkStorageAccessPermissions(Context context)
    {   //Only for Android M and above.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String permission = "android.permission.READ_EXTERNAL_STORAGE";
            int res = context.checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);
        }
        else
        {   //Pre Marshmallow can rely on Manifest defined permissions.
            return true;
        }
    }

    /**
     * Prepares the list of Files and Folders inside 'inter' Directory.
     * The list can be filtered through extensions. 'filter' reference
     * is the FileFilter. A reference of ArrayList is passed, in case it
     * may contain the ListItem for parent directory. Returns the List of
     * Directories/files in the form of ArrayList.
     * @param internalList ArrayList containing parent directory.
     *
     * @param inter The present directory to look into.
     *
     * @param filter Extension filter class reference, for filtering files.
     *
     * @return ArrayList of FileListItem containing file info of current directory.
     */
    public static void prepareFileListEntries(Context c, ArrayList<FileListItem> internalList, File inter, ExtensionFilter filter)
    {
    	boolean PleaseAddTheDirs=false;
        //Check for each and every directory/file in 'inter' directory. Filter by extension using 'filter' reference.
        //Log.d("fatal posioin",inter.listFiles(filter) + "scan file "+inter.getAbsolutePath());
        File[] Lst = inter.listFiles(filter); //TODO check "JNI WARNING: input is not valid Modified UTF-8: illegal start byte 0xf0"
        if(Lst==null) {
            PleaseAddTheDirs = true;
        }
        else {
            internalList.ensureCapacity(Lst.length+internalList.size()+5);
            for (File name : Lst) {
                //if (name.canRead())
                try {
                    FileListItem item = new FileListItem(name, FileListItem.comparation_method == 4 || FileListItem.comparation_method == 5);
                    internalList.add(item);
                } catch (Exception e) {
                    PleaseAddTheDirs = true;
                }
            }
            //long stst = System.currentTimeMillis();
            Collections.sort(internalList);
            //CMNF.Log("排序时间", System.currentTimeMillis() - stst);
        }
        boolean b1=inter.getPath().equals("/storage"),b2=false;
        if(!b1) b2=inter.getPath().equals("/");
        if(b1 || b2 || PleaseAddTheDirs) {
    		try {
	    		StorageManager sm = (StorageManager) c.getSystemService(Activity.STORAGE_SERVICE);
	    		if(mGetVolumePathsMethod==null) {
	                mGetVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", new  Class[0]);
	            }
	            final String[] paths = (String[])( mGetVolumePathsMethod.invoke(sm, new  Object[]{}));
	            // second element in paths[] is secondary storage path
	            Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", new Class[] {String.class});
	            boolean bHeiShou = false;
	            for(String name:paths) {
	            	String state = (String) getVolumeStateMethod.invoke(sm, name);
	            	if(state!=null && state.equals(Environment.MEDIA_MOUNTED)) {
	            		if(b1 && !bHeiShou) {
	            			if(internalList.size()>0) {//sanitycheck
		            			FileListItem p = internalList.get(0);
		            			internalList.clear();
		            			internalList.add(p);
	            			}
	            		}
            			bHeiShou=true;
	            		PleaseAddTheDirs=false;
	            		if(!b1 && !b2) {
	            			if(!name.startsWith(inter.getAbsolutePath()))
	            				continue;
	            			PleaseAddTheDirs=true;
	            		}
	            		FileListItem item = new FileListItem(name);
	                    item.setFilename(PleaseAddTheDirs?(name.substring(inter.getAbsolutePath().length())):b1?name.substring(8):name);
	                    item.setDirectory(2);
	                    //Add row to the List of directories/files
	                    internalList.add(item);
	            	}
	            }
    		}
            catch (Exception e)
            {   //Just dont worry, it rarely occurs.
                e.printStackTrace();
                //internalList.clear();
            }
    	}

    	if(CMNF.AssetMap!=null && inter.getPath().equals("/ASSET")){
                Iterator iter = CMNF.AssetMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    Integer val = (Integer) entry.getValue();
                    if(val!=null && filter.accept(key)){
                        FileListItem item = new FileListItem();
                        item.setFilename(c.getResources().getStringArray(R.array.stellarium)[val] );
                        item.setSize(c.getResources().getIntArray(R.array.stellariumblow)[val] );
                        item.setDirectory(-1);
                        item.setLocation(key);
                        //Add row to the List of directories/files
                        internalList.add(item);
                    }
                }
        }
        //return internalList;
    }

    public static final int checkSdcardPermission(Context context, File new_Folder) {
        //if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N)return  0;
        String filePath = new_Folder.getAbsolutePath();
		if(!filePath.startsWith("/sdcard/") && !filePath.startsWith("/storage/emulated/0/"))
        try {
            StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            boolean isPrimary=false; String uuid=null;
            if (bGoodStorageAvailable) {
                StorageVolume sv = sm!=null?sm.getStorageVolume(new_Folder):null;
                if(sv!=null){ isPrimary=sv.isPrimary(); uuid = sv.getUuid(); }
            }
            if(uuid==null){//反射大法
                try {
                    if(csw.init()){
                        Object [] results=(Object[]) csw.getVolumeList.invoke(sm);
                        if(results!=null) {
                            for (Object rI : results) {
                                isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                String path=(String) csw.getPath.invoke(rI);
                                if(filePath.startsWith(path=new File(path).getAbsolutePath())){
                                    uuid=(String) csw.getUuid.invoke(rI);
                                }
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            if(!isPrimary){
                if(uuid!=null) {

                    StringBuilder url = new StringBuilder("content://com.android.externalstorage.documents/tree/")
                            .append(uuid).append("%3A").append("/document/").append(uuid).append("%3A");
                    //url.append(filePath.replace("/","%2F"));
                    Uri uri_start = Uri.parse(url.toString());
                    DocumentFile doc = DocumentFile.fromSingleUri(context, uri_start);
                    if(!doc.canWrite()) {
                        if(context instanceof Activity) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            ((Activity) context).startActivityForResult(intent, 700);
                            Toast.makeText(context, "请选择目标sd卡路径", Toast.LENGTH_LONG).show();
                        }
                        return -1;
                    }else {
                        return 0;
                    }
                }
            }
            //return -4;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return 0;
	}

    static Method mGetVolumePathsMethod;

	public static int rename5(Context context, File file, File new_Folder) {
		if(!file.exists()) return 222;
		if(FU.exsists(context, new_Folder)) return 233;
		String filePath = file.getAbsolutePath();
		if(!file.getAbsolutePath().startsWith("/sdcard/") && !file.getAbsolutePath().startsWith("/storage/emulated/0/") && !file.getAbsolutePath().startsWith("/data/"))
		try {
			if(bKindButComplexSdcardAvailable) {
				StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
				boolean isPrimary = false; String uuid = null;
				if (bGoodStorageAvailable) {
					StorageVolume sv = sm != null ? sm.getStorageVolume(new_Folder) : null;
					if (sv != null) {
						isPrimary = sv.isPrimary();
						uuid = sv.getUuid();
					}
				}
				if (uuid == null) {//反射大法
					try {
						if (csw.init()) {
							Object[] results = (Object[]) csw.getVolumeList.invoke(sm);
							if (results != null) {
								for (Object rI : results) {
									isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
									String path = (String) csw.getPath.invoke(rI);
									if (filePath.startsWith(path = new File(path).getAbsolutePath())) {
										uuid = (String) csw.getUuid.invoke(rI);
										filePath = filePath.substring(path.length());
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (bGoodStorageAvailable) {
					int index = filePath.indexOf(uuid);
					if (index != -1) {
						filePath = filePath.substring(index + uuid.length());
					}
				}
				if(!isPrimary){
					StringBuilder url = new StringBuilder(256);
					Uri uri_start;
					url.setLength(0);
					url.append(DOCUMENTTREEURIBASE)
							.append(uuid).append(COLON).append(DOCUMENT).append(uuid).append(COLON);

					url.append(filePath.replace("/", SLANT)/*URLEncoder.encode(fnParent,"utf8")*/);
					uri_start = Uri.parse(url.toString());

					DocumentFile docParent = DocumentFile.fromTreeUri(context, uri_start);
					if (docParent == null) return -234;
					if(!docParent.canWrite()) return -1;
					return docParent.renameTo(new_Folder.getName())?0:-233;
				}
			}
			else{//kitkat coder killer
				/*https://forum.xda-developers.com/showthread.php?t=2634840*/
				if (new_Folder.exists() && !new_Folder.isDirectory()) return 0;
				if(file.renameTo(new_Folder))
					return 0;
				//AntientLegacyMakeDirs(context.getContentResolver(), filePath);
				return new_Folder.exists()?0:-103;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file.renameTo(new_Folder) ?0:-6;
	}

    public static int mkdir5(Context context, File new_Folder, boolean bCreateFileButNotFolder) {
        String filePath = new_Folder.getAbsolutePath();
        String fnParent = new_Folder.getParentFile().getAbsolutePath();
        try {
            if(bCreateFileButNotFolder) {
                if(new_Folder.getParentFile()!=null)
                    if(new_Folder.getParentFile().isDirectory() || new_Folder.getParentFile().mkdirs())
                        if(new_Folder.createNewFile()) return 0;
            }
            else if(new_Folder.mkdir()) return 0;
        } catch (Exception e) {}

        //if(!filePath.startsWith("/sdcard/") && !filePath.startsWith("/storage/emulated/0/"))
        try {
            if(bKindButComplexSdcardAvailable) {
                StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                boolean isPrimary = false; String uuid = null;
                if (bGoodStorageAvailable) {
                    StorageVolume sv = sm != null ? sm.getStorageVolume(new_Folder) : null;
                    if (sv != null) {
                        isPrimary = sv.isPrimary();
                        uuid = sv.getUuid();
                    }
                }
                if (uuid == null) {//反射大法
                    try {
                        if (csw.init()) {
                            Object[] results = (Object[]) csw.getVolumeList.invoke(sm);
                            if (results != null) {
                                for (Object rI : results) {
                                    isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                    String path = (String) csw.getPath.invoke(rI);
                                    if (filePath.startsWith(path = new File(path).getAbsolutePath())) {
                                        uuid = (String) csw.getUuid.invoke(rI);
                                        filePath = filePath.substring(path.length());
                                        fnParent = fnParent.substring(path.length());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (bGoodStorageAvailable) {
                    int index = filePath.indexOf(uuid);
                    if (index != -1) {
                        filePath = filePath.substring(index + uuid.length());
                        fnParent = fnParent.substring(index + uuid.length());
                    }
                }
				StringBuilder url = new StringBuilder(256);
				Uri uri_start;
                url = new StringBuilder(256);
                url.setLength(0);
                url.append(DOCUMENTTREEURIBASE)
                        .append(uuid).append(COLON).append(DOCUMENT).append(uuid).append(COLON);

                url.append(fnParent.replace("/", SLANT)/*URLEncoder.encode(fnParent,"utf8")*/);
                uri_start = Uri.parse(url.toString());

                DocumentFile docParent = DocumentFile.fromTreeUri(context, uri_start);
                if (docParent == null) return -234;
                if(!docParent.canWrite()) return -1;
                DocumentFile result = docParent == null ? docParent : docParent.createDirectory(new_Folder.getName());
                if (result == null) return -233;

                return 0;
            }
            else{//kitkat coder killer
                /*https://forum.xda-developers.com/showthread.php?t=2634840*/
                if (new_Folder.exists() && new_Folder.isDirectory()) return 0;
                AntientLegacyMakeDirs(context.getContentResolver(), filePath);
                return new_Folder.exists()?0:-103;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -6;
    }

    public static int mkdirs5(Context context, File new_Folder, boolean bCreateFileButNotFolder) {
        String filePath = new_Folder.getAbsolutePath();
        try {
            if(bCreateFileButNotFolder) {
                if(new_Folder.getParentFile()!=null)
                    if(new_Folder.getParentFile().mkdirs())
                        if(new_Folder.createNewFile()) return 0;
            }
            else if(new_Folder.mkdir()) return 0;
        } catch (Exception e) {}

		//if(!filePath.startsWith("/sdcard/") && !filePath.startsWith("/storage/emulated/0/"))
        try {
            if(bKindButComplexSdcardAvailable){
                StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                boolean isPrimary=false;  String uuid=null;
                if (bGoodStorageAvailable) {
                    StorageVolume sv = sm!=null?sm.getStorageVolume(new_Folder):null;
                    if(sv!=null){ isPrimary=sv.isPrimary(); uuid = sv.getUuid(); }
                }
                if(uuid==null){//反射大法
                    try {
                        if(csw.init()){
                            Object []results=(Object[]) csw.getVolumeList.invoke(sm);
                            if(results!=null) {
                                for (Object rI : results) {
                                    isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                    String path=(String) csw.getPath.invoke(rI);
                                    if(filePath.startsWith(path=new File(path).getAbsolutePath())){
                                        uuid=(String) csw.getUuid.invoke(rI);
                                        filePath=filePath.substring(path.length());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                String url = DOCUMENTTREEURIBASE + uuid + COLON + DOCUMENT + uuid + COLON;
                Uri uri_start = Uri.parse(url);
                if(bGoodStorageAvailable){
                    int index = filePath.indexOf(uuid);
                    if (index != -1) {
                        filePath = filePath.substring(index + uuid.length());
                    }
                }
                if(Build.VERSION.SDK_INT >= 21){
                    String[] list = filePath.split("/");
                    String MIME;
                    int cc = 0;
                    boolean permissionchecked=false;
                    for (String dir : list) {
                        MIME = DocumentsContract.Document.MIME_TYPE_DIR;
                        if (bCreateFileButNotFolder)
                            if (cc == list.length - 1)
                                MIME = DocumentsContract.Document.COLUMN_MIME_TYPE;
                        url += "%2F" + dir;
                        DocumentFile doc = DocumentFile.fromSingleUri(context, Uri.parse(url));
                        if(!permissionchecked){
                            if(!doc.canWrite()) return -1; permissionchecked=true;
                        }
                        if (doc.exists()) {
                            uri_start = Uri.parse(url);
                        } else
                            uri_start = DocumentsContract.createDocument(context.getContentResolver(), uri_start, MIME, dir);
                        if (uri_start == null) return -101;
                        cc++;
                    }
                    return uri_start == null ? -100 : 1;
                }
            }
            else{
                if (new_Folder.exists() && new_Folder.isDirectory()) return 0;
                AntientLegacyMakeDirs(context.getContentResolver(), filePath);
                return new_Folder.exists()?0:-103;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -6;
	}

    @Deprecated
    private static void AntientLegacyMakeDirs(ContentResolver contentResolver, String filePath) throws FileNotFoundException {
        ContentValues values = new ContentValues();
        Uri uri;
        values.put(MediaStore.Files.FileColumns.DATA, filePath + "/temp.jpg");
        try {
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            contentResolver.openOutputStream(uri);
            contentResolver.delete(uri, null, null);
        } catch (Exception e) {
            String where = MediaStore.MediaColumns.DATA + "=?";
            String[] selectionArgs = new String[] { filePath+ "/temp.jpg" };
            contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            contentResolver.openOutputStream(uri);
            contentResolver.delete(uri, null, null);
        }
    }

    public static final int checkSdcardPermission3(Context context, File new_Folder) {
        if(!new_Folder.getAbsolutePath().startsWith("/sdcard/") && !new_Folder.getAbsolutePath().startsWith("/storage/emulated/0/"))
            try {
                StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
                StorageVolume sv = sm.getStorageVolume(new_Folder);
                if (sv == null) return -3;
                if(!sv.isPrimary()) {
                    String fn = new_Folder.getAbsolutePath();
                    String uuid = sv.getUuid();
                    if (uuid!=null){
                        StringBuilder url = new StringBuilder("content://com.android.externalstorage.documents/tree/")
                                .append(uuid).append("%3A").append("/document/").append(uuid).append("%3A");

                        Uri uri_start = Uri.parse(url.toString());
                        //long stst=System.currentTimeMillis();
                        //int ret = DocumentsContract.deleteDocument(context.getContentResolver(), uri_start) ? 0 : -1;//DocumentFile.fromSingleUri(context, uri_start).delete()?0:-1;//
                        //CMNF.Log( System.currentTimeMillis()-stst,"DocumentsContract.deleteDocument");
                        DocumentFile doc = DocumentFile.fromSingleUri(context, uri_start);
                        //Log.e("XXX2", doc.getUri().toString());
                        if(!doc.canWrite()) {
                            if(context instanceof Activity) {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                ((Activity) context).startActivityForResult(intent, 700);
                            }
                            return -1;
                        }else {
                            return 0;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

    public static final boolean bGoodStorageAvailable=Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    public static final boolean bKindButComplexSdcardAvailable =Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static CrudeStorageWork csw=new CrudeStorageWork();

    /**If lack permission to ext-sdcard, will call ((Activity)context).startActivityForResult(intent, 700)*/
    public static int delete3(Context context, File file) {
        if(!file.exists())
            return 0;
        String filePath = file.getAbsolutePath();
        if(!filePath.startsWith("/sdcard/") && !filePath.startsWith("/storage/emulated/0/") && !filePath.startsWith("/data"))
        try {
            if(bKindButComplexSdcardAvailable){
                StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
                boolean isPrimary=false;  String uuid=null;
                if (bGoodStorageAvailable) {
                    StorageVolume sv = sm!=null?sm.getStorageVolume(file):null;
                    if(sv!=null){ isPrimary=sv.isPrimary(); uuid = sv.getUuid(); }
                }
                if(uuid==null){//反射大法
                    try {
                        if(csw.init()){
                            Object []results=(Object[]) csw.getVolumeList.invoke(sm);
                            if(results!=null) {
                                for (Object rI : results) {
                                    isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                    String path=(String) csw.getPath.invoke(rI);
                                    if(filePath.startsWith(path=new File(path).getAbsolutePath())){
                                        //CMNF.Log("歪打正着殊途同归龟孙勾苟", filePath, path, rI.getClass().getName(), uuid);
                                        uuid=(String) csw.getUuid.invoke(rI);
                                        filePath=filePath.substring(path.length());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                if(!isPrimary) {
                    if (uuid!=null){
                        if(bGoodStorageAvailable) {
                            int index = filePath.indexOf(uuid);
                            if (index != -1)
                                filePath = filePath.substring(index + uuid.length());
                            else return -22;
                        }
                        StringBuilder url = new StringBuilder("content://com.android.externalstorage.documents/tree/")
                                .append(uuid).append("%3A").append("/document/").append(uuid).append("%3A");
                        url.append(filePath.replace("/","%2F"));
                        Uri uri_start = Uri.parse(url.toString());
                        DocumentFile doc = DocumentFile.fromSingleUri(context, uri_start);
                        if(doc==null){//api testing
                            //CMNF.Log("debugging",doc.canWrite(),doc.exists());
                            return -110;
                        }
                        if(!doc.canWrite()) {
                            if(context instanceof Activity) {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                ((Activity) context).startActivityForResult(intent, 700);
                            }
                            return -1;
                        }else {
                            return doc.delete()?0:-5;
                        }
                    }
                }
            }
            else{
                AncientLegacyRecurseDeleteFiles(context.getContentResolver(), file);
                return file.exists()?-103:0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		try {
			SimpleRecurseDeleteFiles(file);
			return 0;
		} catch (Exception e) {
			return -6;
		}
	}

    private static void SimpleRecurseDeleteFiles(File file) {
        if(file.isDirectory()){
            File[] lst = file.listFiles();
            if(lst!=null && lst.length>0){
                for (File fI:lst) {
					SimpleRecurseDeleteFiles(fI);
                }
            }
        }
		file.delete();
        if(file.exists())
        	throw new IllegalStateException();
    }

    private static void AncientLegacyRecurseDeleteFiles(ContentResolver contentResolver, File file) {
        if(file.isDirectory()){
            File[] lst = file.listFiles();
            if(lst!=null && lst.length>0){
                for (File fI:lst) {
                    AncientLegacyRecurseDeleteFiles(contentResolver, fI);
                }
            }
        }
        String where = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[] { file.getAbsolutePath() };

        // Delete the entry from the media database. This will actually delete media files (images, audio, and video).
        contentResolver.delete(MediaStore.Files.getContentUri("external"), where, selectionArgs);

        if (file.exists()) {
            // If the file is not a media file, create a new entry suggesting that this location is an image, even
            // though it is not.
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath());
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Delete the created entry, such that content provider will delete the file.
            contentResolver.delete(MediaStore.Files.getContentUri("external"), where, selectionArgs);
        }
    }


    public static String DOCUMENTTREEURIBASE="content://com.android.externalstorage.documents/tree/";
    public static String COLON="%3A", SLANT="%2F", DOCUMENT="/document/";
    public static int move3(Context context, File file, File dest) {
        if(file.equals(dest)) return 0;
        //if(false)try {
        //    if(file.getCanonicalPath().equals(dest.getCanonicalPath())) return 0;
        //} catch (IOException e1) { e1.printStackTrace(); return -111; }
        if(!file.exists()) return 222;
        if(FU.exsists(context, dest)) return 233;
        boolean doit=false;
        String destPath=dest.getAbsolutePath(), filePath=file.getAbsolutePath();
        if(!hasRootPermission()) {
            doit |= !destPath.startsWith("/sdcard/") && !destPath.startsWith("/storage/emulated/0/");
            doit |= !filePath.startsWith("/sdcard/") && !filePath.startsWith("/storage/emulated/0/");
        }else{//调用 shell 命令移动文件
			try {
				ExeCommand cmd = new ExeCommand().disPosable().root();
				int ret=cmd.run("mv "+"\""+file+"\" \""+dest+"\"\n"
						,"exit\n"
						,"exit\n"
				);
				if(FU.exsists(context, dest)) return 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        if(doit) try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            DocumentFile doc = null;
            DocumentFile target = null;
            DocumentFile docParent = null;
            DocumentFile targetParent = null;
            boolean moved = false;
            StringBuilder url = new StringBuilder(256);
            if(Build.VERSION.SDK_INT >= 0){//faster via DocumentsContract method
                Uri uri_start;
                //![0] locating source file on the ext-sdcard
                boolean isPrimary=false;  String uuid=null;
                String fnParent = file.getParentFile().getAbsolutePath();
                if (bGoodStorageAvailable) {
                    StorageVolume sv = sm!=null?sm.getStorageVolume(file):null;
                    if(sv!=null){ isPrimary=sv.isPrimary(); uuid = sv.getUuid(); }
                }
                if(uuid==null){//反射大法
                    try {
                        if(csw.init()){
                            Object []results=(Object[]) csw.getVolumeList.invoke(sm);
                            if(results!=null) {
                                for (Object rI : results) {
                                    isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                    String path=(String) csw.getPath.invoke(rI);
                                    if(filePath.startsWith(path=new File(path).getAbsolutePath())){
                                        uuid=(String) csw.getUuid.invoke(rI);
                                        filePath=filePath.substring(path.length());
                                        fnParent=fnParent.substring(path.length());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                if (!isPrimary) {
                    if (uuid != null) {
                        if(bGoodStorageAvailable){
                            int index = filePath.indexOf(uuid);
                            if (index != -1) {
                                filePath = filePath.substring(index + uuid.length());
                                fnParent = fnParent.substring(index + uuid.length());
                            } else return -22;
                        }
                        url.setLength(0); url.append(DOCUMENTTREEURIBASE)
                                .append(uuid).append(COLON).append(DOCUMENT).append(uuid).append(COLON);
                        int baseLen = url.length();
                        url.append(filePath.replace("/", SLANT)/*URLEncoder.encode(filePath,"utf8")*/ );
                        uri_start = Uri.parse(url.toString());
                        doc = DocumentFile.fromSingleUri(context, uri_start);

                        url.setLength(baseLen);
                        url.append(fnParent.replace("/", SLANT)/*URLEncoder.encode(fnParent,"utf8")*/ );
                        uri_start = Uri.parse(url.toString());
                        docParent = DocumentFile.fromSingleUri(context, uri_start);
                    }
                }

                //![1] locating target file on the ext-sdcard
                isPrimary=false;  uuid=null;
                fnParent = dest.getParentFile().getAbsolutePath();
                if (bGoodStorageAvailable) {
                    StorageVolume sv = sm!=null?sm.getStorageVolume(file):null;
                    if(sv!=null){ isPrimary=sv.isPrimary(); uuid = sv.getUuid(); }
                }
                if(uuid==null){//反射大法
                    try {
                        if(csw.init()){
                            Object []results=(Object[]) csw.getVolumeList.invoke(sm);
                            if(results!=null) {
                                for (Object rI : results) {
                                    isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                    String path=(String) csw.getPath.invoke(rI);
                                    if(destPath.startsWith(path=new File(path).getAbsolutePath())){
                                        uuid=(String) csw.getUuid.invoke(rI);
                                        destPath=destPath.substring(path.length());
                                        fnParent=fnParent.substring(path.length());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
                if (!isPrimary) {
                    if (uuid != null) {
                        if(bGoodStorageAvailable) {
                            int index = destPath.indexOf(uuid);
                            if (index != -1) {
                                destPath = destPath.substring(index + uuid.length());
                                fnParent = fnParent.substring(index + uuid.length());
                            } else return -22;
                        }
                        url.setLength(0); url.append(DOCUMENTTREEURIBASE)
                                .append(uuid).append(COLON).append(DOCUMENT).append(uuid).append(COLON);
                        url.append(fnParent.replace("/", SLANT)/*URLEncoder.encode(fnParent,"utf8")*/);
                        uri_start = Uri.parse(url.toString());
                        targetParent = DocumentFile.fromTreeUri(context, uri_start);
                    }
                }
                if (doc == null){ doc = DocumentFile.fromFile(file); docParent = DocumentFile.fromFile(file.getParentFile());}
                if (docParent == null) docParent = DocumentFile.fromFile(dest.getParentFile());
                //![2] perform the move
                if(bGoodStorageAvailable)
                    moved = DocumentsContract.moveDocument(context.getContentResolver(), doc.getUri(), docParent.getUri(), targetParent.getUri()) != null;
                else{
                    target = targetParent.createFile(null, dest.getName());//抛出UnsupportedOperationException for single uri
                    if(target==null)
                        return -53;
                    OutputStream outStream;
                    InputStream inStream;
                    outStream=context.getContentResolver().openOutputStream(target.getUri());
                    inStream=context.getContentResolver().openInputStream(doc.getUri());
                    byte[] buffer=new byte[1024*4];
                    int len;
                    while((len=inStream.read(buffer))>0) {
                        outStream.write(buffer,0,len);
                    }
                    outStream.flush();
                    outStream.close();
                    inStream.close();
                    dest.setLastModified(System.currentTimeMillis());
                    CMNF.Log("ASDSADSAD",target.length(), dest.lastModified());
                    //moved=doc.delete();
                    //file.delete();
                }
            }
            return moved? -100 : 0;
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        dest.getParentFile().mkdirs();
        return file.renameTo(dest)==true?0:-6;
    }

    public static class CrudeStorageWork{
        public Method getVolumeList;
        public Method getIsPrimary;
        public Method getPath;
        public Method getUuid;
        boolean initialized=false;
        /** @return boolean whether <b>Crude Storage Work</b> is ready to use or not*/
        public boolean init(){
            if(getUuid==null){
                try {
                    getVolumeList = StorageManager.class.getMethod("getVolumeList");
                    getVolumeList.setAccessible(true);
                    Class<?> svclazz = Class.forName("android.os.storage.StorageVolume");
                    getIsPrimary = svclazz.getMethod("isPrimary");
                    getPath = svclazz.getMethod("getPath");
                    getUuid=svclazz.getMethod("getUuid");
                    initialized=true;
                } catch (Exception e) { e.printStackTrace(); }
            }
            return getUuid!=null;
        }
    }


    /**
     * Method checks whether the Support Library has been imported by application
     * or not.
     *
     * @return A boolean notifying value wheter support library is imported as a
     * dependency or not.
     */
    private boolean hasSupportLibraryInClasspath() {
        try {
            Class.forName("com.android.support:appcompat-v7");
            return true;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }



	public static void printStorage(Activity context) {
        try {
            StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumList=StorageManager.class.getMethod("getVolumeList");
            getVolumList.setAccessible(true);
            Object []results=(Object[]) getVolumList.invoke(sm);
            if(results!=null){
                for(Object result:results){
                    Method mRemoveable=result.getClass().getMethod("isRemovable");
                    Boolean isRemovable=(Boolean) mRemoveable.invoke(result);
                    if(isRemovable){
                        Method getPath=result.getClass().getMethod("getPath");
                        String path=(String) getPath.invoke(result);
	                	Log.e("fatal storages", ""+path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return;
	}

    private static Method mRemoveable,mGetVolumList,mGetPath;


    public static int rename(Activity context, File file, File dest) {
		//Log.e("XXX2", ""+file.exists());
		if(!file.exists())
			return 222;
		if(FU.exsists(context, dest))
			return 233;
		if(!file.getAbsolutePath().startsWith("/sdcard/") && !file.getAbsolutePath().startsWith("/storage/emulated/0/"))
		try {
            StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumList=StorageManager.class.getMethod("getVolumeList");
            getVolumList.setAccessible(true);
            Object []results=(Object[]) getVolumList.invoke(sm);
            if(results!=null){
                for(Object result:results){
                    Method mRemoveable=result.getClass().getMethod("isRemovable");
                    Boolean isRemovable=(Boolean) mRemoveable.invoke(result);
                    if(isRemovable){
                        Method getPath=result.getClass().getMethod("getPath");
                        String path=(String) getPath.invoke(result);
	                	//Log.e("XXX", ""+path);
                        if(file.getAbsolutePath().startsWith(new File(path).getAbsolutePath())) {
                        	if(!file.getParent().equals(dest.getParent()))
                        		return -123;
                        	//if(Build.VERSION.SDK_INT>=24)
                        	///Log.e("XXX", ""+path);
                        	StorageVolume sv = (StorageVolume) sm.getStorageVolume(file);
                        	if(sv==null)
                        		return -3;
                        	DocumentFile doc = DocumentFile.fromTreeUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/"+sv.getUuid()+"%3A"));
                        	//Log.e("XXX2", doc.getUri().toString());
                        	if(!doc.canWrite()) {
                            	return -1;
                        	}else {
                        		String sequencer=file.getAbsolutePath().substring(path.length()+1);
                        		String[] list = sequencer.split("/");
                        		//Log.e("XXX2", sequencer+list.length);
                        		for(String dir:list) {
                        			doc = doc.findFile(dir);
                            		if(doc==null) return -51;
                        		}
                        		if(doc==null) return -5;
                        		Log.e("XXX-filepath3", dest.getName());
                        		boolean ret = doc.renameTo(dest.getName());
                        		return ret==false&&!dest.exists()?-100:0;
                        	}
                		}
                    }
                }
        		return -4;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return file.renameTo(dest)==true?0:-6;
	}


    public static int move(Context context, File file, File dest) {
		//Log.e("XXX2", ""+file.exists());
		if(file.equals(dest))
			return 0;
		try {
			if(file.getCanonicalPath().equals(dest.getCanonicalPath()))
				return 0;
		} catch (IOException e1) {
			e1.printStackTrace();
			return -111;
		}
		if(!file.exists())
			return 222;
		if(FU.exsists(context, dest))
			return 233;
		boolean doit=false;
		if(!hasRootPermission()) {
			doit |= !dest.getAbsolutePath().startsWith("/sdcard/") && !dest.getAbsolutePath().startsWith("/storage/emulated/0/");
			doit |= !file.getAbsolutePath().startsWith("/sdcard/") && !file.getAbsolutePath().startsWith("/storage/emulated/0/");
		}
		if(doit)
		try {
            StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumList=StorageManager.class.getMethod("getVolumeList");
            getVolumList.setAccessible(true);
            Object []results=(Object[]) getVolumList.invoke(sm);
            //Log.e("XXX-", ""+results.length);
            DocumentFile doc = null;
        	DocumentFile target = null;
            if(results!=null){
                for(Object result:results){
                    Method mRemoveable=result.getClass().getMethod("isRemovable");
                    Boolean isRemovable=(Boolean) mRemoveable.invoke(result);
                    if(isRemovable){
                        Method getPath=result.getClass().getMethod("getPath");
                        String path=(String) getPath.invoke(result);
	                	//Log.e("XXX-", ""+path);
                        if(file.getAbsolutePath().startsWith(new File(path).getAbsolutePath())) {
                        	//if(!dest.getAbsolutePath().startsWith(new File(path).getAbsolutePath()))
                        	//	return -234;
                        	//if(Build.VERSION.SDK_INT>=24)
                        	//Log.e("XXX+", ""+path);
                        	StorageVolume sv = (StorageVolume) sm.getStorageVolume(file);
                        	if(sv==null)
                        		return -3;

                        	doc = DocumentFile.fromTreeUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/"+sv.getUuid()+"%3A"));

                        	if(!doc.canWrite()) {
                            	return -1;
                        	}else {
                        		String sequencer=file.getAbsolutePath().substring(path.length()+1);
                        		String[] list = sequencer.split("/");if(list.length==0) return -511;
                        		//Log.e("XXX2", sequencer+list.length);
                        		for(String dir:list) {
                        			doc = doc.findFile(dir);
                            		if(doc==null) return -51;
                        		}
                        		if(doc==null) return -5;
                    		}
                		}
                        if(dest.getAbsolutePath().startsWith(new File(path).getAbsolutePath())) {
                        	//if(!dest.getAbsolutePath().startsWith(new File(path).getAbsolutePath()))
                        	//	return -234;
                        	//if(Build.VERSION.SDK_INT>=24)
                        	//Log.e("XXX++", ""+path);
                        	StorageVolume sv = (StorageVolume) sm.getStorageVolume(dest);
                        	if(sv==null)
                        		return -3;

                        	target = DocumentFile.fromTreeUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/"+sv.getUuid()+"%3A"));

                        	if(!target.canWrite()) {
                            	return -1;
                        	}else {
                        		String sequencer=dest.getAbsolutePath().substring(path.length()+1);
                        		String[] list = sequencer.split("/");if(list.length==0) return -522;
                        		String last=list.length>0?list[list.length-1]:null;
                        		for(String dir:list) {
                        			if(dir==last)
                        				break;
                        			target = target.findFile(dir);
                            		if(target==null) return -52;
                        		}
                        		if(target==null) return -5;
                    		}
                		}
                    }
                }
        		//return -4;
            }
            if(doc!=null || target!=null) {
            	//Log.e("XXX-fin",(doc!=null)+":"+(target!=null));
            	if(doc==null)
            		doc=DocumentFile.fromFile(file);
        		if(target==null)
        			target=DocumentFile.fromFile(dest.getParentFile());
				if(file.isDirectory()) {
	    			//target = target.createDirectory(last);
	    			//return (doc.delete()||target!=null)?0:-110;
                    return -7;
	    		}else {
	        		boolean moved = false;
	        		if(Build.VERSION.SDK_INT>=24)
	        			moved = DocumentsContract.moveDocument(context.getContentResolver(), doc.getUri(), doc.getParentFile().getUri(), target.getUri())!=null;
	        		else {
	        			target = target.createFile(null, dest.getName());
	        			if(target==null)
	        				return -53;
	        			OutputStream outStream;
	        			InputStream inStream;
	    				outStream=context.getContentResolver().openOutputStream(target.getUri());
	    				inStream=context.getContentResolver().openInputStream(doc.getUri());
	    				byte[] buffer=new byte[1024*4];
	    				int len;
	        			while((len=inStream.read(buffer))>0) {
	        				outStream.write(buffer,0,len);
	        			}
	        			outStream.flush();
	        			outStream.close();
	        			inStream.close();
	        			moved=doc.delete();
	        			file.delete();
	        		}
	        		return moved&&!dest.exists()?-100:0;
	    		}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		dest.getParentFile().mkdirs();
		return file.renameTo(dest)==true?0:-6;
	}


    public static boolean exsists(Context context, File file) {
        String filePath = file.getAbsolutePath();
	    if(!filePath.startsWith("/sdcard/") && !filePath.startsWith("/storage/emulated/0/"))
		try {
            StorageManager sm=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            boolean isPrimary=false; String uuid=null;
            if (bGoodStorageAvailable) {
                StorageVolume sv = sm!=null?sm.getStorageVolume(file):null;
                if(sv!=null){ isPrimary=sv.isPrimary(); uuid = sv.getUuid(); }
            }
            if(uuid==null){//反射大法
                try {
                    if(csw.init()){
                        Object []results=(Object[]) csw.getVolumeList.invoke(sm);
                        if(results!=null) {
                            for (Object rI : results) {
                                isPrimary = (boolean) csw.getIsPrimary.invoke(rI);
                                String path=(String) csw.getPath.invoke(rI);
                                if(filePath.startsWith(path=new File(path).getAbsolutePath())){
                                    uuid=(String) csw.getUuid.invoke(rI);
                                    filePath=filePath.substring(path.length());
                                }
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            if(!isPrimary){
                if(uuid!=null) {
                    if(bGoodStorageAvailable) {
                        int index = filePath.indexOf(uuid);
                        if (index != -1)
                            filePath = filePath.substring(index + uuid.length());
                        else return false;
                    }

                    StringBuilder url = new StringBuilder("content://com.android.externalstorage.documents/tree/")
                            .append(uuid).append("%3A").append("/document/").append(uuid).append("%3A");
                    url.append(filePath.replace("/","%2F"));
                    Uri uri_start = Uri.parse(url.toString());
                    DocumentFile doc = DocumentFile.fromSingleUri(context, uri_start);

                    return doc.exists();
                    //DocumentFile doc = DocumentFile.fromTreeUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                    //if(!doc.canWrite()) {
                    //}else {
                    //    String sequencer=file.getAbsolutePath().substring(filePath.length()+1);
                    //    String[] list = sequencer.split("/");
                    //     for(String dir:list) {
                    //        doc = doc.findFile(dir);
                    //        if(doc==null) return false;
                    //    }
                    //    if(doc!=null) {
                    //        return doc.exists();
                    //    }
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return file.exists();
	}

	public static boolean hasRootPermission() {
    	if(!FilePickerOptions.getRoot())
    		return false;
	   Process process = null;
	   DataOutputStream os = null;
	   try {

	    //Log.i("roottest", "try it");
	    String cmd = "touch /data/datafolder";
	       process = Runtime.getRuntime().exec("su"); //切换到root帐号
	       os = new DataOutputStream(process.getOutputStream());
	       os.writeBytes(cmd + "\n");
	       os.writeBytes("exit\n");
	       os.flush();
	       process.waitFor();
	   } catch (Exception e) {
	       return false;
	   } finally {
	       try {
	           if (os != null) {
	               os.close();
	           }
	           process.destroy();
	       } catch (Exception e) {
	       }
	   }
	   return true;
	}

    public static boolean startsWith(CharSequence input, CharSequence prefix, int toffset) {
        int to = toffset;
        int po = 0;
        int pc = prefix.length();
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > input.length() - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (input.charAt(to++) != prefix.charAt(po++)) {
                return false;
            }
        }
        return true;
    }


	public static boolean startsWith(CharSequence input, CharSequence prefix, int toffset, int length) {

		Log.d("fatal startsWith", input + " vs "+prefix.subSequence(toffset, length));

		int to = toffset;
        int po = 0;
        int pc = Math.min(length, prefix.length());
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > input.length() - pc)) {
            return false;
        }
        while (--pc >= 0) {
        	//if(po>=length) return true;
            if (input.charAt(to++) != prefix.charAt(po++)) {
                return false;
            }
        }
        return true;
	}


    public static List<String> list(File f,FilenameFilter filter) {
        String names[] = f.list();
        if ((names == null) || (filter == null)) {
            return new ArrayList<>(Arrays.asList(names));
        }
        List<String> v = new ArrayList<>();
        for (int i = 0 ; i < names.length ; i++) {
            if (filter.accept(f, names[i])) {
                v.add(names[i]);
            }
        }
        return v;//.toArray(new String[v.size()])
    }




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Uri[] listFiles(Context context, Uri self) {


        final ContentResolver resolver = context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(self,
                DocumentsContract.getDocumentId(self));
        final ArrayList<Uri> results = new ArrayList<Uri>();
        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            while (c.moveToNext()) {
                final String documentId = c.getString(0);
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(self,
                        documentId);
                results.add(documentUri);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            Log.w("asdasd", "Failed query: " + e);
        } finally {
        	if(c!=null)
        		c.close();
        }
        return results.toArray(new Uri[results.size()]);
    }









    /** handling disgusting android stuffs
     * see https://stackoverflow.com/questions/17546101/get-real-path-for-uri-android */
    public static String getPathFromUri(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /** Check Whether the Uri authority is ExternalStorageProvider. */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /** Check Whether the Uri authority is DownloadsProvider. */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /** Check Whether the Uri authority is MediaProvider. */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /** Check Whether the Uri authority is Google Photos.*/
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
