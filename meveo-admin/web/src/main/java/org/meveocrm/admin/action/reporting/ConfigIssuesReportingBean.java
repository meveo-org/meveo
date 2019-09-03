package org.meveocrm.admin.action.reporting;

import org.meveo.admin.action.BaseBean;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.BaseEntity;
import org.meveo.model.catalog.CounterTemplate;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.model.scripts.CustomScript;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CounterTemplateService;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.service.script.ScriptInstanceService;
import org.primefaces.event.TabChangeEvent;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 *
 */
@Named
@ViewScoped
public class ConfigIssuesReportingBean extends BaseBean<BaseEntity> {

    private static final long serialVersionUID = 1L;


    @Inject
    private ServiceTemplateService serviceTemplateService;

    @Inject
    private CounterTemplateService counterTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    private List<Entry<String, String>> jaspers;

    List<ServiceTemplate> servicesWithNotOfferList = new ArrayList<ServiceTemplate>();
    List<CounterTemplate> counterWithNotServicList = new ArrayList<CounterTemplate>();
    List<CustomScript> scriptInstanceWithErrorList = new ArrayList<CustomScript>();
    Map<String, String> jasperFilesList = new HashMap<String, String>();


    public void constructScriptInstancesWithError(TabChangeEvent event) {
        scriptInstanceWithErrorList = scriptInstanceService.getScriptInstancesWithError();
    }

    private Map<String, String> getJasperFiles() throws IOException {
        Map<String, String> jasperFiles = new HashMap<String, String>();
        ParamBean paramBean = paramBeanFactory.getInstance();
        String jasperCommercial = paramBean.getProperty("jasper.invoiceTemplate.commercial", "invoice.jasper");
        String jasperAdjustment = paramBean.getProperty("jasper.invoiceTemplate.adjustment", "invoice.jasper");
        // check jaspers files
        File jasperDir = new File(paramBeanFactory.getChrootDir() + File.separator + "jasper");
        if (!jasperDir.exists()) {
            jasperDir.mkdirs();
        }
        log.info("Jaspers template used :" + jasperDir.getPath());
        File[] foldersList = jasperDir.listFiles();
        String commercialRep = null;
        String adjustRep = null;
        File commercialInvoice = null;
        File adjustInvoice = null;
        if (foldersList != null && foldersList.length > 0) {
            for (File f : foldersList) {
                adjustRep = f.getCanonicalPath() + File.separator + "invoiceAdjustmentPdf";
                adjustInvoice = new File(adjustRep + File.separator + jasperCommercial);
                if (!adjustInvoice.exists()) {
                    jasperFiles.put(adjustRep, jasperAdjustment);
                }
                commercialRep = f.getCanonicalPath() + File.separator + "pdf";
                commercialInvoice = new File(commercialRep + File.separator + jasperCommercial);
                if (!commercialInvoice.exists()) {
                    jasperFiles.put(commercialRep, jasperCommercial);
                }
            }
        }
        return jasperFiles;
    }

    public void getJasperFilesNotFound(TabChangeEvent event) throws IOException {
        jasperFilesList = getJasperFiles();
        if (jasperFilesList != null && jasperFilesList.size() > 0) {
            jaspers = new ArrayList<>(jasperFilesList.entrySet());
        }
    }

    ConfigIssuesReportingDTO reportConfigDto;

    @PostConstruct
    public void init() {
        reportConfigDto = new ConfigIssuesReportingDTO();
        try {
            reportConfigDto.setNbrJasperDir(getJasperFiles().size());
        } catch (IOException e) {
            log.error("Failed to get number of jasper files", e);
        }
    }


    public long getNbrScriptInstanceWithError() {
        return scriptInstanceService.countScriptInstancesWithError();
    }

    public ConfigIssuesReportingDTO getReportConfigDto() {
        return reportConfigDto;
    }

    public List<ServiceTemplate> getServicesWithNotOfferList() {
        return servicesWithNotOfferList;
    }

    public List<CounterTemplate> getCounterWithNotServicList() {
        return counterWithNotServicList;
    }

    public List<CustomScript> getScriptInstanceWithErrorList() {
        return scriptInstanceWithErrorList;
    }

    @Override
    public IPersistenceService<BaseEntity> getPersistenceService() {
        return getPersistenceService();
    }

    @Override
    public String getEditViewName() {
        return "";
    }

    public List<Entry<String, String>> getJaspers() {
        return jaspers;
    }

    public void setJaspers(List<Entry<String, String>> jaspers) {
        this.jaspers = jaspers;
    }

}