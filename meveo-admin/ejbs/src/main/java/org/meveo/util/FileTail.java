package org.meveo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Worker class to print the tail of a file.
 */
public class FileTail {
	
	/**
	 * File To watch
	 */
	private final File fileToWatch;
	
	private StringBuffer buffer;
	
	private Integer offset;
	
	private final String fileName;

	/**
	 * Variable used to get the last know position of the file
	 */
	private long lastKnownPosition = 0;

	public FileTail(String fileName, Integer offset) {
		this.fileToWatch = new File(fileName);
		
		if (!fileToWatch.exists()) {
			throw new IllegalArgumentException(fileName + " not exists");
		}
		
		this.offset = offset;
		this.fileName = fileName;
		
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void read() throws FileNotFoundException, IOException {
		
		buffer = new StringBuffer();
		
		/* Store the n first lines starting from the end in the buffer */
		if(offset != null) {
			try(ReverseFileReader reverseFileReader = new ReverseFileReader(fileToWatch)) {
				List<String> lines = new ArrayList<>();
	
				for(int i = 0; i < offset; i++) {
					String line = reverseFileReader.readLine();
					if(line == null) {
						break;
					}
					
					lines.add(line);
				}
				
				Collections.reverse(lines);
				for(String line : lines) {
					buffer.append(line)
						.append("\n");
				}
			}
		}
		
		offset = null;	// Make sure we won't read backward the next time

		long fileLength = fileToWatch.length();

		/* This case occur when file is taken backup and new file created with the same name. */
		if (fileLength < lastKnownPosition) {
			lastKnownPosition = 0;
		}
		
		/* Start at the end of the file */
		if(lastKnownPosition == 0) {
			lastKnownPosition = fileLength;
		}
		
		if (fileLength > lastKnownPosition) {
			RandomAccessFile randomAccessFile = new RandomAccessFile(fileToWatch, "r");
			randomAccessFile.seek(lastKnownPosition);
			String line = null;
			while ((line = randomAccessFile.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}
			
			lastKnownPosition = randomAccessFile.getFilePointer();
			randomAccessFile.close();
		}
	}
	
	public String getAsString() {
		if(buffer == null) {
			return "";
		}
		
		return buffer.toString();
	}

	public String getFileName() {
		return fileName;
	}


}