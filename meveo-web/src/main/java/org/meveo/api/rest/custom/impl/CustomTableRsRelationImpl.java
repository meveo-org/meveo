package org.meveo.api.rest.custom.impl;

import javax.interceptor.Interceptors;
import javax.ws.rs.Path;

import org.meveo.api.dto.custom.CustomTableDataRelationDto;
import org.meveo.api.logging.WsRestApiInterceptor;

@Path("/customTable/relationship")
@Interceptors({ WsRestApiInterceptor.class })
public class CustomTableRsRelationImpl extends AbstractCustomTableRsImpl<CustomTableDataRelationDto> {

}