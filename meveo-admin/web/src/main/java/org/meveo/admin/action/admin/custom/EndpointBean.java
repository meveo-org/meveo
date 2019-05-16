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
import java.util.*;

/**
 * Created by Hien Bach.
 */
@Named
@ViewScoped
public class EndpointBean extends BaseBean<Endpoint> {

    private static final long serialVersionUID = 1895532923500996522L;

    @Inject
    private EndpointService endpointService;

    private DualListModel<String> pathParametersDL;

    private List<TSParameterMapping> parameterMappings = new ArrayList<>();

    private List<String> returnedVariableNames;

    private String endpointUrl;

    private String serviceCode;

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
        if (endpoint.getService() != null) {
            serviceCode = endpoint.getService().getCode();
        }
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

    public DualListModel<String> getDualListModel() {
        List<String> perksTarget;
        if (pathParametersDL == null) {
            List<String> perksSource = new ArrayList<>();
            if (entity.getService() != null && CollectionUtils.isNotEmpty(entity.getService().getInputs())) {
                List<FunctionIO> functionIOList = entity.getService().getInputs();
                Set<String> parameterSources = new HashSet<>();
                perksTarget = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(getEntity().getPathParameters())) {
                    getEntity().getPathParameters().forEach(item->parameterSources.add(item.getEndpointParameter().getParameter()));
                }
                for (FunctionIO functionIO : functionIOList) {
                    if (!parameterSources.contains(functionIO.getName())) {
                        perksSource.add(functionIO.getName());
                    } else {
                        perksTarget.add(functionIO.getName());
                    }
                }
                pathParametersDL = new DualListModel<String>(perksSource, perksTarget);
            } else {
                perksTarget = new ArrayList<>();
                pathParametersDL = new DualListModel<String>(perksSource, perksTarget);
            }
        } else if (getEntity().getService() != null && !getEntity().getService().getCode().equals(serviceCode) && CollectionUtils.isNotEmpty(entity.getService().getInputs())) {
            List<FunctionIO> functionIOList = entity.getService().getInputs();
            List<String> perksSource = new ArrayList<>();
            for (FunctionIO functionIO : functionIOList) {
                perksSource.add(functionIO.getName());
            }
            perksTarget = new ArrayList<>();
            pathParametersDL = new DualListModel<String>(perksSource, perksTarget);
        }
        return pathParametersDL;
    }

    public void setDualListModel(DualListModel<String> pathParametersDL) {
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
            for (String endpointPathParameter : pathParametersDL.getSource()) {
                if (tsParameterMappingMap.containsKey(endpointPathParameter)) {
                    parameterMappings.add(tsParameterMappingMap.get(endpointPathParameter));
                } else {
                    tsParameterMapping = new TSParameterMapping();
                    endpointParameter = new EndpointParameter();
                    endpointParameter.setEndpoint(entity);
                    endpointParameter.setParameter(endpointPathParameter);
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
    
    /**
     * When function changes, reset returned variable name list, returned variable name and serialize result fields.
     */
    public void onFunctionChange(Object value) {
    	returnedVariableNames = null;
    	entity.setReturnedVariableName(null);
    	entity.setSerializeResult(false);
    }

    public String getEndpointUrl() {
        endpointUrl = "/rest/"+ getEntity().getCode();
        if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getTarget())) {
            pathParametersDL.getTarget().forEach(endpointPathParameter -> endpointUrl += "/{" + endpointPathParameter + "}");
        }
        return endpointUrl;
    }

    public List<String> getReturnedVariableNames() {
        if (returnedVariableNames == null) {
            returnedVariableNames = new ArrayList<>();
            if (entity.getService() != null && CollectionUtils.isNotEmpty(entity.getService().getOutputs())) {
                List<FunctionIO> functionIOList = entity.getService().getOutputs();
                functionIOList.forEach(item->returnedVariableNames.add(item.getName()));
            }
        } else if (getEntity().getService() != null && !getEntity().getService().getCode().equals(serviceCode) && CollectionUtils.isNotEmpty(entity.getService().getOutputs())) {
            List<FunctionIO> functionIOList = entity.getService().getOutputs();
            returnedVariableNames.clear();
            functionIOList.forEach(item->returnedVariableNames.add(item.getName()));
        }
        return returnedVariableNames;
    }

    public void setReturnedVariableNames(List<String> returnedVariableNames) {
        this.returnedVariableNames = returnedVariableNames;
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
            List<EndpointPathParameter> endpointPathParameters = new ArrayList<>();
            EndpointPathParameter endpointPathParameter;
            EndpointParameter endpointParameter;
            for (String parameter : pathParametersDL.getTarget()) {
                endpointPathParameter = new EndpointPathParameter();
                endpointParameter = new EndpointParameter();
                endpointParameter.setEndpoint(entity);
                endpointParameter.setParameter(parameter);
                endpointPathParameter.setEndpointParameter(endpointParameter);
                endpointPathParameters.add(endpointPathParameter);
            }
            getEntity().getPathParameters().clear();
            getEntity().getPathParameters().addAll(endpointPathParameters);
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