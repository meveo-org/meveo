package org.meveo.model.security;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("WHITE")
public class WhiteListEntry extends EntityPermission {

}
