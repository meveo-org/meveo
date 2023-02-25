/**
 * 
 */
package org.meveo.admin.action.catalog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptTemplate {
	private static Logger log = LoggerFactory.getLogger(ScriptTemplate.class);
	
	private String url;
	private String description;
	private String template;
	private String code;
	
	public String getCode() {
		return code;
	}
	/**
	 * @return the {@link #url}
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
		if (url != null) {
			this.code = FilenameUtils.getBaseName(url);
		}
	}
	/**
	 * @return the {@link #description}
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the {@link #template}
	 */
	public String getTemplate() {
		if (template == null) {
			try {
				template = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
			} catch (IOException e) {
				log.error("Failed to donwload template", e);
			}
		}
		return template;
	}
	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	@Override
	public String toString() {
		return "ScriptTemplate [description=" + description + ", code=" + getCode() + "]";
	}
	
}
