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
package org.meveo.grieg.dunning.input;

import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;
import org.grieg.ticket.GriegTicket;
import org.meveo.core.parser.AbstractTextParser;
import org.meveo.core.parser.CsvReader;
import org.meveo.core.parser.ParserException;
import org.meveo.grieg.dunning.ticket.DunningTicket;

/**
 * @author R.AITYAAZZA
 * @created 18 mars 11
 */
public class FileParser extends AbstractTextParser<GriegTicket> {

    protected final Logger logger = Logger.getLogger(this.getClass());

    private static final int actionTypeIndex = 0;
    private static final int providerCodeIndex = 1;
    private static final int idCustomerAccountIndex = 2;
    private static final int codeCustomerAccountIndex = 3;
    private static final int descriptionCustomerAccountIndex = 4;
    private static final int titleIndex = 5;
    private static final int firstNameIndex = 6;
    private static final int lastNameIndex = 7;  
    private static final int invoiceNumberIndex = 8;
    private static final int soldIndex = 9;
    private static final int amoutWithTaxIndex = 10;
    private static final int invoiceDateIndex = 11;
    private static final int processDateIndex = 12;
    private static final int templateIndex = 15;
    private static final int mailIndex = 16;
    private static final int mailCCIndex = 17;
    private static final int address1Index = 18;
    private static final int address2Index = 19;
    private static final int address3Index = 20;
    private static final int zipCodeIndex = 21;
    private static final int cityIndex = 22;
    private static final int stateIndex = 23;
    private static final int countryIndex = 24;

    CsvReader csvReader = null;
    boolean firstAccess = true;

    public FileParser() throws ParserException {

    }

    @Override
    public GriegTicket next() throws ParserException {
        DunningTicket data = null;
        try {
            long start = System.currentTimeMillis();
            if (firstAccess) {
                File file = new File(fileName);
                FileReader fileReader = new FileReader(file);
                csvReader = new CsvReader(fileReader, ';');
                if (csvReader.readRecord()) {
                    String[] headers = csvReader.getValues();
                    System.out.println("parseDunningFile columns : " + headers);
                }
                firstAccess = false;
            }
            if (csvReader.readRecord()) {
                data = new DunningTicket();
                String[] values = csvReader.getValues();
                System.out.println("parseDunningFile :" + csvReader.getRawRecord());
                data.setSource(csvReader.getRawRecord());
                data.setActionType(values[actionTypeIndex]);
                data.setProviderCode(values[providerCodeIndex]);
                data.setIdCustomerAccount(values[idCustomerAccountIndex]);
                data.setCustomerAccountCode(values[codeCustomerAccountIndex]);
                data.setCustomerAccountDescription(values[descriptionCustomerAccountIndex]);
                data.setAddress1(values[address1Index]);
                data.setAddress2(values[address2Index]);
                data.setAddress3(values[address3Index]);
                data.setZipCode(values[zipCodeIndex]);
                data.setCity(values[cityIndex]);
                data.setState(values[stateIndex]);
                data.setCountry(values[countryIndex]);
                data.setSold(values[soldIndex]);
                data.setAmountWithTax(values[amoutWithTaxIndex]);
                data.setInvoiceDate(values[invoiceDateIndex]);
                data.setInvoiceNumber(values[invoiceNumberIndex]);
                data.setMail(values[mailIndex]);
                data.setMailCC(values[mailCCIndex]);
                data.setFirstName(values[firstNameIndex]);
                data.setLastName(values[lastNameIndex]);  
                data.setTitle(values[titleIndex]);
                data.setProcessDate(values[processDateIndex]);
                data.setTemplate(values[templateIndex]);
            }

            time += (System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;

    }

}
