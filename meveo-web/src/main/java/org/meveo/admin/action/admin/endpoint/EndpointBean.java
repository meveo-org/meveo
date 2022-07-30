package org.meveo.admin.action.admin.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.keycloak.client.KeycloakAdminClientService;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.technicalservice.endpoint.EndpointService;
import org.primefaces.event.TransferEvent;
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
	private EndpointApi endpointApi;

	@Inject
	private ScriptInstanceService scriptInstanceService;

	@Inject
	protected Messages messages;

	@EJB
	private KeycloakAdminClientService keycloakAdminClientService;

	private DualListModel<String> pathParametersDL;

	private List<TSParameterMapping> parameterMappings = new ArrayList<>();

	private List<String> returnedVariableNames;

	private String endpointUrl;
	
	private String path;

	private String basePath;

	private String serviceCode;

	private ScriptInstance scriptInstance = null;
	
	private boolean pathDirty = false;

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
		entity = super.initEntity();
		
		if (entity.getPath().length() > 1) {
			pathDirty = true;
		}
		
		return entity;
	}

	@Override
	protected List<String> getFormFieldsToFetch() {
		return Arrays.asList("service", "pathParameters", "parametersMapping");
	}

	@Override
	protected List<String> getListFieldsToFetch() {
		return Arrays.asList("service");
	}

	@Override
	protected String getListViewName() {
		return "technicalServiceEndpoints";
	}

	@Override
	protected IPersistenceService<Endpoint> getPersistenceService() {
		return endpointService;
	}

	public DualListModel<String> getPathParametersDL() {

		List<String> perksTarget;
		if (pathParametersDL == null) {
			List<String> perksSource = new ArrayList<>();
			if (scriptInstance != null && CollectionUtils.isNotEmpty(scriptInstance.getInputs())) {
				List<FunctionIO> functionIOList = scriptInstance.getInputs();
				Set<String> parameterTarget = new HashSet<>();
				perksTarget = new ArrayList<>();
				if (entity.getPathParameters() != null && CollectionUtils.isNotEmpty(entity.getPathParameters())) {
					entity.getPathParameters().stream()
							.filter(item -> item != null && item.getEndpointParameter() != null)
							.forEach(item -> parameterTarget.add(item.getEndpointParameter().getParameter()));
				}

				for (FunctionIO functionIO : functionIOList) {
					if (!parameterTarget.contains(functionIO.getName())) {
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

		} else if (scriptInstance != null && !scriptInstance.getCode().equals(serviceCode)) {
			List<FunctionIO> functionIOList = scriptInstance.getInputs();
			List<String> perksSource = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(functionIOList)) {
				for (FunctionIO functionIO : functionIOList) {
					perksSource.add(functionIO.getName());
				}
			}
			perksTarget = new ArrayList<>();
			pathParametersDL = new DualListModel<String>(perksSource, perksTarget);
		}

		return pathParametersDL;
	}

	public void setPathParametersDL(DualListModel<String> pathParametersDL) {
		this.pathParametersDL = pathParametersDL;
		
		List<String> pathParams = this.pathParametersDL.getTarget();
		if (pathParams != null && CollectionUtils.isNotEmpty(pathParams)) {
			final List<EndpointPathParameter> entityPathParameters = new ArrayList<>(entity.getPathParametersNullSafe());
			pathParams.stream().filter(e -> entityPathParameters.stream().noneMatch(f -> e.equals(f.getEndpointParameter().getParameter()))).collect(Collectors.toList())
					.forEach(g -> {
						EndpointPathParameter endpointPathParameter = new EndpointPathParameter();
						endpointPathParameter.setEndpointParameter(buildEndpointParameter(entity, g));
						entity.getPathParameters().add(endpointPathParameter);
					});

			entityPathParameters.stream().filter(e -> pathParams.stream().noneMatch(f -> e.getEndpointParameter().getParameter().equals(f))).collect(Collectors.toList())
					.forEach(g -> entity.getPathParameters().remove(g));
		}
	}

	public List<TSParameterMapping> getParameterMappings() {

		parameterMappings.clear();
		if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getSource())) {
			TSParameterMapping tsParameterMapping;
			EndpointParameter endpointParameter;
			Map<String, TSParameterMapping> tsParameterMappingMap = new HashMap<>();
			if (entity.getParametersMapping() != null) {
				entity.getParametersMapping()
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
					tsParameterMapping.setMultivalued(Endpoint.isParameterMultivalued(scriptInstance, tsParameterMapping));
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
		if (scriptInstance != null) {
			List<FunctionIO> functionIOList = scriptInstance.getOutputs();
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
	}

	public String getEndpointUrl() {
		/*endpointUrl = "/rest/" + entity.getCode();
		if (pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getTarget())) {
			pathParametersDL.getTarget()
					.forEach(endpointPathParameter -> endpointUrl += "/{" + endpointPathParameter + "}");
		}
		return endpointUrl;*/
		return entity.getEndpointUrl();
	}

	public List<String> getReturnedVariableNames() {
		if (returnedVariableNames == null) {
			returnedVariableNames = new ArrayList<>();
			if (scriptInstance != null && CollectionUtils.isNotEmpty(scriptInstance.getOutputs())) {
				List<FunctionIO> functionIOList = scriptInstance.getOutputs();
				functionIOList.forEach(item -> returnedVariableNames.add(item.getName()));
			}

		} else if (scriptInstance != null && !scriptInstance.getCode().equals(serviceCode)) {
			List<FunctionIO> functionIOList = scriptInstance.getOutputs();
			returnedVariableNames.clear();
			if (CollectionUtils.isNotEmpty(functionIOList)) {
				functionIOList.forEach(item -> returnedVariableNames.add(item.getName()));
			}
		}
		return returnedVariableNames;
	}

	public void setReturnedVariableNames(List<String> returnedVariableNames) {
		this.returnedVariableNames = returnedVariableNames;
	}

	private EndpointParameter buildEndpointParameter(Endpoint endpoint, String param) {
		EndpointParameter endpointParameter = new EndpointParameter();
		endpointParameter.setEndpoint(endpoint);
		endpointParameter.setParameter(param);
		return endpointParameter;
	}

	@ActionMethod
	@Override
	@Transactional
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
		if (CollectionUtils.isNotEmpty(parameterMappings)) {
			final List<TSParameterMapping> entityParameterMappings = new ArrayList<>(entity.getParametersMappingNullSafe());
			parameterMappings.stream().filter(e -> entityParameterMappings.stream().noneMatch(f -> e.equals(f)))
					.collect(Collectors.toList()).forEach(g -> entity.getParametersMapping().add(g));
			entityParameterMappings.stream().filter(e -> parameterMappings.stream().noneMatch(f -> e.equals(f)))
					.collect(Collectors.toList()).forEach(g -> entity.getParametersMapping().remove(g));

		} else {
			entity.getParametersMappingNullSafe().clear();
		}

		if (!(pathParametersDL != null && CollectionUtils.isNotEmpty(pathParametersDL.getTarget()))) {
			entity.getPathParametersNullSafe().clear();
		}

		var dto = endpointApi.toDto(entity);
		try {
			endpointApi.createOrUpdate(dto);

			if (entity.isTransient()) {
				entity = endpointService.findByCode(dto.getCode());
				setObjectId(entity.getId());
			}

		} catch (Exception e) {
			messages.error("Entity can't be saved. Please retry.");
			throw new BusinessException(e);
		}
		String message = entity.isTransient() ? "save.successful" : "update.successful";
        messages.info(new BundleKey("messages", message));

		return "technicalServiceEndpoints";
	}

	@Override
	public String getNewViewName() {
		return "technicalServiceEndpointDetail";
	}

	@Override
	public String getEditViewName() {
		return "technicalServiceEndpointDetail";
	}

	public ScriptInstance getScriptInstance() {
		if (entity.getService() != null) {
			scriptInstance = scriptInstanceService.findById(entity.getService().getId());
		}
		return scriptInstance;
	}

	public void setScriptInstance(ScriptInstance scriptInstance) {
		this.scriptInstance = scriptInstance;
	}
	
	public void onPathParametersTransfer(TransferEvent event) throws Exception {
		
		if (!pathDirty) {
			StringBuilder newPath = new StringBuilder("");
			String sep = "/";
			for (EndpointPathParameter endpointPathParameter : entity.getPathParameters()) {
				newPath.append(sep).append("{").append(endpointPathParameter).append("}");
			}

			entity.setPath(newPath.toString());
		}
	}

	public boolean isPathDirty() {
		return pathDirty;
	}

	public void setPathDirty(boolean pathDirty) {
		this.pathDirty = pathDirty;
	}
}