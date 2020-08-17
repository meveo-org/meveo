package org.meveo.model.crm;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.ModuleItem;
import org.meveo.model.customEntities.CustomEntityTemplate;

import java.io.Serializable;

@Entity
@Table(name = "cust_cet_unique_constraint", uniqueConstraints = @UniqueConstraint(columnNames = {"code","cypher_query", "trust_score", "applicable_on_el"}))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {@Parameter(name = "sequence_name", value = "cust_cet_unique_constraint_seq")})
@IdClass(CetUcPk.class)
public class CustomEntityTemplateUniqueConstraint implements Serializable {
    public static final String RETURNED_ID_PROPERTY_NAME = "id";

    @Id
    @Column(name = "code")
    @NotNull
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "cypher_query", columnDefinition = "TEXT")
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

    @Column(name = "position")
    @Min(0)
    @NotNull
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cet_id")
    @Id
    private CustomEntityTemplate customEntityTemplate;

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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setCustomEntityTemplate(CustomEntityTemplate customEntityTemplate) {
        this.customEntityTemplate = customEntityTemplate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CustomEntityTemplateUniqueConstraint{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", cypherQuery='" + cypherQuery + '\'' +
                '}';
    }
}
