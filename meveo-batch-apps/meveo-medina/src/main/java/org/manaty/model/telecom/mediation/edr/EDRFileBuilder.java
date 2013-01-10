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
package org.manaty.model.telecom.mediation.edr;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.meveo.model.rating.EDR;

/**
 * EDR File Builder.
 * 
 * @author seb
 * @created Aug 6, 2012
 */
public class EDRFileBuilder {
    
    private static final Logger logger = Logger.getLogger(EDRFileBuilder.class);
    
    protected static final String SEPARATOR = ";";
    
    protected final Format timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    
    protected PrintWriter writer;
    
    public EDRFileBuilder(String filename) {
        try {
            writer = new PrintWriter(filename);
        } catch (FileNotFoundException e) {
           logger.error("Could not open file for writing", e);
           throw new RuntimeException("EDRFileBuilder can not be created", e);
        }
    }

    /**
     * Append single EDR to EDR output file.
     * 
     * @param edr EDR data.
     * @return
     */
   public String append(EDR edr) {
        
        StringBuilder entryBuilder = new StringBuilder();

        entryBuilder.append(getCSVValue(edr.getCustomText1()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomText2()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomText3()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomText4()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(timeFormat.format(edr.getCustomDate1()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(timeFormat.format(edr.getCustomDate2()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(timeFormat.format(edr.getCustomDate3()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(timeFormat.format(edr.getCustomDate4()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomNumber1()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomNumber2()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomNumber3()));
        entryBuilder.append(SEPARATOR);
        entryBuilder.append(getCSVValue(edr.getCustomNumber4()));
        String line = entryBuilder.toString();
        writer.println(line);
        
        return line;
    }


    /**
     * Get value to write to CSV file.
     * 
     * @param value
     *            Primary value.
     * @return CSV value.
     */
    protected String getCSVValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
    
    /**
     * Close allocated resources.
     */
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
