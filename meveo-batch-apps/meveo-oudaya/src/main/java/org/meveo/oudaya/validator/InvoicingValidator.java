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
package org.meveo.oudaya.validator;

import org.meveo.core.validator.Validator;
import org.meveo.oudaya.InvoicingTicket;

/**
 * @author R.AITYAAZZA
 * @created 11 janv. 11
 */
public class InvoicingValidator implements Validator<InvoicingTicket> {

    /**
     * No Validation yet. All loaded tickets are valid.
     * 
     * @see org.meveo.core.validator.Validator#validate(org.meveo.core.ticket.Ticket)
     */
    public boolean validate(InvoicingTicket ticket) {
        return true;
    }

}
