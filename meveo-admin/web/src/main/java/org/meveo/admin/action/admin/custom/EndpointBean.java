package org.meveo.admin.action.admin.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.keycloak.client.KeycloakAdminClientConfig;
import org.meveo.keycloak.client.KeycloakAdminClientService;
import org.meveo.keycloak.client.KeycloakUtils;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.technicalservice.endpoint.EndpointService;
import org.primefaces.model.DualListModel;

/**
 * Created by Hien Bach.
 * 
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Named
@ViewScoped
public class EndpointBean extends BaseBean<Endpoint> {

	private static final long serialVersionUID = 1895532923500996522L;

	@Inject
	private EndpointService endpointService;

	@Inject
	protected Messages messages;

	@EJB
	private KeycloakAdminClientService keycloakAdminClientService;

	private DualListModel<String> pathParametersDL;

	private List<TSParameterMapping> parameterMappings = new ArrayList<>();

	private List<String> returnedVariableNames;

	private String endpointUrl;

	private String serviceCode;

	private DualListModel<String> rolesDM;

	/**
	 * Constructor. Invokes super constructor and provides class type of this bean
	 * for {@link BaseBean}.
	 */
	public EndpointBean() {
		super(Endpoint.class);
	}

	/**
	 * Factory method for entity to edit. If objectId param set load that entity
	 * from database, otherwise create new.
	 * 
	 * @return currency
	 */
	@Override
	public Endpoint initEntity() {
		Endpoint endpoint = super.initEntity();

		return endpoint;
	}

	@Override
	protected List<String> getFormFieldsToFetch() {
		return Arrays.asList("service", "roles", "pathParameters", "parametersMapping");
	}

	@Override
	protected List<String> getListFieldsToFetch() {
		return Arrays.asList("service", "roles");
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
				if (CollectionUtils.isNotEmpty(getEntity().getPathParametersNullSafe())) {
					getEntity().getPathParametersNullSafe().stream()
							.filter(item -> item != null && item.getEndpointParameter() != null)
							.forEach(item -> parameterSources.add(item.getEndpointParameter().getParameter()));
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
			
		} else if (getEntity().getService() != null && !getEntity().getService().getCode().equals(serviceCode)
				&& CollectionUtils.isNotEmpty(entity.getService().getInputs())) {
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
			if (getEntity().getParametersMappingNullSafe() != null) {
				getEntity().getParametersMappingNullSafe()
						.forEach(item -> tsParameterMappingMap.put(item.getEndpointParameter().getParameter(), item));
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
	 * When function changes, reset returned variable name list, returned variable
	 * name and serialize result fields.
	 */
	public void onFunctionChange() {
		List<FunctionIO> functionIOList = entity.getService().getOutputs();
		if (CollectionUtils.isNotEmpty(functionIOList)) {
			if (entity.getReturnedVariableName() != null) {
				for (FunctionIO functionIO : functionIOList) {
					if (entity.getReturnedVariableName().equals(functionIO.getName())) {
						if (functionIO.getType().startsWith("Map")) {
							entity.setSerializeResult(true);
						} else {
							entity.setSerializeResult(false);
						}

						break;
					}
				}
			}
		}
	}

	public String getEndpointUrl() {
		endpointUrl = "/rest/" + getEntity().getCode();
		if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getTarget())) {
			pathParametersDL.getTarget()
					.forEach(endpointPathParameter -> endpointUrl += "/{" + endpointPathParameter + "}");
		}
		return endpointUrl;
	}

	public List<String> getReturnedVariableNames() {
		if (returnedVariableNames == null) {
			returnedVariableNames = new ArrayList<>();
			if (entity.getService() != null && CollectionUtils.isNotEmpty(entity.getService().getOutputs())) {
				List<FunctionIO> functionIOList = entity.getService().getOutputs();
				functionIOList.forEach(item -> returnedVariableNames.add(item.getName()));
			}
			
		} else if (getEntity().getService() != null && !getEntity().getService().getCode().equals(serviceCode)
				&& CollectionUtils.isNotEmpty(entity.getService().getOutputs())) {
			List<FunctionIO> functionIOList = entity.getService().getOutputs();
			returnedVariableNames.clear();
			functionIOList.forEach(item -> returnedVariableNames.add(item.getName()));
		}
		return returnedVariableNames;
	}

	public void setReturnedVariableNames(List<String> returnedVariableNames) {
		this.returnedVariableNames = returnedVariableNames;
	}

	public DualListModel<String> getRolesDM() {
		if (rolesDM == null) {
			KeycloakAdminClientConfig keycloakAdminClientConfig = KeycloakUtils.loadConfig();
			List<String> perksSource = keycloakAdminClientService.getCompositeRolesByRealmClientId(
					keycloakAdminClientConfig.getClientId(), keycloakAdminClientConfig.getRealm());
			List<String> perksTarget = new ArrayList<>();
			if (getEntity().getRoles() != null) {
				perksTarget.addAll(getEntity().getRoles());
			}
			perksSource.removeAll(perksTarget);
			rolesDM = new DualListModel<>(perksSource, perksTarget);

		}
		rolesDM.getSource().remove(EndpointService.ENDPOINT_MANAGEMENT);
		return rolesDM;
	}

	public void setRolesDM(DualListModel<String> rolesDM) {
		this.rolesDM = rolesDM;
	}

	@Override
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

//		if (CollectionUtils.isNotEmpty(parameterMappings)) {
//			entity.getParametersMappingNullSafe().clear();
//			entity.getParametersMappingNullSafe().addAll(parameterMappings);
//
//		} else {
//			entity.getParametersMappingNullSafe().clear();
//		}
//
//		if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getTarget())) {
//			List<EndpointPathParameter> endpointPathParameters = new ArrayList<>();
//			EndpointPathParameter endpointPathParameter;
//			EndpointParameter endpointParameter;
//			for (String parameter : pathParametersDL.getTarget()) {
//				endpointPathParameter = new EndpointPathParameter();
//				endpointParameter = new EndpointParameter();
//				endpointParameter.setEndpoint(entity);
//				endpointParameter.setParameter(parameter);
//				endpointPathParameter.setEndpointParameter(endpointParameter);
//				endpointPathParameters.add(endpointPathParameter);
//			}
//			entity.getPathParametersNullSafe().clear();
//			entity.getPathParametersNullSafe().addAll(endpointPathParameters);
//
//		} else {
//			entity.getPathParametersNullSafe().clear();
//		}
//
//		if (CollectionUtils.isNotEmpty(entity.getRoles())) {
//			entity.getRoles().clear();
//			entity.getRoles().addAll(rolesDM.getTarget());
//
//		} else {
//			entity.setRoles(new HashSet<>(rolesDM.getTarget()));
//		}
//
//		boolean isError = false;
//		if (parameterMappings != null) {
//			for (TSParameterMapping param : parameterMappings) {
//				if (StringUtils.isBlank(param.getParameterName())) {
//					if (param.getDefaultValue() == null) {
//						messages.error(new BundleKey("messages", "endpoint.parameters.mapping.default.error"),
//								param.getEndpointParameter().getParameter());
//						isError = true;
//					}
//				}
//			}
//		}
//
//		if (isError) {
//			return null;
//		}

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