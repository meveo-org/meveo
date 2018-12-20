package org.meveo.model.customEntities;

import org.hibernate.annotations.*;
import org.meveo.model.AuditableEntity;
import org.meveo.model.ModuleItem;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by Hien.Bach
 */
@Entity
@ModuleItem
@Cacheable
@Table(name = "cet_ref")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "cet_ref_seq"), })
@NamedQueries({@NamedQuery(name = "CustomEntityReference.getCER", query = "SELECT cer from CustomEntityReference cer join fetch cer.customEntityTemplate cet join fetch cet.subTemplates")})
public class CustomEntityReference extends AuditableEntity {
    private static final long serialVersionUID = 8281478284763353310L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cet_id")
    private CustomEntityTemplate customEntityTemplate;

    public CustomEntityTemplate getCustomEntityTemplate() {
        return customEntityTemplate;
    }

    public void setCustomEntityTemplate(CustomEntityTemplate customEntityTemplate) {
        this.customEntityTemplate = customEntityTemplate;
    }
}
