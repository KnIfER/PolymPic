package com.knziha.filepicker.utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {
    public int comparation_method;
    public FileComparator(int comparation_method_){
        comparation_method=comparation_method_;
    }
    @Override
    public int compare(File f1,File f2) {
        long ret;
        switch(comparation_method){
            case 0://time then name
            case 1:
                ret = f1.lastModified() - f2.lastModified();
                if (ret == 0)
                    ret = f1.getName().compareTo(f2.getName());
            return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==0?1:-1);
            case 2://name
            case 3:
                ret = f1.getName().compareTo(f2.getName());
            return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==2?1:-1);
            case 4://size then name
            case 5:
                ret = f1.length() - f2.length();
                if (ret == 0)
                    ret = f1.getName().compareTo(f2.getName());
            return (ret == 0 ? 0 : ret > 0 ? 1 : -1) * (comparation_method==4?1:-1);
        }
        return 0;
    }







}
