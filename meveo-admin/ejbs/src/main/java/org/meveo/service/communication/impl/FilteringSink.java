package org.meveo.service.communication.impl;

import java.util.concurrent.CompletionStage;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;

/**
 * @version 6.10.0
 */
public class FilteringSink implements SseEventSink {
	private SseEventSink sink;
	private String callerIp;
	private String userName;
	private String filterEL;

	FilteringSink(String callerIp, String userName, String filterEL, SseEventSink sink) {
		this.callerIp = callerIp;
		this.userName = userName;
		this.filterEL = filterEL;
		this.sink = sink;
	}

	public void close() {
		sink.close();
	}

	public boolean isClosed() {
		return sink.isClosed();
	}

	@Override
	public CompletionStage<?> send(OutboundSseEvent outboundSseEvent) {
		return sink.send(outboundSseEvent);
	}

	String getKey() {
		return callerIp + "_" + userName + "_" + filterEL;
	}

	public String getCallerIp() {
		return callerIp;
	}

	public String getUserName() {
		return userName;
	}

	public String getfilterEL() {
		return filterEL;
	}

}