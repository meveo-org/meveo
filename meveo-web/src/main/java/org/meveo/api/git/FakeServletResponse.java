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

package org.meveo.api.git;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Supplier;

/**
 * {@link HttpServletResponse} that register the output writing to a list instead of actualy writing them
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
public class FakeServletResponse implements HttpServletResponse {

    private ServletOutputStream servletOutputStream = new ServletOutputStream() {
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }

        @Override
        public void write(int b) {
            lines.add(b);
        }
    };

    private HttpServletResponse httpServletResponse;
    private List<Integer> lines = new ArrayList<>();

    public List<Integer> getLines() {
        return lines;
    }

    public FakeServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public void addCookie(Cookie cookie) {
        httpServletResponse.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return httpServletResponse.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return httpServletResponse.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return httpServletResponse.encodeRedirectURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return httpServletResponse.encodeUrl(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return httpServletResponse.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        httpServletResponse.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        httpServletResponse.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        httpServletResponse.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        httpServletResponse.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        httpServletResponse.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        httpServletResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        httpServletResponse.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        httpServletResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        httpServletResponse.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        httpServletResponse.setStatus(sc);
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String sm) {
        httpServletResponse.setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return httpServletResponse.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return httpServletResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return httpServletResponse.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return httpServletResponse.getHeaderNames();
    }

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        httpServletResponse.setTrailerFields(supplier);
    }

    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        return httpServletResponse.getTrailerFields();
    }

    @Override
    public String getCharacterEncoding() {
        return httpServletResponse.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return httpServletResponse.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(servletOutputStream);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        httpServletResponse.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        httpServletResponse.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        httpServletResponse.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type) {
        httpServletResponse.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        httpServletResponse.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return httpServletResponse.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        httpServletResponse.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        httpServletResponse.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return httpServletResponse.isCommitted();
    }

    @Override
    public void reset() {
        httpServletResponse.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        httpServletResponse.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return httpServletResponse.getLocale();
    }
}
