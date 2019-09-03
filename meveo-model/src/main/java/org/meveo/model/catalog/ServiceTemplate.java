/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.catalog;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.*;
import org.meveo.model.annotation.ImageType;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@ModuleItem
@ObservableEntity
@Cacheable
@CustomFieldEntity(cftCodePrefix = "SERVICE")
@ExportIdentifier({ "code" })
@Table(name = "cat_service_template", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "cat_service_template_seq"), })
public class ServiceTemplate extends BusinessCFEntity implements IImageUpload {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoicing_calendar_id")
    private Calendar invoicingCalendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_service_model_id")
    private BusinessServiceModel businessServiceModel;

    @Size(max = 2000)
    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @ImageType
    @Column(name = "image_path", length = 100)
    @Size(max = 100)
    private String imagePath;

    @Column(name = "minimum_amount_el", length = 2000)
    @Size(max = 2000)
    private String minimumAmountEl;

    @Column(name = "minimum_label_el", length = 2000)
    @Size(max = 2000)
    private String minimumLabelEl;

    @Transient
    private boolean selected;

	/**
	 * If service is from BSM, it allows us to have a duplicate service template when instantiating BOM.
	 */
	@Transient
	private boolean instantiatedFromBSM;
	
	@Transient
	private String descriptionOverride;


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof ServiceTemplate)) {
            return false;
        }

        ServiceTemplate other = (ServiceTemplate) obj;
        
        if (id != null && other.getId() != null && id.equals(other.getId())) {
            return true;
        }
        
        if (code == null) {
            if (other.getCode() != null)
                return false;
        } else if (!code.equals(other.getCode()))
            return false;
        return true;
    }

    public Calendar getInvoicingCalendar() {
        return invoicingCalendar;
    }

    public void setInvoicingCalendar(Calendar invoicingCalendar) {
        this.invoicingCalendar = invoicingCalendar;
    }

    public BusinessServiceModel getBusinessServiceModel() {
        return businessServiceModel;
    }

    public void setBusinessServiceModel(BusinessServiceModel businessServiceModel) {
        this.businessServiceModel = businessServiceModel;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

	public boolean isInstantiatedFromBSM() {
		return instantiatedFromBSM;
	}

	public void setInstantiatedFromBSM(boolean instantiatedFromBSM) {
		this.instantiatedFromBSM = instantiatedFromBSM;
	}
    public String getDescriptionOverride() {
        return descriptionOverride;
    }

    public void setDescriptionOverride(String descriptionOverride) {
        this.descriptionOverride = descriptionOverride;
    }

    public String getMinimumAmountEl() {
        return minimumAmountEl;
    }

    public void setMinimumAmountEl(String minimumAmountEl) {
        this.minimumAmountEl = minimumAmountEl;
    }

    public String getMinimumLabelEl() {
        return minimumLabelEl;
    }

    public void setMinimumLabelEl(String minimumLabelEl) {
        this.minimumLabelEl = minimumLabelEl;
    }

}