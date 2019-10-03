package org.meveo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Worker class to print the tail of a file.
 */
public class FileTail {
	
	/**
	 * File To watch
	 */
	private final File fileToWatch;
	
	private final StringBuffer buffer;

	/**
	 * Variable used to get the last know position of the file
	 */
	private long lastKnownPosition = 0;

	public FileTail(String fileName) {
		this(fileName, 2048);
	}
	
	public FileTail(FileTail ft, int capacity) {
		buffer = new StringBuffer(capacity);
		fileToWatch = ft.fileToWatch;
		lastKnownPosition = ft.lastKnownPosition;
		buffer.append(ft.buffer);
	}
	
	public FileTail(FileTail ft, String logFile) {
		this(logFile, ft.buffer.capacity());
	}

	public FileTail(String fileName, int capacity) {
		this.fileToWatch = new File(fileName);
		
		if (!fileToWatch.exists()) {
			throw new IllegalArgumentException(fileName + " not exists");
		}
		
		buffer = new StringBuffer(capacity);
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void read() throws FileNotFoundException, IOException {
		long fileLength = fileToWatch.length();

		/**
		 * This case occur, when file is taken backup and new file created with the same name.
		 */
		if (fileLength < lastKnownPosition) {
			lastKnownPosition = 0;
		}
		
		if (fileLength > lastKnownPosition) {
			RandomAccessFile randomAccessFile = new RandomAccessFile(fileToWatch, "r");
			randomAccessFile.seek(lastKnownPosition);
			String line = null;
			while ((line = randomAccessFile.readLine()) != null) {
				buffer.append(line);
			}
			
			lastKnownPosition = randomAccessFile.getFilePointer();
			randomAccessFile.close();
		}
	}
	
	public String getAsString() {
		return buffer.toString();
	}


}