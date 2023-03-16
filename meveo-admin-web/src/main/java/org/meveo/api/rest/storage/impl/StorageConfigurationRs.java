/**
 * 
 */
package org.meveo.api.rest.storage.impl;

import javax.ws.rs.Path;

import org.meveo.api.rest.BusinessRs;
import org.meveo.api.storage.StorageConfigurationDto;
import org.meveo.model.storage.StorageConfiguration;

@Path("/storages/configurations")
public class StorageConfigurationRs extends BusinessRs<StorageConfiguration, StorageConfigurationDto>{

}
