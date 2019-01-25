package org.meveo.model.scripts;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.commons.utils.XStreamCDATAConverter;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;

import com.thoughtworks.xstream.annotations.XStreamConverter;

@ExportIdentifier({"code"})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "meveo_function",  uniqueConstraints = @UniqueConstraint(columnNames = { "code"}))
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "meveo_function_seq")}
)
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
    
    public abstract List<FunctionInput> getInputs();
    
    public abstract boolean hasInputs();
}
