/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.api.rest.technicalservice.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.meveo.commons.utils.StringUtils;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
public class EndpointRequest {

	private HttpServletRequest httpServletRequest;
	private String remainingPath;

	@SuppressWarnings("unused")
	public EndpointRequest(HttpServletRequest httpServletRequest, Endpoint endpoint) {
		this.httpServletRequest = httpServletRequest;

		// Compute remaining path if endpoint exists
		if (endpoint != null) {
			remainingPath = httpServletRequest.getPathInfo();
			remainingPath = remainingPath.replace("/" + endpoint.getCode(), "");
			if (!StringUtils.isBlank(remainingPath)) {
				for (EndpointPathParameter p : endpoint.getPathParametersNullSafe()) {
					final int slashIdx = remainingPath.indexOf("/");
					if (remainingPath.substring(slashIdx + 1).contains("/")) {
						final int secondSlashIdx = remainingPath.substring(slashIdx + 1).indexOf("/") + 1;
						remainingPath = getRemainingPath().substring(secondSlashIdx);
					} else {
						remainingPath = remainingPath.substring(slashIdx);
					}
				}
			}
		}
	}

	public String getRemainingPath() {
		return remainingPath;
	}

	public String getAuthType() {
		return httpServletRequest.getAuthType();
	}

	public Cookie[] getCookies() {
		return httpServletRequest.getCookies();
	}

	public long getDateHeader(String name) {
		return httpServletRequest.getDateHeader(name);
	}

	public String getHeader(String name) {
		return httpServletRequest.getHeader(name);
	}

	public Enumeration<String> getHeaders(String name) {
		return httpServletRequest.getHeaders(name);
	}

	public Enumeration<String> getHeaderNames() {
		return httpServletRequest.getHeaderNames();
	}

	public int getIntHeader(String name) {
		return httpServletRequest.getIntHeader(name);
	}

	public HttpServletMapping getHttpServletMapping() {
		return httpServletRequest.getHttpServletMapping();
	}

	public String getMethod() {
		return httpServletRequest.getMethod();
	}

	public String getPathInfo() {
		return httpServletRequest.getPathInfo();
	}

	public String getPathTranslated() {
		return httpServletRequest.getPathTranslated();
	}

	public String getContextPath() {
		return httpServletRequest.getContextPath();
	}

	public String getQueryString() {
		return httpServletRequest.getQueryString();
	}

	public String getRemoteUser() {
		return httpServletRequest.getRemoteUser();
	}

	public boolean isUserInRole(String role) {
		return httpServletRequest.isUserInRole(role);
	}

	public Principal getUserPrincipal() {
		return httpServletRequest.getUserPrincipal();
	}

	public String getRequestedSessionId() {
		return httpServletRequest.getRequestedSessionId();
	}

	public String getRequestURI() {
		return httpServletRequest.getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return httpServletRequest.getRequestURL();
	}

	public String getServletPath() {
		return httpServletRequest.getServletPath();
	}

	public boolean isRequestedSessionIdValid() {
		return httpServletRequest.isRequestedSessionIdValid();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return httpServletRequest.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return httpServletRequest.isRequestedSessionIdFromURL();
	}

	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return httpServletRequest.isRequestedSessionIdFromUrl();
	}

	public Collection<Part> getParts() throws IOException, ServletException {
		return httpServletRequest.getParts();
	}

	public Part getPart(String name) throws IOException, ServletException {
		return httpServletRequest.getPart(name);
	}

	public Map<String, String> getTrailerFields() {
		return httpServletRequest.getTrailerFields();
	}

	public boolean isTrailerFieldsReady() {
		return httpServletRequest.isTrailerFieldsReady();
	}

	public Object getAttribute(String name) {
		return httpServletRequest.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return httpServletRequest.getAttributeNames();
	}

	public String getCharacterEncoding() {
		return httpServletRequest.getCharacterEncoding();
	}

	public int getContentLength() {
		return httpServletRequest.getContentLength();
	}

	public long getContentLengthLong() {
		return httpServletRequest.getContentLengthLong();
	}

	public String getContentType() {
		return httpServletRequest.getContentType();
	}

	public ServletInputStream getInputStream() throws IOException {
		return httpServletRequest.getInputStream();
	}

	public String getParameter(String name) {
		return httpServletRequest.getParameter(name);
	}

	public Enumeration<String> getParameterNames() {
		return httpServletRequest.getParameterNames();
	}

	public String[] getParameterValues(String name) {
		return httpServletRequest.getParameterValues(name);
	}

	public Map<String, String[]> getParameterMap() {
		return httpServletRequest.getParameterMap();
	}

	public String getProtocol() {
		return httpServletRequest.getProtocol();
	}

	public String getScheme() {
		return httpServletRequest.getScheme();
	}

	public String getServerName() {
		return httpServletRequest.getServerName();
	}

	public int getServerPort() {
		return httpServletRequest.getServerPort();
	}

	public BufferedReader getReader() throws IOException {
		return httpServletRequest.getReader();
	}

	public String getRemoteAddr() {
		return httpServletRequest.getRemoteAddr();
	}

	public String getRemoteHost() {
		return httpServletRequest.getRemoteHost();
	}

	public Locale getLocale() {
		return httpServletRequest.getLocale();
	}

	public Enumeration<Locale> getLocales() {
		return httpServletRequest.getLocales();
	}

	public boolean isSecure() {
		return httpServletRequest.isSecure();
	}

	@SuppressWarnings("deprecation")
	public String getRealPath(String path) {
		return httpServletRequest.getRealPath(path);
	}

	public int getRemotePort() {
		return httpServletRequest.getRemotePort();
	}

	public String getLocalName() {
		return httpServletRequest.getLocalName();
	}

	public String getLocalAddr() {
		return httpServletRequest.getLocalAddr();
	}

	public int getLocalPort() {
		return httpServletRequest.getLocalPort();
	}

	public boolean isAsyncStarted() {
		return httpServletRequest.isAsyncStarted();
	}

	public boolean isAsyncSupported() {
		return httpServletRequest.isAsyncSupported();
	}

	public DispatcherType getDispatcherType() {
		return httpServletRequest.getDispatcherType();
	}

	public ServletContext getServletContext() {
		return httpServletRequest.getServletContext();
	}

}
