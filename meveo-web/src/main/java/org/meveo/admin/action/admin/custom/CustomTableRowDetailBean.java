/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.admin.action.admin.custom;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.CETUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.BinaryStoragePathParam;
import org.meveo.service.storage.FileSystemService;
import org.meveo.service.storage.RepositoryService;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;

/**
 * @author Clement Bareth
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.3.0
 */
@Named
@ViewScoped
public class CustomTableRowDetailBean extends CustomTableBean implements Serializable{

    private static final long serialVersionUID = -2748591950645172132L;
    
    @Inject
    private FileSystemService fileSystemService;
    
    @Inject
    private RepositoryService repositoryService;
    
    private CustomFieldValues values;
    
    private Collection<CustomFieldTemplate> fields;
    
    private CustomFieldTemplate selectedCft;
    
    private String cetCode;
    
    private Repository repository;
    
    private List<String> filesToDeleteOnExit = new ArrayList<>();
    
    public void initEntity(String cetCode, Map<String, Object> valuesMap, Collection<CustomFieldTemplate> fields) {
    	this.cetCode = cetCode;
    	this.fields = fields;
    	
    	values = CETUtils.initCustomFieldValues(valuesMap, fields);
    }
	
	@SuppressWarnings("unchecked")
    @ActionMethod
    @Override
    public void onEntityReferenceSelected(SelectEvent event) {
		Map<String, Object> selectedEntityInPopup = (Map<String,Object>) event.getObject();
		String newId = (String) selectedEntityInPopup.get("uuid");
    	CustomFieldValue cfValue = values.getCfValue(selectedCft.getDbFieldname());
    	if (selectedCft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
    		List<String> listValue = cfValue.getListValue();
    		if(listValue == null) {
    			listValue = new ArrayList<String>();
        		listValue.add(newId);
    		} else {
        		listValue.add(newId);
        		listValue = listValue.stream().distinct().collect(Collectors.toList());
    		}
    		cfValue.setListValue(listValue);
    	} else {
    		cfValue.setStringValue(newId);
    	}
	}
	
	@Override
	@ActionMethod
	public void onChildEntityUpdated(CustomFieldValues cfValues) {
    	CustomFieldValue cfValue = values.getCfValue(selectedCft.getDbFieldname());
    	if(selectedCft.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
    		List<Map<?,?>> listValue = cfValue.getListValue();
    		if(listValue == null) {
    			listValue = new ArrayList<>();
        		listValue.add(cfValues.getValues());
    		} else {
        		listValue.add(cfValues.getValues());
        		listValue = listValue.stream().distinct().collect(Collectors.toList());
    		}
    		
    		cfValue.setListValue(listValue);
    	} else {
    		String serializedValues = JacksonUtil.toString(cfValues.getValues());
        	cfValue.setStringValue(serializedValues);
    	}
	}
	
    public void handleBinaryFileUpload(FileUploadEvent event) throws IOException {
    	UploadedFile uploadedBinaryFile = event.getFile();
    	
		Map<String, Object> attrs = event.getComponent().getAttributes();
		String uuid = (String) attrs.get("uuid");
		String cetCode = (String) attrs.get("cetCode");
		CustomFieldTemplate cft = (CustomFieldTemplate) attrs.get("cft");
		CustomFieldValue cfv = (CustomFieldValue) attrs.get("cfv");
		String strIsSingle = (String) attrs.get("isSingle");
		boolean isSingle = Boolean.parseBoolean(strIsSingle);
		Repository repository = (Repository) attrs.get("repository");
		CustomFieldValues fieldValues = (CustomFieldValues) attrs.get("values");
		
		log.info("" + repository);

		if (repository != null) {
			repository = repositoryService.retrieveIfNotManaged(repository);
		}

		String rootPath = repository != null && repository.getBinaryStorageConfiguration() != null ? repository.getBinaryStorageConfiguration().getRootPath() : "";

		
		BinaryStoragePathParam params = new BinaryStoragePathParam();
		params.setShowOnExplorer(cft.isSaveOnExplorer());
		params.setRootPath(rootPath);
		params.setCetCode(cetCode);
		params.setUuid(uuid);
		params.setCftCode(cft.getCode());
		params.setFilePath(cft.getFilePath());
		params.setContentType(uploadedBinaryFile.getContentType());
		params.setFilename(uploadedBinaryFile.getFileName());
		params.setInputStream(uploadedBinaryFile.getInputstream());
		params.setFileSizeInBytes(uploadedBinaryFile.getSize());
		params.setFileExtensions(cft.getFileExtensions());
		params.setContentTypes(cft.getContentTypes());
		params.setMaxFileSizeAllowedInKb(cft.getMaxFileSizeAllowedInKb());
		
		rootPath = fileSystemService.persists(params, fieldValues.getValues());
		
		log.debug("binary path={}", rootPath);
		
		if (isSingle) {
			cfv.setStringValue(rootPath);

		} else {
			List<Object> listValue = cfv.getListValue();
			if (listValue == null) {
				listValue = new ArrayList<Object>();
			}
			listValue.add(rootPath);
			cfv.setListValue(listValue);
		}
		
		repository = null;
    }
    
	public void deleteDeletedFiles() {
		if (filesToDeleteOnExit != null && !filesToDeleteOnExit.isEmpty()) {
			for (String file : filesToDeleteOnExit) {
				Path path = Paths.get(file);
				try {
					Files.delete(path);
					
				} catch (IOException e) {
					log.debug("File does not exists {}", path.getFileName());
				}
			}
		}
	}
	
	@ActionMethod
	public void onListElementUpdated() {
		
	}
    
	public CustomFieldValues getValues() {
		return values;
	}
	
	public Map<String, Object> getValuesMap(){
		return values.getValues();
	}

	public void setValues(CustomFieldValues values) {
		this.values = values;
	}

	@Override
    public Collection<CustomFieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Collection<CustomFieldTemplate> fields) {
		this.fields = fields;
	}

	public String getCetCode() {
		return cetCode;
	}

	public CustomFieldTemplate getSelectedCft() {
		return selectedCft;
	}

	public void setSelectedCft(CustomFieldTemplate selectedCft) {
		this.selectedCft = selectedCft;
	}

	public void setCetCode(String cetCode) {
		this.cetCode = cetCode;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public List<String> getFilesToDeleteOnExit() {
		return filesToDeleteOnExit;
	}

	public void setFilesToDeleteOnExit(List<String> filesToDeleteOnExit) {
		this.filesToDeleteOnExit = filesToDeleteOnExit;
	}
	
}