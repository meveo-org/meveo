package org.meveo.undertow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.builder.HandlerBuilder;

/**
 * <p>
 * Overrides the undertow server to increase the maximum post size default of
 * 10MB to 100MB.
 * </p>
 * <p>
 * The value can be overriden in src/main/webapp/WEB-INF/undertow-handlers.conf
 * </p>
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
public class MaxPostSizeHandler implements HttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(MaxPostSizeHandler.class);

	private final HttpHandler next;
	private final Long limit;

	public MaxPostSizeHandler(final HttpHandler next, final Long limit) {
		this.next = next;
		this.limit = limit;
	}

	public void handleRequest(final HttpServerExchange exchange) throws Exception {

		logger.trace("Set max-entity-size undertow to {}", limit);
		exchange.setMaxEntitySize(limit);
		next.handleRequest(exchange);
	}

	public static class Builder implements HandlerBuilder {

		public String name() {
			return "max-entity-size";
		}

		public Map<String, Class<?>> parameters() {
			return Collections.<String, Class<?>>singletonMap("limit", long.class);
		}

		public Set<String> requiredParameters() {
			return Collections.singleton("limit");
		}

		public String defaultParameter() {
			return "limit";
		}

		public HandlerWrapper build(Map<String, Object> config) {
			return new Wrapper((Long) config.get("limit"));
		}
	}

	private static class Wrapper implements HandlerWrapper {

		private final Long limit;

		public Wrapper(final Long limit) {
			this.limit = limit;
		}

		public HttpHandler wrap(final HttpHandler handler) {
			return new MaxPostSizeHandler(handler, limit);
		}
	}
}