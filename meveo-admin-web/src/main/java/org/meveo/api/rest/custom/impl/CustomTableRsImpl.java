package org.meveo.api.rest.custom.impl;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.ws.rs.Path;

import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.logging.WsRestApiInterceptor;

@Path("/customTable")
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class CustomTableRsImpl extends AbstractCustomTableRsImpl<CustomTableDataDto> {

}