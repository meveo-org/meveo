/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class MeveoFileUtils {

    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    public static String readString(String filePath) throws IOException {
    	File file = new File(filePath);
    	Charset charset = getCharset(file);
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(file.toPath(), charset)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        return contentBuilder.toString();
    }
    
    public static Charset getCharset(File file) throws IOException {
    	try (var fileStream = new FileInputStream(file)) {
    		try (var fileReader = new InputStreamReader(fileStream)) {
    			return Charset.forName(fileReader.getEncoding());
    		}
    	}
    }
    
    public static void writeAndPreserveCharset(String content, File file) throws IOException {
    	if (!file.getParentFile().exists()) {
    		file.getParentFile().mkdirs();
    	}
    	
    	Charset charset;
    	if (!file.exists()) {
    		file.createNewFile();
    		charset = StandardCharsets.UTF_8;
    	} else {
    		charset = MeveoFileUtils.getCharset(file);
    	}
    	
    	Files.writeString(file.toPath(), content, charset);
    }

}
