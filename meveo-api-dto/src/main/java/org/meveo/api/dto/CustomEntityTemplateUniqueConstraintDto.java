package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;

public class CustomEntityTemplateUniqueConstraintDto extends BaseDto {
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
    @XmlElement()
    private boolean disabled;

    public CustomEntityTemplateUniqueConstraintDto() {
        super();
    }

    public CustomEntityTemplateUniqueConstraintDto(CustomEntityTemplateUniqueConstraint cetUniqueConstraint) {
        super();

        this.code = cetUniqueConstraint.getCode();
        this.appliesTo = cetUniqueConstraint.getAppliesTo();
        this.description = cetUniqueConstraint.getDescription();
        this.cypherQuery = cetUniqueConstraint.getCypherQuery();
        this.trustScore = cetUniqueConstraint.getTrustScore();
        this.applicableOnEl = cetUniqueConstraint.getApplicableOnEl();
        this.disabled = cetUniqueConstraint.isDisabled();
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

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
