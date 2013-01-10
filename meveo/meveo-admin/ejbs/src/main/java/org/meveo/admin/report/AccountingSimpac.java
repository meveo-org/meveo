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
package org.meveo.admin.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.bi.OutputFormatEnum;
import org.meveo.model.bi.Report;
import org.meveo.service.reporting.local.DWHAccountOperationServiceLocal;

@Name("accountingSimpac")
public class AccountingSimpac extends FileProducer implements Reporting {
    /** Logger. */
    @Logger
    protected Log log;

    private DWHAccountOperationServiceLocal accountOperationTransformationService;

    private String separator;

    private String reportsFolder;
    private String templateFilename;
    public Map<String, Object> parameters = new HashMap<String, Object>();

    public void generateSIMPACFile(String providerCode, Date startDate, Date endDate, OutputFormatEnum outputFormat) {
        try {
            File file = null;
            if (outputFormat == OutputFormatEnum.PDF) {
                file = File.createTempFile("tempAccountingSimpac", ".csv");
            } else if (outputFormat == OutputFormatEnum.CSV) {
                StringBuilder sb = new StringBuilder(getFilename(providerCode, startDate, endDate));
                sb.append(".csv");
                file = new File(sb.toString());
            }
            FileWriter writer = new FileWriter(file);
            writer.append("Ste;CG;CA;DA;CR;IC;IP;Debit;Credit");
            writer.append('\n');
            List<Object> accountOperations = accountOperationTransformationService.getObjectsForSIMPAC(providerCode,
                    startDate, endDate);
            Iterator<Object> itr = accountOperations.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                

                // line client side
                if (row[0] != null) {// accountingCode
                    writer.append(("" + row[0]).replace(separator.toCharArray()[0], ';') + ";");
                } else {
                    writer.append("00000;00000;0000;000;0000;00000000;00000;");
                }
               double amount = Double.parseDouble(""+row[2]); 
                if (amount>0) {// case debit
                    writer.append((amount + ";").replace('.', ','));// amount debit
                    writer.append("0;");// amount credit
                } else {
                    writer.append("0;");// amount debit
                    writer.append((amount*-1.0+ ";").replace('.', ','));// amount credit
                    
                }
                writer.append('\n');

                // line client side
                if (row[1] != null) {// accountingCodeClientSide
                    writer.append(("" + row[1]).replace(separator.toCharArray()[0], ';') + ";");
                } else {
                    writer.append("00000;00000;0000;000;0000;00000000;00000;");
                }
                if (amount>0) {// case credit on client side
                    writer.append("0;");// amount debit
                    writer.append((amount + ";").replace('.', ','));// amount credit
                } else {
                    writer.append((amount*-1.0+ ";").replace('.', ','));// amount debit
                    writer.append("0;");// amount credit
                    
                }
                writer.append('\n');
            }
            writer.flush();
            writer.close();
            if (outputFormat == OutputFormatEnum.PDF) {
                parameters.put("startDate", startDate);
                parameters.put("endDate", endDate);
                parameters.put("provider", providerCode);

                StringBuilder sb = new StringBuilder(getFilename(providerCode, startDate, endDate));
                sb.append(".pdf");
                generatePDFfile(file, sb.toString(), templateFilename, parameters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilename(String providerName, Date startDate, Date endDate) {
        String DATE_FORMAT = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        StringBuilder sb = new StringBuilder();
        sb.append(reportsFolder);
        sb.append(providerName + "_");
        sb.append("SIMPAC_TRESO_");
        sb.append(sdf.format(new Date()).toString());
        sb.append("_du_");
        sb.append(sdf.format(startDate).toString());
        sb.append("_au_");
        sb.append(sdf.format(endDate).toString());
        return sb.toString();
    }

    @Override
    public void export(Report report) {
        ParamBean param = ParamBean.getInstance("meveo-admin.properties");
        reportsFolder = param.getProperty("reportsURL");
        separator = param.getProperty("reporting.accountingCode.separator");
        String jasperTemplatesFolder = param.getProperty("reports.jasperTemplatesFolder");
        templateFilename = jasperTemplatesFolder + "accountingSimpac.jasper";
        accountOperationTransformationService = (DWHAccountOperationServiceLocal) Component
                .getInstance("DWHAccountOperationService");
        generateSIMPACFile(report.getProvider() == null ? null : report.getProvider().getCode(), report.getStartDate(),
                report.getEndDate(), report.getOutputFormat());

    }

}
