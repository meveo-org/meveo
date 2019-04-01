package org.meveo.admin.action.admin.custom;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.technicalservice.endpoint.EndpointService;
import org.primefaces.model.DualListModel;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hien Bach.
 */
@Named
@ViewScoped
public class EndpointBean extends BaseBean<Endpoint> {

    private static final long serialVersionUID = 1895532923500996522L;

    @Inject
    private EndpointService endpointService;

    private DualListModel<EndpointPathParameter> pathParametersDL;

    private List<TSParameterMapping> parameterMappings = new ArrayList<>();

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

    public DualListModel<EndpointPathParameter> getDualListModel() {
        if (pathParametersDL == null) {
            List<EndpointPathParameter> perksSource = new ArrayList<>();
            if (entity.getService() != null && CollectionUtils.isNotEmpty(entity.getService().getInputs())) {
                List<FunctionIO> functionIOList = entity.getService().getInputs();
                EndpointPathParameter endpointPathParameter;
                EndpointParameter endpointParameter;
                for (FunctionIO functionIO : functionIOList) {
                    endpointPathParameter = new EndpointPathParameter();
                    endpointParameter = new EndpointParameter();
                    endpointParameter.setEndpoint(entity);
                    endpointParameter.setParameter(functionIO.getName());
                    endpointPathParameter.setEndpointParameter(endpointParameter);
                    perksSource.add(endpointPathParameter);
                }
                List<EndpointPathParameter> perksTarget = new ArrayList<>();
                if (getEntity().getPathParameters() != null) {
                    perksTarget.addAll(getEntity().getPathParameters());
                }
                perksSource.removeAll(perksTarget);
                pathParametersDL = new DualListModel<EndpointPathParameter>(perksSource, perksTarget);
            } else {
                List<EndpointPathParameter> perksTarget = new ArrayList<>();
                pathParametersDL = new DualListModel<EndpointPathParameter>(perksSource, perksTarget);
            }
        }
        return pathParametersDL;
    }

    public void setDualListModel(DualListModel<EndpointPathParameter> pathParametersDL) {
        this.pathParametersDL = pathParametersDL;
    }

    public List<TSParameterMapping> getParameterMappings() {
        parameterMappings.clear();
        if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getSource())) {
            TSParameterMapping tsParameterMapping;
            EndpointParameter endpointParameter;
            Map<String, TSParameterMapping> tsParameterMappingMap = new HashMap<>();
            if (getEntity().getParametersMapping() != null) {
                getEntity().getParametersMapping().forEach(item->tsParameterMappingMap.put(item.getEndpointParameter().getParameter(), item));
            }
            for (EndpointPathParameter endpointPathParameter : pathParametersDL.getSource()) {
                if (tsParameterMappingMap.containsKey(endpointPathParameter.getEndpointParameter().getParameter())) {
                    parameterMappings.add(tsParameterMappingMap.get(endpointPathParameter.getEndpointParameter().getParameter()));
                } else {
                    tsParameterMapping = new TSParameterMapping();
                    endpointParameter = new EndpointParameter();
                    endpointParameter.setEndpoint(entity);
                    endpointParameter.setParameter(endpointPathParameter.getEndpointParameter().getParameter());
                    tsParameterMapping.setEndpointParameter(endpointParameter);
                    parameterMappings.add(tsParameterMapping);
                }
            }
        }
        return parameterMappings;
    }

    public void setParameterMappings(List<TSParameterMapping> parameterMappings) {
        this.parameterMappings = parameterMappings;
    }

    @Override
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
        if (CollectionUtils.isNotEmpty(parameterMappings)) {
            getEntity().getParametersMapping().clear();
            getEntity().getParametersMapping().addAll(parameterMappings);
        } else {
            getEntity().getParametersMapping().clear();
        }
        if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getTarget())) {
            getEntity().getPathParameters().clear();
            getEntity().getPathParameters().addAll(pathParametersDL.getTarget());
        } else {
            getEntity().getPathParameters().clear();
        }
        return super.saveOrUpdate(killConversation);
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