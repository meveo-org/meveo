package org.meveo.model.customEntities;

import org.hibernate.annotations.*;
import org.meveo.model.AuditableEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by Hien.Bach
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Entity
@ModuleItem(value = "CustomEntityReference", path = "customEntityReferences")
@ModuleItemOrder(30)
@Cacheable
@Table(name = "cet_ref")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "cet_ref_seq"), })
@NamedQueries({@NamedQuery(name = "CustomEntityReference.getCER", query = "SELECT distinct cer from CustomEntityReference cer join fetch cer.customEntityTemplate"),
        @NamedQuery(name = "CustomEntityReference.getExistingCET", query = "SELECT distinct cer from CustomEntityReference cer join fetch cer.customEntityTemplate cet where cet.id =:cetId"),
        @NamedQuery(name = "CustomEntityReference.getExistingUpdateCET", query = "SELECT distinct cer from CustomEntityReference cer join cer.customEntityTemplate cet where cet.id =:cetId and cer.id<>:id")})
public class CustomEntityReference extends AuditableEntity {
    private static final long serialVersionUID = 8281478284763353310L;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "cet_id")
    private CustomEntityTemplate customEntityTemplate;

    public boolean isDisplaySub() {
        return displaySub;
    }

    public void setDisplaySub(boolean displaySub) {
        this.displaySub = displaySub;
    }

    @Column(name = "display_sub")
    @Type(type = "numeric_boolean")
    private boolean displaySub;

    @Column(name = "label_cet")
    private String labelCet;

    public CustomEntityTemplate getCustomEntityTemplate() {
        return customEntityTemplate;
    }

    public void setCustomEntityTemplate(CustomEntityTemplate customEntityTemplate) {
        this.customEntityTemplate = customEntityTemplate;
    }

    public String getLabelCet() {
        return labelCet;
    }

    public void setLabelCet(String labelCet) {
        this.labelCet = labelCet;
    }
}
