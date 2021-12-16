package org.meveo.admin.action.admin.endpoint;

import java.util.Arrays;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.meveo.admin.action.BaseBean;
import org.meveo.model.technicalservice.wsendpoint.WSEndpoint;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.technicalservice.wsendpoint.WSEndpointService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since
 */
@Named
@ViewScoped
public class WsEndpointBean extends BaseBean<WSEndpoint> {

  @Inject
  private WSEndpointService wsEndpointService;

  public WsEndpointBean() {
    super(WSEndpoint.class);
  }

  @Override
  protected IPersistenceService<WSEndpoint> getPersistenceService() {
    return wsEndpointService;
  }

  @Override
  protected List<String> getFormFieldsToFetch() {
    return Arrays.asList("service");
  }

  @Override
  protected List<String> getListFieldsToFetch() {
    return Arrays.asList("service");
  }

  @Override
  protected String getListViewName() {
    return "webSocketEndpoints";
  }

  @Override
  public String getEditViewName() {
    return "webSocketEndpointDetail";
  }

  public void onFunctionChange() {

  }
}
