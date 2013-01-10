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
import org.meveo.model.billing.InvoiceCategory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Currency} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.23
 * 
 */
public class InvoiceCategoryTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/InvoiceCategory.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayInvoiceCategories() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object invoiceCategories = getValue("#{invoiceCategories}");
                Assert.assertTrue(invoiceCategories instanceof PaginationDataModel<?>);
                PaginationDataModel<InvoiceCategory> invoiceCategoriesDataModel = (PaginationDataModel<InvoiceCategory>) invoiceCategories;

                // Check for the correct number of results
                Assert.assertEquals(invoiceCategoriesDataModel.getRowCount(), 3);

                // Load data
                invoiceCategoriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertinvoiceCategory(invoiceCategoriesDataModel, 2, "IC_code_2");
                assertinvoiceCategory(invoiceCategoriesDataModel, 3, "IC_code_3");
                assertinvoiceCategory(invoiceCategoriesDataModel, 4, "IC_code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterInvoiceCategories() throws Exception {
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                login();
                Object invoiceCategories = getValue("#{invoiceCategories}");
                Assert.assertTrue(invoiceCategories instanceof PaginationDataModel<?>);
                PaginationDataModel<InvoiceCategory> invoiceCategoriesDataModel = (PaginationDataModel<InvoiceCategory>) invoiceCategories;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "IC_code_2");

                // Load data
                invoiceCategoriesDataModel.addFilters(filters);
                invoiceCategoriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(invoiceCategoriesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                login();
                Object invoiceCategories = getValue("#{invoiceCategories}");
                Assert.assertTrue(invoiceCategories instanceof PaginationDataModel<?>);
                PaginationDataModel<InvoiceCategory> invoiceCategoriesDataModel = (PaginationDataModel<InvoiceCategory>) invoiceCategories;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "IC_code_*");

                // Load data
                invoiceCategoriesDataModel.addFilters(filters);
                invoiceCategoriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(invoiceCategoriesDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditInvoiceCategory() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{invoiceCategoryBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{invoiceCategory.code}", "IC_code_mod");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{invoiceCategoryBean.saveOrUpdate}"), "invoiceCategories");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{invoiceCategoryBean.instance.code}"), null);

                Object invoiceCategories = getValue("#{invoiceCategories}");
                Assert.assertTrue(invoiceCategories instanceof PaginationDataModel<?>);
                PaginationDataModel<InvoiceCategory> invoiceCategoriesDataModel = (PaginationDataModel<InvoiceCategory>) invoiceCategories;

                // Check for the correct number of results
                Assert.assertEquals(invoiceCategoriesDataModel.getRowCount(), 3);

                // Load data
                invoiceCategoriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertinvoiceCategory(invoiceCategoriesDataModel, 2, "IC_code_mod");
                assertinvoiceCategory(invoiceCategoriesDataModel, 3, "IC_code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddInvoiceCategory() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{invoiceCategoryBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{invoiceCategory.code}", "IC_code_1");
                setValue("#{invoiceCategory.description}", "IC_description_1");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{invoiceCategoryBean.saveOrUpdate}"), "invoiceCategories");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{invoiceCategoryBean.instance.code}"), null);

                Object invoiceCategories = getValue("#{invoiceCategories}");
                Assert.assertTrue(invoiceCategories instanceof PaginationDataModel<?>);
                PaginationDataModel<InvoiceCategory> invoiceCategoriesDataModel = (PaginationDataModel<InvoiceCategory>) invoiceCategories;

                // Check for the correct number of results'
                invoiceCategoriesDataModel.forceRefresh();
                Assert.assertEquals(invoiceCategoriesDataModel.getRowCount(), 4);

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
    private void assertinvoiceCategory(PaginationDataModel<InvoiceCategory> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof InvoiceCategory);
        InvoiceCategory object = (InvoiceCategory) rowData;
        Assert.assertEquals(object.getCode(), code);
        Assert.assertTrue(object.getId() == row);
    }
}
