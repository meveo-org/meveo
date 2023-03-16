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
import java.util.function.Consumer;

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
			command.add("npm.cmd");
			if (args != null) {
				for(var arg : args) {
					command.add(arg);
				}
			}
		} else {
			command.add("bash");
			command.add("-c");
			command.add("npm " + String.join(" ", args));
		}
	
		return command;
	}
	
	public static int npm(File directory, Consumer<BufferedReader> callback, String... commands) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder()
				.command(npmCmd(commands))
				.directory(directory)
				.redirectErrorStream(true);
		
		try {
			Process process = processBuilder.start();
			
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				if (callback != null) {
					callback.accept(reader);
				}
			}
			
			return process.waitFor();
		} catch (InterruptedException e) {
			return -100;
		}
	}
	
	public static int npmInstallDependencies(File directory) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder()
				.command(npmCmd("install"))
				.directory(directory)
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
	
	public static int npmRun(File directory, String command) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder()
				.command(npmCmd("run", command))
				.directory(directory)
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
		var cmdList = npmCmd("install");
		
		if (args != null && args.length > 0) {
			if (args.length == 1) {
				cmdList.add(args[0]);
			} else {
				cmdList.add(args[0] + "@" + args[1]);
			}
		}
		
		ProcessBuilder processBuilder = new ProcessBuilder()
				.command(cmdList)
				.directory(directory)
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
			.command(npmCmd("init", "-y"))
			.directory(directory)
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
