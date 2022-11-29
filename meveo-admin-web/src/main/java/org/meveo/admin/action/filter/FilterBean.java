package org.meveo.admin.action.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.ModuleItemBaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.XmlUtil;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.filter.Filter;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.filter.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi
 **/
@Named
@ViewScoped
public class FilterBean extends ModuleItemBaseBean<Filter> {

	private static final long serialVersionUID = 6689238784280187702L;
	
	private static Logger log = LoggerFactory.getLogger(FilterBean.class);

	@Inject
	private FilterService filterService;
	
    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

	private List<CustomFieldTemplate> parameters;

	private boolean forceUpdateParameters;

	public FilterBean() {
		super(Filter.class);
	}

	@Override
	protected IPersistenceService<Filter> getPersistenceService() {
		return filterService;
	}

	@Override
    @ActionMethod
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

		String inputXml = entity.getInputXml();

		if (inputXml != null && !StringUtils.isBlank(inputXml)) {
			if (!XmlUtil.validate(inputXml)) {
				messages.error(new BundleKey("messages", "message.filter.invalidXml"));
				return "";
			}
		}
		forceUpdateParameters = true;
		return super.saveOrUpdate(killConversation);
	}

	

	public List<CustomFieldTemplate> getParameters() {
		if (parameters == null || forceUpdateParameters) {
			log.trace("Initializing filter parameters.");
			forceUpdateParameters = false;
			parameters = new ArrayList<>();
			if(this.getEntity() != null){
				Map<String, CustomFieldTemplate> customFieldTemplateMap = customFieldTemplateService.findByAppliesTo(this.getEntity());
				for (Map.Entry<String, CustomFieldTemplate> customFieldTemplateEntry : customFieldTemplateMap.entrySet()) {
					parameters.add(customFieldTemplateEntry.getValue());
				}
			}
			log.trace("Filter parameters initialized.");
		}
		return parameters;
	}

}
