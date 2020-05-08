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

package com.knziha.filepicker.view;

import java.io.File;
import java.text.Collator;

/* <p>
 * Created by Angad Singh on 09-07-2016.
 * </p>
 */

/**
 * The model/container class holding file list data.
 */
public class FileListItem implements Comparable<FileListItem> {
    public String filename;
    String location;
    boolean marked;
    int directory=0;
    long time;
    long size;

    public FileListItem(){
        location="";
    }
    public FileListItem(String location_){
        location=location_;
    }
    public FileListItem(File fRaw, boolean readLen){
    	filename=fRaw.getName();
    	location=fRaw.getAbsolutePath();
        if(!fRaw.isDirectory()) {
            size = readLen?fRaw.length():-1;
        }else {
            size = -1;
            directory = 1;
        }
        time = fRaw.lastModified();
    }
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location_) {
        location = location_;
    }

    public boolean isDirectory() {
        return directory>0;
    }

    public void setDirectory(int directory) {
    	this.directory = directory;
    }

	public void setDirectory(boolean directory) {
		this.directory = directory?1:0;
	}
	
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSize(long length) {
        size = length;
    }
    public long getSize(long length) {
        return size;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
    public static int comparation_method;
    @Override
    public int compareTo(FileListItem f2) {
        if(f2.isDirectory()&&!isDirectory()){
            return 1;
        }
        else if(!f2.isDirectory()&&isDirectory()){
            return -1;
        }
    	if(time==-1){
            return -1;
        }
    	if(f2.time==-1)
    		return 1;
        long ret;
        switch(comparation_method){
            case 0://name
            case 1:
                ret = compare_name(filename, f2.filename);
                return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==0?1:-1);
            case 2://TODO type then name
            case 3:
                ret = size - f2.size;
                if (ret == 0)
                    ret = compare_name(filename, f2.filename);
                return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==2?1:-1);
            case 4://size then name
            case 5:
                ret = size - f2.size;
                if (ret == 0)
                    ret = compare_name(filename, f2.filename);
                return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==4?1:-1);
            case 6://time then name
            case 7:
                ret = time - f2.time;
                if (ret == 0)
                    ret = compare_name(filename, f2.filename);
                return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==6?1:-1);
        }
        //if(fileListItem.isDirectory()&&isDirectory())
        //{
        //    return filename.compareToIgnoreCase(fileListItem.getFilename());
        //}
        //else if(!fileListItem.isDirectory()&&!isDirectory())
        //{
        //    return filename.compareToIgnoreCase(fileListItem.getFilename());
        //}
        //else if(fileListItem.isDirectory()&&!isDirectory())
        //{
        //    return 1;
        //}
        //else
        //{
        //    return -1;
        //}
        return 0;
    }

    private int compare_name(String f1, String f2) {
        return f1.compareToIgnoreCase(f2);
        //return Collator.getInstance(java.util.Locale.CHINA).compare(f1,f2);
    }

    @Override
    public boolean equals(Object other) {
    	if(FileListItem.class.isInstance(other))
    		return compareTo((FileListItem) other)==0;
    	else
    		return super.equals(other);
    }
}