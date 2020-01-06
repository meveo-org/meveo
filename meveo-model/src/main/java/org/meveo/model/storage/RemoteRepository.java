package org.meveo.model.storage;

import org.meveo.model.AuditableEntity;

public class RemoteRepository extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    private String code;

    private String url;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
