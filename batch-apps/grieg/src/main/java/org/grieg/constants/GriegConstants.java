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
package org.grieg.constants;

import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.StepExecution;

/**
 * String constants that is used as keys in context parameters in
 * {@link StepExecution} and {@link TaskExecution} objects. Using this constants
 * user can get value from context parameter Map. For example ACCESS_KEY would
 * return {@link Access}. Of course putting value to context should be of same
 * type as expected when getting it using same key.
 * 
 * @author Ignas Lelys
 * @created Dec 21, 2010
 * 
 */
public class GriegConstants {

    public static final String PDF_PARAMETERS = "PDF_PARAMETERS";
    public static final String DUNNING_PARAMETERS = "DUNNING_PARAMETERS";
    public static final String CUSTOMER_ACCOUNT = "CUSTOMER_ACCOUNT";
    public static final String BILLING_TEMPLATE = "BILLING_TEMPLATE";
    public static final String PAYMENT_METHOD = "PAYMENT_METHOD";
    public static final String BILLING_ACCOUNT = "BILLING_ACCOUNT";
    public static final String INVOICE = "INVOICE";
    public static final String PROVIDER = "PROVIDER";
    public static final String SUBREPORT_DIR = "SUBREPORT_DIR";

    public static final String VALIDATED_INVOICE_INPUT_PROPERTY = "VALIDATED_INVOICE_INPUT_PROPERTY";

    public static final String LOGO_PATH_KEY = "logoPath";
    public static final String MESSAGE_PATH_KEY = "messagePath";
    public static final String NET_TO_PAY_KEY = "netToPay";
    public static final String BALANCE_KEY = "balance";
    public static final String ACCOUNT_TERMINATED_KEY = "accountTerminated";
    public static final String CUSTOMER_ADDRESS_KEY = "customerAddress";
    public static final String INVOICE_NUMBER_KEY = "invoiceNumber";
    public static final String HIGH_OPTICAL_LINE_KEY = "TIPligneHaute";
    public static final String LOW_OPTICAL_LINE_KEY = "TIPligneBasse";

    public static final String MEDIA_TEMPLATES = "mediaTemplates";
    public static final String MEDIA_CONFIG = "mediaConfig";

}
