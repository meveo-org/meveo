/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ImageUploadEventHandler;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.IEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.catalog.IImageUpload;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.filter.Filter;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.filter.FilterService;
import org.meveo.service.index.ElasticClient;
import org.meveo.util.ApplicationProvider;
import org.meveo.util.view.ESBasedDataModel;
import org.meveo.util.view.PagePermission;
import org.meveo.util.view.ServiceBasedLazyDataModel;
import org.omnifaces.cdi.Param;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.lapis.jsfexporter.csv.CSVExportOptions;

/**
 * Base bean class. Other backing beans extends this class if they need functionality it provides.
 * 
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 * 
 */
@Named
@ViewScoped
public abstract class BaseCrudBean<T extends IEntity, D extends BaseEntityDto> extends BaseBean<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private BaseCrudApi<T, D> baseCrudApi;
    
    private boolean override;

    /**
     * Constructor
     */
    public BaseCrudBean() {
        super();
    }
    
    public BaseCrudBean(Class<T> clazz) {
    	super(clazz);
    }

    
    @PostConstruct
    public void init() {
    	baseCrudApi = getBaseCrudApi();
    }
    
    public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

    
    public abstract BaseCrudApi<T, D> getBaseCrudApi();
    
	public StreamedContent exportXML() throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
        if(baseCrudApi == null) {
        	throw new BusinessException(getClass().getSimpleName() + " is not using a base crud api");
        }
        PaginationConfiguration configuration = new PaginationConfiguration(super.getFilters());
        File exportXML = baseCrudApi.exportXML(configuration);
        
        DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent();
        defaultStreamedContent.setContentEncoding("UTF-8");
        defaultStreamedContent.setContentType("application/xml");
        defaultStreamedContent.setStream(new FileInputStream(exportXML));
        defaultStreamedContent.setName(exportXML.getName());
        
        return defaultStreamedContent;
	}
	
	public StreamedContent exportJSON() throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
		if(baseCrudApi == null) {
			baseCrudApi = getBaseCrudApi();
		}
		
        if(baseCrudApi == null) {
        	throw new BusinessException(getClass().getSimpleName() + " is not using a base crud api");
        }
        PaginationConfiguration configuration = new PaginationConfiguration(getFilters());
        File exportJSON = baseCrudApi.exportJSON(configuration);
		
        DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent();
        defaultStreamedContent.setContentEncoding("UTF-8");
        defaultStreamedContent.setContentType("application/json");
        defaultStreamedContent.setStream(new FileInputStream(exportJSON));
        defaultStreamedContent.setName(exportJSON.getName());
        
		return defaultStreamedContent;
	}
	
	public StreamedContent exportCSV() throws JsonGenerationException, JsonMappingException, IOException, BusinessException {
        if(baseCrudApi == null) {
        	throw new BusinessException(getClass().getSimpleName() + " is not using a base crud api");
        }
        PaginationConfiguration configuration = new PaginationConfiguration(getFilters());
        File exportCSV = baseCrudApi.exportCSV(configuration);
		
        DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent();
        defaultStreamedContent.setContentEncoding("UTF-8");
        defaultStreamedContent.setContentType("application/csv");
        defaultStreamedContent.setStream(new FileInputStream(exportCSV));
        defaultStreamedContent.setName(exportCSV.getName());
        
		return defaultStreamedContent;
	}
	
	public void importData(FileUploadEvent event) throws IOException, BusinessException, MeveoApiException {
		if(baseCrudApi == null) {
			baseCrudApi = getBaseCrudApi();
		}
		
		String contentType = event.getFile().getContentType();
		InputStream inputStream = event.getFile().getInputstream();
		
		switch(contentType.trim()) {
			case "text/xml": 
			case "application/xml":
				baseCrudApi.importXML(inputStream, override);
				break;
				
			case "application/json": 
				baseCrudApi.importJSON(inputStream, override);
				break;
				
			case "text/csv":
			case "application/vnd.ms-excel":
				baseCrudApi.importCSV(inputStream, override);
				break;
				
		}
	}
    
    
}