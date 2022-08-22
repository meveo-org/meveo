/**
 * 
 */
package org.meveo.api.rest;

import javax.ws.rs.Path;

import org.meveo.api.dto.MvCredentialDto;
import org.meveo.model.admin.MvCredential;

@Path("/credential")
public class MvCredentialRs extends BusinessRs<MvCredential, MvCredentialDto>{
	
}
