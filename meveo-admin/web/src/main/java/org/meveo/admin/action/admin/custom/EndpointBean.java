package org.meveo.admin.action.admin.custom;

import org.meveo.admin.action.BaseBean;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.technicalservice.endpoint.EndpointService;
import org.primefaces.model.DualListModel;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hien Bach.
 */
@Named
@ViewScoped
public class EndpointBean extends BaseBean<Endpoint> {

    private static final long serialVersionUID = 1895532923500996522L;

    @Inject
    private EndpointService endpointService;

    private DualListModel<EndpointPathParameter> pathParameterListModel;

    /**
     * Constructor. Invokes super constructor and provides class type of this
     * bean for {@link org.meveo.admin.action.BaseBean}.
     */
    public EndpointBean() {
        super(Endpoint.class);
    }
    /**
     * Factory method for entity to edit. If objectId param set load that entity
     * from database, otherwise create new.
     * @return currency
     */
    @Override
    public Endpoint initEntity() {
        Endpoint endpoint = super.initEntity();
        return endpoint;
    }

    @Override
    protected String getListViewName() {
        return "technicalServiceEndpoints";
    }

    @Override
    protected IPersistenceService<Endpoint> getPersistenceService() {
        return endpointService;
    }


    public DualListModel<EndpointPathParameter> getEndpointPathParameter() {

        if (pathParameterListModel == null) {
            List<EndpointPathParameter> perksSource = new ArrayList<>();
            perksSource =  endpointService.listActive().get(0).getPathParameters();
            List<EndpointPathParameter> perksTarget = new ArrayList<>();
            if (getEntity().getPathParameters() != null) {
                perksTarget.addAll(getEntity().getPathParameters());
            }
            perksSource.removeAll(perksTarget);
            pathParameterListModel = new DualListModel<EndpointPathParameter>(perksSource, perksTarget);
        }
        return pathParameterListModel;
    }

    public void setPathParameterListModel(DualListModel<EndpointPathParameter> pathParameterListModel) {
        this.pathParameterListModel = pathParameterListModel;
    }

    @Override
    public String getNewViewName() {
        return "technicalServiceEndpointDetail";
    }

    @Override
    public String getEditViewName() {
        return "technicalServiceEndpointDetail";
    }
}