/**
 * 
 */
package org.meveo.api.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.BusinessEntity;

/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public interface IBusinessRs<E extends BusinessEntity, T extends BaseEntityDto> {

	@GET
	@Path("/")
	List<T> list();
	
}
