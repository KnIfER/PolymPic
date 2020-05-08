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

package com.knziha.filepicker.utils;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Locale;

/**<p>
 * Created by Angad Singh on 11-07-2016.
 * </p>
 */

/*  Class to filter the list of files.
 */
public class ExtensionFilter implements FileFilter {
    private HashSet<String> validExtensions;
    private DialogProperties properties;

    public ExtensionFilter(DialogProperties properties) {
        this.validExtensions = properties.extensions;
        this.properties=properties;
    }

    /**Function to filter files based on defined rules.
     */
    @Override
    public boolean accept(File file) {
        //if(true) return true;
        //All directories are added in the least that can be read by the Application
        if (file.isDirectory())//&&file.canRead()
        {   return true;
        }
        else if(properties.locked && properties.selection_type==DialogConfigs.DIR_SELECT)
          return false;
        if(!properties.locked || validExtensions==null)
            return true;
        else
        {   /*  Check whether name of the file ends with the extension. Added if it
             *  does.
             */
            String name = file.getName().toLowerCase(Locale.getDefault());
            int idx = name.lastIndexOf(".");
            if(idx!=-1 && idx<name.length()){
                String ext = name.substring(idx);
                if(validExtensions.contains(ext))
                    return true;
            }if(validExtensions.contains("*")){
                return true;
            }
//            for (String ext : validExtensions) {
//                if (name.endsWith(ext)) {
//                    return true;
//                }
//            }
        }
        return false;
    }


    public boolean accept(String name) {
            name = name.toLowerCase(Locale.getDefault());
            int idx = name.lastIndexOf(".");
            if(idx!=-1 && idx<name.length()){
                String ext = name.substring(idx);
                if(validExtensions.contains(ext))
                    return true;
            }else if(validExtensions.contains("*")){
                return true;
            }
        return false;
    }


}
