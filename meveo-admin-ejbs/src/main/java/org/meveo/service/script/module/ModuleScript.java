package org.meveo.service.script.module;

import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.service.script.Script;

/**
 * @author Andrius Karpavicius
 **/
public class ModuleScript extends Script implements ModuleScriptInterface {
	
	@Override
	public void preReleaseModule(Map<String, Object> methodContext) throws BusinessException {
	}
	
	@Override
	public void postReleaseModule(Map<String, Object> methodContext) throws BusinessException {
	}

    @Override
    public void preInstallModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void postInstallModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void preUninstallModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void postUninstallModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void preEnableModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void postEnableModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void preDisableModule(Map<String, Object> methodContext) throws BusinessException {
    }

    @Override
    public void postDisableModule(Map<String, Object> methodContext) throws BusinessException {
    }
    
    @Override
    public void prePull(Map<String, Object> methodContext) throws BusinessException {	
    }
    
    @Override
    public void postPull(Map<String, Object> methodContext) throws BusinessException {	
    }
}