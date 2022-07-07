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
package org.meveo.model.billing;

import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.admin.Currency;
import org.meveo.model.persistence.JsonTypes;

@Entity
@Cacheable
@ExportIdentifier("code")
@Table(name = "adm_country")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "adm_country_seq"), })
@NamedQueries({
        @NamedQuery(name = "Country.listByStatus", query = "SELECT c FROM Country c where c.disabled=:isDisabled order by c.description ASC") })
public class Country extends BusinessEntity {
    private static final long serialVersionUID = 1L;


    @Column(name = "nationality", length = 100)
    @Size(max = 100)
    private String nationality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;
    
	@Type(type = JsonTypes.JSON)
	@Column(name = "description_i18n", columnDefinition = "text")
	private Map<String, String> descriptionI18n;

    /**
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * @param nationality the nationality to set
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
    
    /**
	 * @return the {@link #descriptionI18n}
	 */
	public Map<String, String> getDescriptionI18n() {
		return descriptionI18n;
	}
	
	/**
	 * @param descriptionI18n the descriptionI18n to set
	 */
	public void setDescriptionI18n(Map<String, String> descriptionI18n) {
		this.descriptionI18n = descriptionI18n;
	}

    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof Country)) {
            return false;
        }

        Country other = (Country) obj;

        if (id != null && other.getId() != null && id.equals(other.getId())) {
            return true;
        }
        return (other.code != null) && other.code.equals(this.code);
    }



    public int hashCode() {
        int result = 961 + ((code == null) ? 0 : ("Country" + code).hashCode());
        return result;
    }

}
