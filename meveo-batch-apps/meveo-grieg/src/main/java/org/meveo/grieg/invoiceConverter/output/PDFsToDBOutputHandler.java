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
package org.meveo.grieg.invoiceConverter.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.process.step.Constants;
import org.meveo.model.billing.Invoice;
import org.meveo.persistence.MeveoPersistence;

/**
 * This handler save file outputs to DB as blobs. 
 * 
 * @author Ignas Lelys
 * @created Jan 3, 2011
 *
 */
public class PDFsToDBOutputHandler implements OutputHandler {

    private static final Logger logger = Logger.getLogger(PDFsToDBOutputHandler.class);
    
    @SuppressWarnings("unchecked")
    public void handleOutput(TaskExecution taskExecution) {
        Object outputObject = taskExecution.getOutputObject();
        if (outputObject != null) {
            if (!(outputObject instanceof List)) {
                logger.error(String.format("In TaskExecution context wrong type of argument was put %s parameter. If you want to use MultipleFileOutputHandler it must be List<String> with list of output file names in temp directory", Constants.OUTPUT_OBJECT));
                throw new IllegalStateException("Wrong outputObject type");
            }
            List<FileInfoHolder> files = (List<FileInfoHolder>)outputObject;
            logger.info("Saving output files to DB:");
            for (FileInfoHolder fileInfo : files) {
                Long invoiceId = fileInfo.getInvoice().getId();
                String fileName = fileInfo.getFileName();
                
                logger.info(String.format("Save output file %s to %s table where invoice id = '%s'", fileName, "BILLING_INVOICE", invoiceId));
                
                EntityManager em = MeveoPersistence.getEntityManager();
                FileInputStream fileInputStream = null;
                try {
                    File file = new File(fileName);
                    long fileSize = file.length();
                    if (fileSize > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("File is too big to put it to buffer in memory");
                    }
                    byte[] fileBytes = new byte[(int)file.length()];
                    Invoice invoice = (Invoice)em.find(Invoice.class, invoiceId);
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(fileBytes);
                    invoice.setPdf(fileBytes);
                    em.merge(invoice);
                } catch (Exception e) {
                    logger.error("Error handling file.", e);
                    throw new ConfigurationException("Error saving file to DB as blob.");
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            logger.error("Error closing file input stream.", e);
                        }
                    }
                }
                em.flush();
            }
        }
    }
    
}
