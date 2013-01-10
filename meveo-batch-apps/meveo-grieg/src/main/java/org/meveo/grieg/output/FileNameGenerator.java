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
package org.meveo.grieg.output;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utils for maileva files names and date formats.
 * 
 * @author Ignas Lelys
 * @created Jan 27, 2011
 * 
 */
public class FileNameGenerator {

    private static String DATE_PATERN = "yyyy.MM.dd";

    /**
     * Formats file name from provided parameters and add a sequence number (001 for now)
     */
    public static String getNameWSequence(String tempDir, Date invoiceDate, String invoiceNumber) {
        return new StringBuilder(tempDir).append(File.separator).append(
                FileNameGenerator.formatInvoiceDate(invoiceDate)).append("_").append(invoiceNumber).append(
                "_").append(".001").toString();
    }

    /**
     * Formats file name from provided parameters and do not add a sequence number
     */
    public static String getNameWoutSequence(String tempDir, Date invoiceDate, String invoiceNumber) {
        return new StringBuilder(tempDir).append(File.separator).append(
                FileNameGenerator.formatInvoiceDate(invoiceDate)).append("_").append(invoiceNumber).toString();
    }
    
    /**
     * Format invoice date.
     */
    public static String formatInvoiceDate(Date invoiceDate) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATERN);
        return dateFormat.format(invoiceDate);
    }

}
