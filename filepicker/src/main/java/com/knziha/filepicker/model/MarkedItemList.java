/*
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

package com.knziha.filepicker.model;

import com.knziha.filepicker.view.FileListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**<p>
 * Created by Angad Singh on 11-07-2016.
 * </p>
 */
//蛋疼  DangleTon

/*  SingleTon containing <Key,Value> pair of all the selected files.
 *  Key: Directory/File path.
 *  Value: FileListItem Object.
 */
public class MarkedItemList {
    private static HashMap<String, FileListItem> ourInstance = new HashMap<>();

    public static FileListItem addSelectedItem(FileListItem item) {
        return ourInstance.put(item.getLocation(),item);
    }
	public static void addALLFile(List<FileListItem> l) {
		for(FileListItem fi:l) {
			if(!fi.isDirectory())
				ourInstance.put(fi.getLocation(), fi);
		}
	}
	public static void addALL(List<FileListItem> l) {
		for(FileListItem fi:l) {
			ourInstance.put(fi.getLocation(), fi);
		}
	}
	public static void removeAll(ArrayList<FileListItem> l) {
		for(FileListItem fi:l) {
			ourInstance.remove(fi.getLocation());
		}
	}
	
	public static void removeAllByLoc(ArrayList<String> l) {
		for(String fnI:l) {
			ourInstance.remove(fnI);
		}
	}

    public static FileListItem removeSelectedItem(String key) {
        return ourInstance.remove(key);
    }

    public static boolean hasItem(String key) {
        return ourInstance.containsKey(key);
    }

    public static void clearSelectionList() {
        ourInstance = new HashMap<>();
    }

    public static void addSingleFile(FileListItem item) {
        ourInstance = new HashMap<>();
        ourInstance.put(item.getLocation(),item);
    }

    public static ArrayListTree<String> getSelectedPaths() {
    	ArrayListTree<String> ret = new ArrayListTree<>();
        Set<String> paths = ourInstance.keySet();
        for(String path:paths)
        {   ret.insert(path);
        }
        return ret;
    }

    public static int getFileCount() {
        return ourInstance.size();
    }


    public static Set<Map.Entry<String,FileListItem>> entrySet() {
        return ourInstance.entrySet();
    }
}
