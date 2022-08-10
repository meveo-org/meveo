package org.meveo.admin.action.storage;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.RemoteRepositoryApi;
import org.meveo.api.storage.RemoteRepositoryDto;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.RemoteRepositoryService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.15
 */
@Named
@ViewScoped
public class RemoteRepositoryBean extends BaseCrudBean<RemoteRepository, RemoteRepositoryDto> {

  @Inject
  private RemoteRepositoryService remoteRepositoryService;

  @Inject
  private RemoteRepositoryApi remoteRepositoryApi;

  @Override
  protected IPersistenceService<RemoteRepository> getPersistenceService() {
    return remoteRepositoryService;
  }

  @Override
  public BaseCrudApi<RemoteRepository, RemoteRepositoryDto> getBaseCrudApi() {
    return remoteRepositoryApi;
  }
}
