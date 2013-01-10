/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.grieg.dunning.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.core.output.Output;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.grieg.output.CustomizedFieldEnum;
import org.meveo.model.payments.DunningActionTypeEnum;

import com.google.inject.Inject;

/**
 * Loads pdf from invoice in database and creates pdf file in temp dir, which
 * will be later used in outputHandler.
 * 
 * @author Ignas Lelys
 * @created Jan 27, 2011
 * 
 */
public class DunningOutputProducer implements OutputProducer {

    @Inject
    private GriegConfig griegConfig;

    private static final Logger logger = Logger.getLogger(DunningOutputProducer.class);

    @Override
    public Object produceOutput(List<Output> outputTickets) {
        List<DunningAction> files = new ArrayList<DunningAction>();
        String resDir = griegConfig.getResourcesFilesDirectory();
        try {
            for (Output output : outputTickets) {
                DunningOutput dunningOutput = (DunningOutput) output;
                DunningAction dunningAction = new DunningAction();
                dunningAction.setDunningTicket(dunningOutput.getDunningTicket());
                if (DunningActionTypeEnum.LETTER.name().equals(dunningOutput.getDunningTicket().getActionType())) {
                	String customerAccountName = getNotNull(dunningAction.getDunningTicket().getTitle())+" "+
                    getNotNull(dunningAction.getDunningTicket().getFirstName())+" "+
		             getNotNull(dunningAction.getDunningTicket().getLastName());
                	
                    File jasperFile = getJasperTemplateFile(resDir, dunningOutput.getDunningTicket().getTemplate());
                    logger.info(String.format("Jasper template used: %s", jasperFile.getCanonicalPath()));
                    InputStream reportTemplate = new FileInputStream(jasperFile);
                    JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportTemplate);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("DUE_AMOUNT", dunningOutput.getDunningTicket().getAmountWithTax());
                    params.put("CUSTOMER_ACCOUNT_CODE", dunningOutput.getDunningTicket().getCustomerAccountCode());
                    params.put("INVOICE_NUMBER", dunningOutput.getDunningTicket().getInvoiceNumber());
                    params.put("logoPath", resDir + File.separator + "dunning" + File.separator);
                    params.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_NAME.name(), getNotNull(customerAccountName));                    
                    params.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ADDRESS_1.name(),getNotNull( dunningAction.getDunningTicket().getAddress1()));
                    params.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ADDRESS_2.name(),getNotNull( dunningAction.getDunningTicket().getAddress2()));
                    params.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ADDRESS_3.name(),getNotNull(dunningAction.getDunningTicket().getAddress3()));                                                       
                    params.put(CustomizedFieldEnum.CUSTOMER_ACCOUNT_ZIP_CITY.name(),getNotNull(dunningAction.getDunningTicket().getZipCode())+" "+getNotNull(dunningAction.getDunningTicket().getCity()));                    
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params);
                    String fileName = griegConfig.getTempFilesDirectory()+File.separator+"maileva.001";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, fileName);
                    logger.info(String.format("PDF file '%s' produced", fileName));
                    dunningAction.setFileName(fileName);
                }
                files.add(dunningAction);
            }
        } catch (Exception e) {
            logger.error("Error producing PDF output", e);
            throw new ConfigurationException();
        }
        return files;
    }

    private File getJasperTemplateFile(String resDir, String template) {
        String jasperDirName = resDir + File.separator + "dunning";
        File jasperDir = new File(jasperDirName);
        String jasperTemplateName = template + ".jasper";
        File templateFile = new File(jasperDir, jasperTemplateName);
        return templateFile;
    }
    private String getNotNull(String str){
    	if(str == null){
    		return "";
    	}
    	return str;
    }
}
