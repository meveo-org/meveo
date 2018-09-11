package org.meveo.model.catalog;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Edward P. Legaspi
 */
@Entity
@ExportIdentifier({ "code"})
@Table(name = "cat_digital_resource", uniqueConstraints = @UniqueConstraint(columnNames = { "code"}))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {@Parameter(name = "sequence_name", value = "cat_digital_resource_seq"), })
public class DigitalResource extends BusinessEntity {

	private static final long serialVersionUID = -7528761006943581984L;

	@Column(name = "uri", length = 255)
	private String uri;

	@Column(name = "mime_type", length = 50)
	@Size(max = 50)
	private String mimeType;

	@ManyToMany(mappedBy = "attachments")
	private List<ProductOffering> productOfferings;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public List<ProductOffering> getProductOfferings() {
		return productOfferings;
	}

	public void setProductOfferings(List<ProductOffering> productOfferings) {
		this.productOfferings = productOfferings;
	}

}
