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
package org.grieg.output;

import java.util.Map;

import org.meveo.core.output.Output;

/**
 * @author Ignas Lelys
 * @created Dec 28, 2010
 *
 */
public class GriegOutput implements Output {
    
    /** Xml invoice (<invoice></invoice>>) with all data needed for pdf generator.  */
    private String xmlInvoice;
    
    /** Additional data for pdf generator. */
    private Map<String, Object> parameters;
    
    public GriegOutput(String xmlInvoice, Map<String, Object> parameters) {
        super();
        this.xmlInvoice = xmlInvoice;
        this.parameters = parameters;
    }

    @Override
    public String getTicketOutput() {
        return xmlInvoice;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
}
