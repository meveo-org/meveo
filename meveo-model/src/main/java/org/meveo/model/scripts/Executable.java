package org.meveo.model.scripts;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.meveo.commons.utils.XStreamCDATAConverter;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@ExportIdentifier({"code"})
@MappedSuperclass
public abstract class Executable extends BusinessEntity {

    private static final long serialVersionUID = -1615762108685208441L;

    @Column(name = "script", nullable = false, columnDefinition = "TEXT")
    @NotNull
    @XStreamConverter(XStreamCDATAConverter.class)
    private String script;

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
