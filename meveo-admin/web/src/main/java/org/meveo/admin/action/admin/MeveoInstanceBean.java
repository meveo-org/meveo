/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.action.admin;

import java.sql.BatchUpdateException;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.dto.config.MavenConfigurationResponseDto;
import org.meveo.model.communication.MeveoInstance;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.communication.impl.MeveoInstanceService;
import org.meveo.service.config.impl.MavenConfigurationService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 */
@Named
@ViewScoped
public class MeveoInstanceBean extends BaseBean<MeveoInstance> {

	private static final long serialVersionUID = 1L;

	@Inject
	private MeveoInstanceService meveoInstanceService;

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	public MeveoInstanceBean() {
		super(MeveoInstance.class);
	}

	/**
	 * @see org.meveo.admin.action.BaseBean#getPersistenceService()
	 */
	@Override
	protected IPersistenceService<MeveoInstance> getPersistenceService() {
		return meveoInstanceService;
	}

	public void test() throws BatchUpdateException {
		throw new BatchUpdateException();
	}

	@Override
	protected String getDefaultSort() {
		return "creationDate";
	}

	@ActionMethod
	public void synchRemoteRepositories() {

		try {
			Response response = meveoInstanceService.getRemoteRepositories("api/rest/mavenConfiguration", entity);
			MavenConfigurationResponseDto result = response.readEntity(MavenConfigurationResponseDto.class);
			mavenConfigurationService.updateRepository(result.getMavenConfiguration().getMavenRepositories());

			messages.info(new BundleKey("messages", "meveoInstance.remoteRepository.synch.ok"));

		} catch (BusinessException e) {
			messages.error(new BundleKey("messages", "meveoInstance.remoteRepository.synch.ko"));
		}
	}
}