/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.myevo.rating.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	
	public static List<String> getCSVList(String directory){
		List<String> csvList = new ArrayList<String>();
		File dir = new File(directory);

		String[] children = dir.list();
		if(null == children) return new ArrayList<String>();
		for(String currentChild : children){
			if(currentChild.indexOf(".csv") > -1){
				csvList.add(directory + "/" + currentChild);
			} 
		}
		
		return csvList;
	}
	
	public static void removeAllItems(String directory){
		File dir = new File(directory);

		String[] children = dir.list();
		for(String currentChild : children){
			File currentFile = new File(directory + "/" + currentChild);
			currentFile.delete();
		}
	}
	
	public static void copyfile(String sourceFile, String destinationFile) {
		try {
			System.out.println("sourceFile : " + sourceFile);
			System.out.println("destinationFile : " + destinationFile);
			
			File f1 = new File(sourceFile);
			File f2 = new File(destinationFile);
			if(!f2.exists()) f2.createNewFile();
			
			InputStream in = new FileInputStream(f1);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
