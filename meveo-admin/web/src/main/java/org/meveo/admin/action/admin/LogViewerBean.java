package org.meveo.admin.action.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.Messages;
import org.meveo.commons.utils.ParamBean;
import org.meveo.util.FileTail;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;

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

	private volatile Unix4jCommandBuilder tail;

	private boolean paused = true;

	private boolean initialized;

	public LogViewerBean() {
		super();
	}

	@PostConstruct
	protected void init() {
		String serverLogFile = System.getProperty("jboss.server.log.dir") + File.separator + "server.log";
		logFile = paramBean.getProperty("meveo.log.file", serverLogFile);
		offset = Integer.parseInt(paramBean.getProperty("log.offset", "5000"));
		
		if (logFile != null) {
			tail = Unix4j.tail(offset, logFile);
		}
	}

	public void start() {

		try {
			if (logFile != null) {
				this.paused = false;
				this.initialized = true;
				tail = Unix4j.tail(offset, logFile);
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
		PrimeFaces.current().ajax().addCallbackParam("value", tail.toStringResult());
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

}
