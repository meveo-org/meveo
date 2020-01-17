package org.meveo.api.dto.account;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.crm.ProviderContact;

import io.swagger.annotations.ApiModelProperty;

/**
 * Dto class of {@link ProviderContact}.
 *
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "ProviderContract")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderContactDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -763450889692487278L;

	/** The code. */
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "Code of the provider contact")
	private String code;

	/** The description. */
	@XmlAttribute
	@ApiModelProperty("Description of the provider contact")
	private String description;

	/** The first name. */
	@ApiModelProperty("First name of the provider")
	private String firstName;

	/** The last name. */
	@ApiModelProperty("Last name of the provider")
	private String lastName;

	/** The email. */
	@ApiModelProperty("Email of the provider")
	private String email;

	/** The phone. */
	@ApiModelProperty("Phone of the provider")
	private String phone;

	/** The mobile. */
	@ApiModelProperty("Mobile of the provider")
	private String mobile;

	/** The fax. */
	@ApiModelProperty("Fax of the provider")
	private String fax;

	/** The generic mail. */
	@ApiModelProperty("Generic mail of the provider")
	private String genericMail;

	/** The address dto. */
	@ApiModelProperty("Address information")
	private AddressDto addressDto;

	/**
	 * Instantiates a new provider contact dto.
	 */
	public ProviderContactDto() {
		super();
	}

	/**
	 * Instantiates a new provider contact dto.
	 *
	 * @param providerContact the provider contact
	 */
	public ProviderContactDto(ProviderContact providerContact) {
		this.code = providerContact.getCode();
		this.description = providerContact.getDescription();
		this.firstName = providerContact.getFirstName();
		this.lastName = providerContact.getLastName();
		this.email = providerContact.getEmail();
		this.phone = providerContact.getPhone();
		this.mobile = providerContact.getMobile();
		this.fax = providerContact.getFax();
		this.genericMail = providerContact.getGenericMail();
		if (providerContact.getAddress() != null) {
			this.addressDto = new AddressDto(providerContact.getAddress());
		}
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the first name.
	 *
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name.
	 *
	 * @param firstName the new first name
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the last name.
	 *
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name.
	 *
	 * @param lastName the new last name
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the phone.
	 *
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the phone.
	 *
	 * @param phone the new phone
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Gets the mobile.
	 *
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * Sets the mobile.
	 *
	 * @param mobile the new mobile
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * Gets the fax.
	 *
	 * @return the fax
	 */
	public String getFax() {
		return fax;
	}

	/**
	 * Sets the fax.
	 *
	 * @param fax the new fax
	 */
	public void setFax(String fax) {
		this.fax = fax;
	}

	/**
	 * Gets the generic mail.
	 *
	 * @return the generic mail
	 */
	public String getGenericMail() {
		return genericMail;
	}

	/**
	 * Sets the generic mail.
	 *
	 * @param genericMail the new generic mail
	 */
	public void setGenericMail(String genericMail) {
		this.genericMail = genericMail;
	}

	/**
	 * Gets the address dto.
	 *
	 * @return the address dto
	 */
	public AddressDto getAddressDto() {
		return addressDto;
	}

	/**
	 * Sets the address dto.
	 *
	 * @param addressDto the new address dto
	 */
	public void setAddressDto(AddressDto addressDto) {
		this.addressDto = addressDto;
	}
}