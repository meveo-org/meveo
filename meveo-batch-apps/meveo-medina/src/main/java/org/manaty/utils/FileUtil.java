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
package org.manaty.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.meveo.commons.utils.FileFormat;


/**
 * File utilities class.
 * 
 * @author Donatas Remeika
 * @created Mar 4, 2009
 */
public final class FileUtil {

    private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

    /**
     * No need to create instance.
     */
    private FileUtil() {

    }

    /**
     * Add extension to existing file by renaming it.
     * 
     * @param file
     *        File to be renamed.
     * @param extension
     *        Extension.
     * @return Renamed File object.
     */
    public static File addExtension(File file, String extension) {
        if (file.exists()) {
            String name = file.getName();
            File dest = new File(file.getParentFile(), name + extension);
            if (file.renameTo(dest)) {
                return dest;
            }
        }
        return null;
    }

    /**
     * Move file with an option to overwrite file
     * 
     * @param file
     *        File to move
     * @param toPath
     *        Directory to move the file to
     * @param newName
     *        New file name
     * @param force
     *        Delete existing destination file if existed
     * @return True if file was moved
     */
    public static boolean moveFile(String toPath, File file, String newName, boolean force) {
        File f = new File(toPath);
        f.mkdirs();

        if (newName == null) {
            newName = file.getName();
        }

        if (force) {
            File testFile = new File(toPath + File.separator + newName);
            if (testFile.exists()) {
                testFile.delete();
            }
        }

        return file.renameTo(new File(toPath + File.separator + newName));
    }

    /**
     * Move file
     * 
     * @param file
     *        File to move
     * @param toPath
     *        Directory to move the file to
     * @param newName
     *        New file name
     * @return True if file was moved
     */
    public static boolean moveFile(String toPath, File file, String newName) {
        return moveFile(toPath, file, newName, false);
    }

    /**
     * Replaces file extension with new one.
     * 
     * @param filename
     *        Old filename.
     * @param extension
     *        New extension.
     * @return New Filename.
     */
    public static String replaceFilenameExtension(String filename, String extension) {
        int indexOfExtension = filename.lastIndexOf(".");
        if (indexOfExtension < 1) {
            return filename + extension;
        } else {
            filename = filename.substring(0, indexOfExtension) + extension;
            return filename;
        }
    }

    /**
     * Get file format by file name extension.
     * 
     * @param filename
     *        File name.
     * @return FileFormat enum.
     */
    public static FileFormat getFileFormatByExtension(String filename) {
        int indexOfExtension = filename.lastIndexOf(".");
        if (indexOfExtension == -1) {
            return FileFormat.CSV;
        } else {
            String extension = filename.substring(indexOfExtension + 1);
            return FileFormat.parseFromExtension(extension);
        }
    }

    /**
     * Get File representation ready for parsing.
     * 
     * @param sourceDirectory
     *        Directory to search inside.
     * @return File object.
     */
    public static File getFileForParsing(String sourceDirectory, final List<String> extensions) {
        
        File sourceDir = new File(sourceDirectory);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            logger.info(String.format("Wrong source directory: %s", sourceDir.getAbsolutePath()));
            return null;
        }
        File[] files = sourceDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (extensions == null) {
                    return true;
                }
                for (String extension : extensions) {
                    if (name.endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }

        });
        if (files == null || files.length == 0) {
            return null;
        }

        Arrays.sort(files, new Comparator<File>() {
            // sort files by modification date asc.
            public int compare(File file1, File file2) {
                if (file1.lastModified() < file2.lastModified()) {
                    return -1;
                } else if (file1.lastModified() > file2.lastModified()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (File file : files) {
            if (file.isFile()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Gets directory by name. If it does not exist - creates it.
     * 
     * @param dirName
     *        Directory name
     * @return Directory.
     */
    public static File getOrCreateDirectory(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!dir.isDirectory()) {
            logger.severe(String.format("Wrong directory name: " +dir.getAbsolutePath()));
            return null;
        }
        return dir;
    }
}
