/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.api.rest.module.impl;

import java.io.File;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtoResponse;
import org.meveo.api.dto.response.module.MeveoModuleDtosResponse;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.module.MeveoModuleApi;
import org.meveo.api.module.OnDuplicate;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.module.ModuleRs;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.ModuleUninstall;
import org.meveo.service.git.GitRepositoryService;

/**
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class ModuleRsImpl extends BaseRs implements ModuleRs {

    @Inject
    private MeveoModuleApi moduleApi;
    
    @Inject
    private GitRepositoryService gitRepositoryService;
    
    @Context
    private HttpServletResponse httpServletResponse;

    @Override
    public ActionStatus create(MeveoModuleDto moduleData, boolean development) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            moduleApi.create(moduleData, development);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus update(MeveoModuleDto moduleDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            moduleApi.update(moduleDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus delete(String code, ModuleUninstall moduleUninstall) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            moduleApi.delete(code, moduleUninstall);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public Response list(boolean codesOnly, MeveoModuleFilters filters) {

        if(!codesOnly) {
            MeveoModuleDtosResponse result = new MeveoModuleDtosResponse();
            result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);
            result.getActionStatus().setMessage("");
            try {
                List<MeveoModuleDto> dtos = moduleApi.list(filters);
                result.setModules(dtos);
            } catch (Exception e) {
                processException(e, result.getActionStatus());
            }

            return Response.ok(result).build();
        } else {
            return Response.ok(moduleApi.listCodesOnly(filters)).build();
        }

    }

    @Override
    public MeveoModuleDtoResponse get(String code) {
        MeveoModuleDtoResponse result = new MeveoModuleDtoResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        try {
            result.setModule(moduleApi.find(code));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdate(MeveoModuleDto moduleDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.createOrUpdate(moduleDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus install(MeveoModuleDto moduleDto, List<String> repositories) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
        	//TODO: add option in query
        	MeveoModuleDto dto = moduleApi.findIgnoreNotFound(moduleDto.getCode());
        	if(dto == null) {
        		moduleApi.create(moduleDto, false);
                moduleApi.install(repositories, moduleDto, OnDuplicate.FAIL);
        	} else {
                moduleApi.install(repositories, dto, OnDuplicate.FAIL);
        	}

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus uninstall(String code, boolean remove) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.uninstall(MeveoModule.class, ModuleUninstall.of(code));

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus enable(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.enable(code, MeveoModule.class);

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus disable(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.disable(code, MeveoModule.class);

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public MeveoModuleDto addItemToModule(String moduleCode, String itemCode, String itemType, String appliesTo) throws EntityDoesNotExistsException, BusinessException {
        return moduleApi.addToModule(moduleCode, itemCode, itemType, appliesTo);
    }

    @Override
    public MeveoModuleDto removeItemFromModule(String moduleCode, String itemCode, String itemType, String appliesTo) throws EntityDoesNotExistsException, BusinessException {
        return moduleApi.removeFromModule(moduleCode, itemCode, itemType, appliesTo);
    }

    @Override
    public MeveoModuleDto addFileToModule(String moduleCode, String path) throws EntityDoesNotExistsException, BusinessException {
        return moduleApi.addFileToModule(moduleCode, path);
    }

    @Override
    public MeveoModuleDto removeFileFromModule(String moduleCode, String path) throws EntityDoesNotExistsException, BusinessException {
        return moduleApi.removeFileFromModule(moduleCode, path);
    }

    @Override
	public ActionStatus fork(String moduleCode) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            moduleApi.fork(moduleCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
	}

    @Override
    public void importZip(@NotNull ModuleUploadForm uploadForm, boolean overwrite) throws EntityDoesNotExistsException {
        moduleApi.importZip(uploadForm.getFilename(), uploadForm.getData(), overwrite);
    }

    @Override
    public File export(List<String> modulesCode, ExportFormat exportFormat) throws Exception {
    	return moduleApi.exportModules(modulesCode, exportFormat);
    }

    @Override
    public ActionStatus release(String moduleCode, String nextVersion) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            moduleApi.release(moduleCode, nextVersion);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

	@Override
	public void installFromGitRepository(String code, List<String> repositories) throws BusinessException, MeveoApiException {
		GitRepository repo = gitRepositoryService.findByCode(code);
		if (repo == null) {
			throw new IllegalArgumentException("The repository " + code + " doesn't exist");
		} else {
			moduleApi.install(repositories, repo);
		}
	}
}
