package org.meveo.model.scripts;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.meveo.commons.utils.XStreamCDATAConverter;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.security.Role;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@ExportIdentifier({"code"})
@MappedSuperclass
public abstract class Function extends BusinessEntity {

    private static final long serialVersionUID = -1615762108685208441L;

    @Column(name = "script", nullable = false, columnDefinition = "TEXT")
    @NotNull
    @XStreamConverter(XStreamCDATAConverter.class)
    private String script;

    @Column(name = "function_version", nullable = false)
    private Integer functionVersion = 1;

    public Integer getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(Integer functionVersion) {
        this.functionVersion = functionVersion;
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }
}
