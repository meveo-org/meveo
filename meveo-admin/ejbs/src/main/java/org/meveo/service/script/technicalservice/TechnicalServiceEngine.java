package org.meveo.service.script.technicalservice;

import org.meveo.model.technicalservice.TechnicalService;
import org.meveo.service.script.ScriptInterface;

public abstract class TechnicalServiceEngine<T extends TechnicalService> implements ScriptInterface {

    protected T service;

    public TechnicalServiceEngine(T service) {
        this.service = service;
    }

    public TechnicalService getService() {
        return service;
    }

    public void setService(T service) {
        this.service = service;
    }

}
