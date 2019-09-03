package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;

@XmlRootElement(name = "CustomEntityTemplateUniqueConstraint")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomEntityTemplateUniqueConstraintDto {
    @XmlAttribute(required = true)
    private String code;
    @XmlAttribute()
    protected String appliesTo;
    @XmlAttribute()
    private String description;
    @XmlAttribute(required = true)
    private String cypherQuery;
    @XmlAttribute()
    private Integer trustScore;
    @XmlElement()
    private String applicableOnEl;
    @XmlAttribute(required = true)
    private Integer order;

    public CustomEntityTemplateUniqueConstraintDto() {
        super();
    }

    public CustomEntityTemplateUniqueConstraintDto(CustomEntityTemplateUniqueConstraint cetUniqueConstraint) {
        super();

        this.code = cetUniqueConstraint.getCode();
        this.description = cetUniqueConstraint.getDescription();
        this.cypherQuery = cetUniqueConstraint.getCypherQuery();
        this.trustScore = cetUniqueConstraint.getTrustScore();
        this.applicableOnEl = cetUniqueConstraint.getApplicableOnEl();
        this.order = cetUniqueConstraint.getPosition();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
