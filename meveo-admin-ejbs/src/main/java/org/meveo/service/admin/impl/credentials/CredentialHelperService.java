/**
 *
 */
package org.meveo.service.admin.impl.credentials;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.elresolver.ValueExpressionWrapper;
import org.meveo.model.admin.MvCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialHelperService {

	private static final Logger log = LoggerFactory.getLogger(CredentialHelperService.class);

	@Inject
	private MvCredentialService credentialService;

	public static class LoggingFilter implements ClientRequestFilter {
		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			if (requestContext != null) {
				if (requestContext.getEntity() != null) {
					log.info(requestContext.getEntity().toString());
				} else {
					log.info("uri:{}", requestContext.getUri());
				}
			}
		}
	}

	public MvCredential getCredential(String domain) {
		PaginationConfiguration paginationConfiguration = new PaginationConfiguration();
		paginationConfiguration.setFilters(new HashMap<>());
		paginationConfiguration.getFilters().put("domainName", domain);

		List<MvCredential> matchingCredentials = credentialService.list(paginationConfiguration);

		if (matchingCredentials.size() > 0) {
			return matchingCredentials.get(0);
		} else {
			return null;
		}
	}

	public MvCredential getCredentialByCode(String code) {
		MvCredential credentialFound = credentialService.findByCode(code);

    if (credentialFound != null) {
			return credentialFound;
		} else {
			return null;
		}
	}

	public Invocation.Builder setCredential(Invocation.Builder invocBuilder, MvCredential credential) throws BusinessException {
		String headerKey = credential.getHeaderKey();
		String headerValue = credential.getHeaderValue();
		try {
			if (headerKey.contains("#{")) {
				headerKey = ValueExpressionWrapper.evaluateToStringMultiVariable(headerKey, "entity", credential);
			}
			if (headerValue.contains("#{")) {
				headerValue = ValueExpressionWrapper.evaluateToStringMultiVariable(headerValue, "entity", credential);
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
		return invocBuilder.header(headerKey, headerValue);
	}

}
