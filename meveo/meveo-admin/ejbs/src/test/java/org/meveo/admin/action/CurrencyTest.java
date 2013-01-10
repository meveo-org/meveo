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

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.security.Identity;
import org.manaty.BaseIntegrationTest;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.admin.Currency;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Currency} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.23
 * 
 */
public class CurrencyTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayCurrencies() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object currencies = getValue("#{currencies}");
                Assert.assertTrue(currencies instanceof PaginationDataModel<?>);
                PaginationDataModel<Currency> currenciesDataModel = (PaginationDataModel<Currency>) currencies;

                // Check for the correct number of results
                Assert.assertEquals(currenciesDataModel.getRowCount(), 3);

                // Load data
                currenciesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertCurrency(currenciesDataModel, 2, "EUR");
                assertCurrency(currenciesDataModel, 3, "USD");
                assertCurrency(currenciesDataModel, 4, "GBP");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterCurrencies() throws Exception {
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object currencies = getValue("#{currencies}");
                Assert.assertTrue(currencies instanceof PaginationDataModel<?>);
                PaginationDataModel<Currency> currenciesDataModel = (PaginationDataModel<Currency>) currencies;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("isoCode", "0001");

                // Load data
                currenciesDataModel.addFilters(filters);
                currenciesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(currenciesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object currencies = getValue("#{currencies}");
                Assert.assertTrue(currencies instanceof PaginationDataModel<?>);
                PaginationDataModel<Currency> currenciesDataModel = (PaginationDataModel<Currency>) currencies;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("isoCode", "0*");

                // Load data
                currenciesDataModel.addFilters(filters);
                currenciesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(currenciesDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditCurrency() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{currencyBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{currency.code}", "EURO");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{currencyBean.saveOrUpdate}"), "currencies");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{currencyBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{currencyBean.instance.isoCode}"), null);
                Assert.assertEquals(getValue("#{currencyBean.instance.name}"), null);
                Assert.assertEquals(getValue("#{currencyBean.instance.systemCurrency}"), null);

                Object currencies = getValue("#{currencies}");
                Assert.assertTrue(currencies instanceof PaginationDataModel<?>);
                PaginationDataModel<Currency> currenciesDataModel = (PaginationDataModel<Currency>) currencies;

                // Check for the correct number of results
                Assert.assertEquals(currenciesDataModel.getRowCount(), 3);

                // Load data
                currenciesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertCurrency(currenciesDataModel, 2, "EURO");
                assertCurrency(currenciesDataModel, 3, "USD");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddCurrency() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{currencyBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{currency.code}", "AUD");
                setValue("#{currency.isoCode}", "4217");
                setValue("#{currency.name}", "Australian dollar");
                setValue("#{currency.systemCurrency}", false);
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{currencyBean.saveOrUpdate}"), "currencies");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{currencyBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{currencyBean.instance.isoCode}"), null);
                Assert.assertEquals(getValue("#{currencyBean.instance.name}"), null);
                Assert.assertEquals(getValue("#{currencyBean.instance.systemCurrency}"), null);

                Object currencies = getValue("#{currencies}");
                Assert.assertTrue(currencies instanceof PaginationDataModel<?>);
                PaginationDataModel<Currency> currenciesDataModel = (PaginationDataModel<Currency>) currencies;

                // Check for the correct number of results'
                currenciesDataModel.forceRefresh();
                Assert.assertEquals(currenciesDataModel.getRowCount(), 4);
            }

        }.run();
    }

    /**
     * Check correct entity values from dataModel
     * 
     * @param dataModel
     *            filtered data model
     * @param row
     *            Entities row (id)
     * @param code
     *            Entities code to compare with existing
     */
    private void assertCurrency(PaginationDataModel<Currency> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof Currency);
        Currency currency = (Currency) rowData;
        Assert.assertEquals(currency.getCode(), code);
        Assert.assertTrue(currency.getId() == row);
    }
}
