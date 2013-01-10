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
package org.meveo.admin.action;

import org.jboss.seam.security.Identity;
import org.manaty.BaseIntegrationTest;
import org.meveo.model.admin.Currency;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for SystemCurrency
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.23
 * 
 */
public class SystemCurrencyTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
    }

    @Test(groups = { "integration", "editing" })
    public void testEditSystemCurrency() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            Currency systemCurrency = null;

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{systemCurrencyBean.initSystemCurrency}");
                systemCurrency = getSystemCurrency();
            }

            @Override
            protected void updateModelValues() throws Exception {

                Currency currency = loadCurrency(new Long(3));
                setValue("#{systemCurrencyBean.selectedCurrency}", currency);

            }

            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{systemCurrencyBean.saveNewSystemCurrency}");
            }

            @Override
            protected void renderResponse() throws Exception {
                Currency newSystemCurrency = (Currency) getValue("#{systemCurrencyBean.systemCurrency}");
                Assert.assertTrue(newSystemCurrency.getId() == systemCurrency.getId() + 1);

            }

        }.run();
    }

}
