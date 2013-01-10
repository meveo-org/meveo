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
package org.meveo.bayad.util;

import java.io.File;
import java.util.ResourceBundle;

import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.CsvBuilder;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.NumberUtils;
import org.meveo.model.payments.ActionDunning;
import org.meveo.model.payments.DunningLOT;

/**
 * DunningLotBuilder
 * 
 * @author anasseh
 * @created 07.12.2010
 * 
 */
public class DunningLotBuilder {
    private DunningLOT dunningLOT;
    private String fileName;

    public DunningLotBuilder(DunningLOT dunningLOT) {
        this.fileName = BayadConfig.getDunningLotFileName() + "_" + dunningLOT.getProvider().getCode() + "_" + dunningLOT.getActionType() + "_"
                + dunningLOT.getId() + BayadConfig.getDunningLotFileNameExtention();
        this.dunningLOT = dunningLOT;
    }

    public void exportToFile() throws Exception {
    	ResourceBundle resource = ResourceBundle.getBundle("messages");
        CsvBuilder csv = new CsvBuilder();
        csv.appendValue("ActionType");
        csv.appendValue("ProviderCode");
        csv.appendValue("CustomerAccountId");
        csv.appendValue("CustomerAccountCode");
        csv.appendValue("CustomerAccountDescription");
        csv.appendValue("Title");
        csv.appendValue("FirstName");
        csv.appendValue("LastName");  
        csv.appendValue("InvoiceReference");
        csv.appendValue("Sold");
        csv.appendValue("AmountInvoice");
        csv.appendValue("InvoiceDate");        
        csv.appendValue("CreationDate");
        csv.appendValue("FromLevel");
        csv.appendValue("ToLevel");
        csv.appendValue("LetterTemplate");
        csv.appendValue("Email");
        csv.appendValue("EmailCC");
        csv.appendValue("Address1");
        csv.appendValue("Address2");
        csv.appendValue("Address3");
        csv.appendValue("ZipCode");
        csv.appendValue("City");
        csv.appendValue("State");
        csv.appendValue("Country");
        csv.startNewLine();

        for (ActionDunning actionDunning : dunningLOT.getActions()) {
            String descTitle = null, firstName = null,lastName = null;
            
            if (actionDunning.getCustomerAccount().getName() != null) {
                if (actionDunning.getCustomerAccount().getName().getTitle() != null) {
                    descTitle = resource.getString(actionDunning.getCustomerAccount().getName().getTitle().getDescriptionKey());
                }      
                firstName = actionDunning.getCustomerAccount().getName().getFirstName();
                lastName =  actionDunning.getCustomerAccount().getName().getLastName();
            }
            csv.appendValue("" + actionDunning.getTypeAction());
            csv.appendValue("" + actionDunning.getCustomerAccount().getProvider().getCode());
            csv.appendValue("" + actionDunning.getCustomerAccount().getId());
            csv.appendValue("" + actionDunning.getCustomerAccount().getCode());
            csv.appendValue("" + actionDunning.getCustomerAccount().getDescription());
            csv.appendValue(getNotNull(descTitle));            
            csv.appendValue(getNotNull(firstName));
            csv.appendValue(getNotNull(lastName)); 
            csv.appendValue(actionDunning.getRecordedInvoice().getReference());
            csv.appendValue(NumberUtils.format(actionDunning.getAmountDue(),BayadConfig.getDecimalFormat()));
            csv.appendValue(NumberUtils.format(actionDunning.getRecordedInvoice().getAmount(),BayadConfig.getDecimalFormat()));
            csv.appendValue(DateUtils.formatDateWithPattern(actionDunning.getRecordedInvoice().getInvoiceDate(), "dd/MM/yyyy"));
            csv.appendValue(DateUtils.formatDateWithPattern(actionDunning.getCreationDate(), "dd/MM/yyyy"));
            csv.appendValue("" + actionDunning.getFromLevel());
            csv.appendValue("" + actionDunning.getToLevel());
            csv.appendValue(actionDunning.getActionPlanItem().getLetterTemplate() == null ? "" : getNotNull(actionDunning.getActionPlanItem().getLetterTemplate()
                    .toUpperCase()));
            csv.appendValue(actionDunning.getCustomerAccount().getPrimaryContact()==null? "" : getNotNull(actionDunning.getCustomerAccount().getPrimaryContact().getEmail()));
            csv.appendValue(actionDunning.getCustomerAccount().getExternalRef1());
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getAddress1()));
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getAddress2()));
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getAddress3()));
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getZipCode()));
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getCity()));
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getState()));
            csv.appendValue(actionDunning.getCustomerAccount().getAddress() == null ? "" : getNotNull(actionDunning.getCustomerAccount().getAddress().getCountry()));

            csv.startNewLine();
        }
        csv.toFile(BayadConfig.getDunningLotOutputDir() + File.separator + fileName);
    }

    public String getFileName() {
        return this.fileName;
    }

    private String getNotNull(String str) {
        if (str == null) {
            str = "";
        }
        return str;
    }
}
