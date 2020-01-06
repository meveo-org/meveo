package org.meveo.service.storage;

import org.meveo.model.storage.RemoteRepository;
import org.meveo.service.base.PersistenceService;

import javax.ejb.Stateless;
import javax.inject.Named;

@Stateless
@Named
public class RemoteRepositoryService extends PersistenceService<RemoteRepository> {

}
