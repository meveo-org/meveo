package org.meveo.api.rest;

import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.transaction.RollbackException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.Validation;
import org.meveo.admin.exception.EntityAlreadyLinkedToModule;
import org.meveo.admin.exception.ExistsRelatedEntityException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.exceptions.EntityAlreadyExistsException;
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
        try {

            if (e instanceof UnrecognizedPropertyException) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ActionStatus(ActionStatusEnum.FAIL, MeveoApiErrorCodeEnum.INVALID_PARAMETER, e.getMessage())).build();

            } else if (e instanceof NotFoundException || e instanceof NotAllowedException || e instanceof EntityDoesNotExistsException || e instanceof org.meveo.exceptions.EntityDoesNotExistsException) {
                return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();

            } else if (e instanceof ValidationException || e instanceof JsonParseException || e instanceof JsonMappingException || e instanceof IllegalArgumentException) {
                return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

            } else if (e instanceof EJBException && ((EJBException) e).getCausedByException() instanceof Exception) {
                return toResponse(((EJBException) e).getCausedByException());

            } else if (e instanceof RollbackException && e.getCause() instanceof Exception) {
                return toResponse((Exception)((RollbackException) e).getCause());

            } else if(e instanceof MeveoApiException && e.getCause() instanceof Exception) {
                return toResponse((Exception)((MeveoApiException) e).getCause());
                
            } else if (e instanceof ExistsRelatedEntityException) {
                return Response.status(Status.CONFLICT).entity(e.getMessage()).build();

            } else if (e instanceof UserNotAuthorizedException) {
                return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();

            } else if (e instanceof EntityAlreadyExistsException || e instanceof org.meveo.api.exception.EntityAlreadyExistsException || e instanceof EntityAlreadyLinkedToModule) {
                return Response.status(Status.CONFLICT).entity(e.getMessage()).build();

            } else {
            	log.error("Rest request failed", e);
            }

            return buildResponse(unwrapException(e), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);

        } catch (Exception ex) {
            log.error("REST request failed", e);
            log.error("Error mapping exception", ex);
            return Response.status(500).build();
        }
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