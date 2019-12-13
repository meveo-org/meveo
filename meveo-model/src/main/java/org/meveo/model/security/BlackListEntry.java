package org.meveo.model.security;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BLACK")
public class BlackListEntry extends EntityPermission {

}
