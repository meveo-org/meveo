package org.meveo.model.scripts;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@ExportIdentifier({"code"})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "meveo_function",  uniqueConstraints = @UniqueConstraint(columnNames = { "code"}))
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "meveo_function_seq")}
)
public class Function extends BusinessEntity {

    private static final long serialVersionUID = -1615762108685208441L;

    @Column(name = "function_version", nullable = false)
    private Integer functionVersion = 1;

    @Column(name = "test_suite", columnDefinition = "TEXT")
    @Type(type = "json")
    private String testSuite;
    
    /**
     * @deprecated Use child implementations
     */
    @Deprecated
    public Function() {
    	
    }

    public Integer getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(Integer functionVersion) {
        this.functionVersion = functionVersion;
    }

    public List<FunctionIO> getInputs(){
    	return new ArrayList<>();
    }
    
    public boolean hasInputs() {
    	return false;
    }

    public List<FunctionIO> getOutputs(){
    	return new ArrayList<>();
    }

    public boolean hasOutputs() {
    	return false;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    public String getFunctionType() {
    	return "Unknown";
    }
}
