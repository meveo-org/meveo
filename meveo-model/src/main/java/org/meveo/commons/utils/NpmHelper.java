/**
 * 
 */
package org.meveo.commons.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmHelper {
	
	private static Logger LOGGER = LoggerFactory.getLogger(NpmHelper.class);
	
	private static boolean isWindows() {
	    return System.getProperty("os.name").toLowerCase().contains("win");
	}
	
	private static List<String> npmCmd(String... args) {
		List<String> command = new ArrayList<>();
		
		if (isWindows()) {
			command.add("npm.cmd " + String.join(" ", args));
		} else {
			command.add("bash");
			command.add("-c");
			command.add("npm " + String.join(" ", args));
		}
	
		return command;
	}
	/**
	 * Run an npm install command on the given directory
	 * 
	 * @param directory directory to execute the npm install
	 * @param args Optional arguments
	 * <ul>
	 * 	<li> 0 = artifact </li>
	 * 	<li> 1 = artifact version </li>
	 * </ul>
	 * @return the process exit code
	 * @throws IOException if the command can't be executed
	 */
	public static int npmInstall(File directory, String... args) throws IOException {
		String cmd = "install";
		if (args != null && args.length > 0) {
			cmd += " " + String.join("@", args);
		}
		
		if (directory != null) {
			cmd += " --prefix " + directory.getAbsolutePath();
		}
		
		ProcessBuilder processBuilder = new ProcessBuilder()
				.command(npmCmd(cmd))
				.redirectErrorStream(true);
		
		Process process = processBuilder.start();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			LOGGER.info(line);
		}
		
		try {
			return process.waitFor();
		} catch (InterruptedException e) {
			return -100;
		}
	}
	
	public static int npmInit(File directory) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder()
			.command(npmCmd("init", "-y", "--prefix " + directory.getAbsolutePath()))
			.redirectErrorStream(true);
		
		Process process = processBuilder.start();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
		    LOGGER.info(line);
		}
		
		try {
			return process.waitFor();
		} catch (InterruptedException e) {
			return -100;
		}
	}
}
