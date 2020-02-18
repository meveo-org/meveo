/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

import java.io.*;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.model.IEntity;
import org.meveo.model.module.MeveoModule;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.meveo.commons.utils.FileUtils.addToZipFile;

/**
 * Base bean class. Other backing beans extends this class if they need functionality it provides.
 *
 * @author Clément Bareth
 * @author Wassim Drira
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Named
@ViewScoped
public abstract class BaseCrudBean<T extends IEntity, D extends BaseEntityDto> extends BaseBean<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    private BaseCrudApi<T, D> baseCrudApi;
    
    private boolean override;

    private ExportFormat exportFormat;

    public BaseCrudBean() {
        super();
    }
    
    public BaseCrudBean(Class<T> clazz) {
    	super(clazz);
    }

    @Override
    @PostConstruct
    public void init() {
    	baseCrudApi = getBaseCrudApi();
    }
    
    @Override
    public boolean isOverride() {
		return override;
	}

	@Override
    public void setOverride(boolean override) {
		this.override = override;
	}

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    @Override
    public abstract BaseCrudApi<T, D> getBaseCrudApi();

	public StreamedContent exportXML() throws IOException, BusinessException {
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
	
	public StreamedContent exportJSON() throws IOException, BusinessException {
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
	
	public StreamedContent exportCSV() throws IOException, BusinessException {
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
		String fileName = event.getFile().getFileName();
		
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


            case "application/octet-stream":
            case "application/x-zip-compressed":
                baseCrudApi.importZip(fileName, inputStream, override);
                break;
		}
	}

	public DefaultStreamedContent export() throws BusinessException, IOException {
		baseCrudApi = baseCrudApi == null ? getBaseCrudApi() : baseCrudApi;
		
        if(baseCrudApi == null) {
            throw new BusinessException(getClass().getSimpleName() + " is not using a base crud api");
        }
        
        DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent();

        File exportFile = baseCrudApi.exportEntities(exportFormat, getSelectedEntities());
        try {
            String exportName = exportFile.getName();
            String[] exportFileName = exportName.split("_");
            String name = exportFileName[1];
            if (name.startsWith("MeveoModule")) {
                String[] moduleName = name.split("\\.");
                String fileName = moduleName[0];
                List<MeveoModule> meveoModules = (List<MeveoModule>) getSelectedEntities();
                for (int i = 0; i < meveoModules.size(); i++) {
                    if (CollectionUtils.isNotEmpty(meveoModules.get(i).getModuleFiles())) {
                        String providerRoot = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
                        byte[] filedata = createZipFile(exportFile.getAbsolutePath(), meveoModules, providerRoot);
                        InputStream is = new ByteArrayInputStream(filedata);
                        return new DefaultStreamedContent(is, "application/octet-stream", fileName + ".zip");
                    }
                }
            }
        } catch (Exception e) {
        }

        defaultStreamedContent.setContentEncoding("UTF-8");
        defaultStreamedContent.setStream(new FileInputStream(exportFile));
        defaultStreamedContent.setName(exportFile.getName());

        return defaultStreamedContent;
    }

	public ExportFormat[] getExportFormats(){
	    return ExportFormat.values();
    }

    /**
     * Compress module and its files into byte array.
     *
     * @param exportFile file to export
     * @param meveoModules list of meveo modules
     * @param providerRoot
     * @return zip file as byte array
     * @throws Exception exception.
     */
    public static byte[] createZipFile(String exportFile, List<MeveoModule> meveoModules, String providerRoot) throws Exception {

        Logger log = LoggerFactory.getLogger(FileUtils.class);
        log.info("Creating zip file for {}", exportFile);

        ZipOutputStream zos = null;
        ByteArrayOutputStream baos = null;
        CheckedOutputStream cos = null;
        try {
            baos = new ByteArrayOutputStream();
            cos = new CheckedOutputStream(baos, new CRC32());
            zos = new ZipOutputStream(new BufferedOutputStream(cos));
            File sourceFile = new File(exportFile);
            addToZipFile(sourceFile, zos, null);
           for (MeveoModule meveoModule: meveoModules) {
               if (CollectionUtils.isNotEmpty(meveoModule.getModuleFiles())) {
                   for (String pathFile : meveoModule.getModuleFiles()) {
                       File file = new File(providerRoot + pathFile);
                       addToZipFile(file, zos, null);
                   }
               }
           }
            zos.flush();
            zos.close();
            return baos.toByteArray();

        } finally {
            IOUtils.closeQuietly(zos);
            IOUtils.closeQuietly(cos);
            IOUtils.closeQuietly(baos);
        }
    }

}