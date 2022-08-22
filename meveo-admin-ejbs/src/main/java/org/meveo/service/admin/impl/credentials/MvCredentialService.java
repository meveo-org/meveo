/**
 * 
 */
package org.meveo.service.admin.impl.credentials;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;

import org.meveo.model.admin.MvCredential;
import org.meveo.model.security.DefaultRoleNames;
import org.meveo.service.base.BusinessService;

@RolesAllowed(DefaultRoleNames.ADMIN)
@Stateless
public class MvCredentialService extends BusinessService<MvCredential> {
	
	
}
