package org.meveo.model.crm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BusinessEntity;

@Entity
@Table(name = "cust_cet_unique_constraint", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {@Parameter(name = "sequence_name", value = "cust_cet_unique_constraint_seq")})
public class CustomEntityTemplateUniqueConstraint extends BusinessEntity {
    public static final String RETURNED_ID_PROPERTY_NAME = "id";

    @Column(name = "applies_to", nullable = false, length = 100)
    @Size(max = 100)
    @NotNull
    private String appliesTo;

    @Column(name = "cypher_query", columnDefinition = "text")
    @NotNull
    private String cypherQuery;

    @Column(name = "trust_score")
    @Min(0)
    @Max(100)
    @NotNull
    private Integer trustScore;

    @Column(name = "applicable_on_el", length = 2000)
    @Size(max = 2000)
    private String applicableOnEl;

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public String getCypherQuery() {
        return cypherQuery;
    }

    public void setCypherQuery(String cypherQuery) {
        this.cypherQuery = cypherQuery;
    }

    public Integer getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Integer trustScore) {
        this.trustScore = trustScore;
    }

    public String getApplicableOnEl() {
        return applicableOnEl;
    }

    public void setApplicableOnEl(String applicableOnEl) {
        this.applicableOnEl = applicableOnEl;
    }
}
