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
package org.meveo.api.ws.impl;

import java.util.List;

import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jws.WebService;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtoResponse;
import org.meveo.api.dto.response.module.MeveoModuleDtosResponse;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.module.MeveoModuleApi;
import org.meveo.api.module.OnDuplicate;
import org.meveo.api.ws.ModuleWs;
import org.meveo.model.module.MeveoModule;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.ModuleUninstall;
import org.meveo.service.storage.RepositoryService;

/**
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @lastModifiedVersion 6.3.0
 */
@WebService(serviceName = "ModuleWs", endpointInterface = "org.meveo.api.ws.ModuleWs")
@Interceptors({ WsRestApiInterceptor.class })
public class ModuleWsImpl extends BaseWs implements ModuleWs {

    @Inject
    private MeveoModuleApi moduleApi;
    
    @Override
    public ActionStatus create(MeveoModuleDto moduleDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.create(moduleDto, false);
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
    public ActionStatus delete(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.delete(code);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public MeveoModuleDtosResponse list() {
        MeveoModuleDtosResponse result = new MeveoModuleDtosResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);
        result.getActionStatus().setMessage("");
        try {
            List<MeveoModuleDto> dtos = moduleApi.list((MeveoModuleFilters) null);
            result.setModules(dtos);
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public MeveoModuleDtoResponse get(String code) {
        MeveoModuleDtoResponse result = new MeveoModuleDtoResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);
        result.getActionStatus().setMessage("");
        try {
            MeveoModuleDto dto = moduleApi.find(code);
            result.setModule(dto);
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
    public ActionStatus installModule(MeveoModuleDto moduleDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.install(null, moduleDto, OnDuplicate.OVERWRITE);

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus uninstallModule(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.uninstall(MeveoModule.class, ModuleUninstall.of(code));

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus enableModule(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.enable(code, MeveoModule.class);

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus disableModule(String code) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            moduleApi.disable(code, MeveoModule.class);

        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }
}
