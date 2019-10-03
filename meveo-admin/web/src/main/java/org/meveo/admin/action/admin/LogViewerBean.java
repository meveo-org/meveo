package org.meveo.admin.action.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.meveo.commons.utils.ParamBean;
import org.meveo.util.FileTail;

@Named
@ViewScoped
public class LogViewerBean implements Serializable {
	
	private static final long serialVersionUID = 3761175276890871506L;
	
	private ParamBean paramBean = ParamBean.getInstance();
	
	private int bufferSize = 2048;
	
	private String logFile;
	
	private FileTail fileTail = null;
	
	public LogViewerBean() {
		super();
	}

	@PostConstruct
	protected void init() {
		bufferSize = Integer.parseInt(paramBean.getProperty("meveo.log.viewer.buffer", "2048"));
		logFile = paramBean.getProperty("meveo.log.file", null);
		
		try {
			if(logFile != null) {
				fileTail = new FileTail(logFile, bufferSize);
			}
			
		} catch (Exception e) {
			logFile = null;
			FacesContext context = FacesContext.getCurrentInstance();
	        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  "Error", e.getLocalizedMessage()));
		}
 
	}

	public void setBufferSize(int bufferSize) {
		if(this.bufferSize == bufferSize) {
			return;
		}
		
		try { 
			fileTail = new FileTail(fileTail, bufferSize);
			this.bufferSize = bufferSize;
			paramBean.setProperty("meveo.log.viewer.buffer", String.valueOf(bufferSize));
		} catch(Exception e) {
			
		}
	}

	public void setLogFile(String logFile) {
		if(this.logFile != null && this.logFile.equals(logFile)) {
			return;
		}
		
		try { 
			fileTail = new FileTail(fileTail, logFile);
			this.logFile = logFile;
			paramBean.setProperty("meveo.log.file", logFile);
			
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
	        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,  "Error", e.getLocalizedMessage()));
		}
	}
	
	public String getLogFile() {
		return logFile;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}

	public void read() throws FileNotFoundException, IOException {
		if(fileTail != null) {
			fileTail.read();
		}
	}

	public String getValue() {
		if(fileTail == null) {
			return "";
		}
		
		return fileTail.getAsString();
	}

}
