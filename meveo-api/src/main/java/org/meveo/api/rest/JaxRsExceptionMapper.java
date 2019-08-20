package org.meveo.api.rest;

import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.Validation;
import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

@Provider
@Singleton
public class JaxRsExceptionMapper implements ExceptionMapper<Exception> {

    private static Logger log = LoggerFactory.getLogger(JaxRsExceptionMapper.class);

    @Override
    public Response toResponse(Exception e) {
    	if(!(e instanceof EJBException)) {
    		log.error("REST request failed : ", e);
    	}

        if (e instanceof UnrecognizedPropertyException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ActionStatus(ActionStatusEnum.FAIL, MeveoApiErrorCodeEnum.INVALID_PARAMETER, e.getMessage())).build();

        } else if (e instanceof NotFoundException || e instanceof NotAllowedException || e instanceof EntityDoesNotExistsException) {
            return Response.status(Response.Status.NOT_FOUND).build();

        } else if (e instanceof JsonParseException || e instanceof JsonMappingException || e instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ActionStatus(ActionStatusEnum.FAIL, MeveoApiErrorCodeEnum.INVALID_PARAMETER, e.getMessage())).build();

        } else if(e instanceof EJBException) {
            return toResponse(((EJBException) e).getCausedByException());
        }

        return buildResponse(unwrapException(e), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
    }

    protected Response buildResponse(Object entity, String mediaType, Status status) {
        ResponseBuilder builder = Response.status(status).entity(entity);
        builder.type(MediaType.TEXT_PLAIN);
        builder.header(Validation.VALIDATION_HEADER, "true");
        return builder.build();
    }

    protected String unwrapException(Throwable t) {
        StringBuffer sb = new StringBuffer();
        doUnwrapException(sb, t);
        return sb.toString();
    }

    private void doUnwrapException(StringBuffer sb, Throwable t) {
        if (t == null) {
            return;
        }
        sb.append(t.toString());
        if (t.getCause() != null && t != t.getCause()) {
            sb.append('[');
            doUnwrapException(sb, t.getCause());
            sb.append(']');
        }
    }

}