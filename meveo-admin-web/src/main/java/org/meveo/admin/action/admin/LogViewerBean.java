package org.meveo.admin.action.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.Messages;
import org.meveo.commons.utils.ParamBean;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.unix4j.Unix4j;
import org.unix4j.unix.grep.GrepOption;
import org.unix4j.unix.grep.GrepOptions;

/**
 * Bean for managing the log viewer.
 * 
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.9.0
 */
@Named
@ViewScoped
public class LogViewerBean implements Serializable {

	private static final long serialVersionUID = 3761175276890871506L;

	@Inject
	private Logger log;

	@Inject
	protected Messages messages;

	private ParamBean paramBean = ParamBean.getInstance();

	private String logFile;

	private Integer offset = 5000;

	private boolean paused = true;

	private boolean initialized;
	
	private String grepOptions;
	
	private String grepPattern;

	public LogViewerBean() {
		super();
	}

	@PostConstruct
	protected void init() {
		String serverLogFile = System.getProperty("jboss.server.log.dir") + File.separator + "server.log";
		logFile = paramBean.getProperty("meveo.log.file", serverLogFile);
		offset = Integer.parseInt(paramBean.getProperty("log.offset", "5000"));
	}

	public void start() {

		try {
			if (logFile != null) {
				this.paused = false;
				this.initialized = true;
				read();
			}

		} catch (Exception e) {
			logFile = null;
			messages.error(e.getLocalizedMessage());
			log.error("Cannot create file tail", e);
		}
	}

	public void stop() {
		this.paused = true;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
		if (!StringUtils.isEmpty(logFile)) {
			paramBean.setProperty("meveo.log.file", logFile);
			paramBean.saveProperties();
		}
	}

	public String getLogFile() {
		return logFile;
	}

	public void read() throws FileNotFoundException, IOException {
		String readValue;
		if (StringUtils.isBlank(grepPattern)) {
			readValue = Unix4j.tail(offset, logFile).toStringResult();
		} else {
			// Parse options
			GrepOptions options = null;
			if (!StringUtils.isBlank(grepOptions)) {
				List<String> optionsAsStrs = Arrays.stream(grepOptions.split(" "))
						.map(optionAsStr -> {
							if (optionAsStr.startsWith("-")) {
								return optionAsStr.substring(1);
							} else if (optionAsStr.startsWith("--")) {
								return optionAsStr.substring(2);
							} else {
								return optionAsStr;
							}
						}).collect(Collectors.toList());
				options = GrepOption.CONVERTER.convert(optionsAsStrs);
			}
			
			if(options == null) {
				options = GrepOptions.EMPTY;
			}
			
			if (offset == null || offset == 0) {
				readValue = Unix4j.grep(options, grepPattern, logFile).toStringResult();
			} else {
				readValue = Unix4j.tail(offset, logFile).grep(options, grepPattern).toStringResult();
			}
		}
		
		PrimeFaces.current().ajax().addCallbackParam("value", readValue);
	}

	public boolean isPaused() {
		return paused;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param grepOptions the grepOptions to set
	 */
	public void setGrepOptions(String grepOptions) {
		this.grepOptions = grepOptions;
	}

	/**
	 * @param grepPattern the grepPattern to set
	 */
	public void setGrepPattern(String grepPattern) {
		this.grepPattern = grepPattern;
	}

	/**
	 * @return the {@link #grepOptions}
	 */
	public String getGrepOptions() {
		return grepOptions;
	}

	/**
	 * @return the {@link #grepPattern}
	 */
	public String getGrepPattern() {
		return grepPattern;
	}
	

}
