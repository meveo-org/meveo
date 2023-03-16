package org.meveo.api.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.exceptions.EntityAlreadyExistsException;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.BusinessEntity;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public abstract class BusinessRs<E extends BusinessEntity, T extends BusinessEntityDto> extends BaseRs /* implements IBusinessRs<E, T> */ {

	@Inject
	protected BaseCrudApi<E, T> api;
	
	@POST
	@Path("/")
	public void create(T postData) throws Exception {
		if (api.exists(postData)) {
			throw new EntityAlreadyExistsException("Entity already exists");
		}
		api.createOrUpdate(postData);
	}
	
	@PUT
	@Path("/")
	public void createOrUpdate(T postData) throws Exception {
		api.createOrUpdate(postData);
	}
	
	@PUT
	@Path("/{code}")
	public void update(@PathParam("code") String code, T postData) throws Exception {
		if (!api.exists(postData)) {
			throw new EntityDoesNotExistsException("Entity does not exists");
		}
		api.createOrUpdate(postData);
	}
	
	@DELETE
	@Path("/{code}")
	public void delete(@PathParam("code") String code) throws Exception {
		T entity = api.findIgnoreNotFound(code);
		if (entity != null) {
			api.remove(entity);
		}
	}
	
	@GET
	@Path("/")
	public List<T> list() {
		return api.findAll();
	}
	
	@GET
	@Path("/{code}")
	public T find(String code) throws Exception {
		return api.find(code);
	}

    
}
