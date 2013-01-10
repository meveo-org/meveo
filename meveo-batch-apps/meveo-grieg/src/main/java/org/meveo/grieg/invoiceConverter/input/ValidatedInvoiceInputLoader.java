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
package org.meveo.grieg.invoiceConverter.input;

import java.io.File;

import org.grieg.GriegConfig;
import org.meveo.commons.utils.FileUtils;
import org.meveo.core.inputloader.SimpleFileInputLoader;

import com.google.inject.Inject;

/**
 * Its same file loader, just that it does not use default source files
 * directory for searching files. It searches for files in validated invoice
 * directory - they are processed a bit different.
 * 
 * @author Ignas Lelys
 * @created Jan 20, 2011
 * 
 */
public class ValidatedInvoiceInputLoader extends SimpleFileInputLoader {

    @Inject
    private GriegConfig config;

    /**
     * @see org.meveo.core.inputloader.AbstractFileInputLoader#getFileForProcessing()
     */
    @Override
    public File getFileForProcessing() {
        return FileUtils.getFileForParsing(config.getValidatedFilesDirectory(), config.getFileExtensions());
    }

}
