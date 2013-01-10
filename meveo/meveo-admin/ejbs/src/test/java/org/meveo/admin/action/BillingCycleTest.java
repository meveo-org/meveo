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

import org.manaty.BaseIntegrationTest;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.billing.BillingCycle;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link BillingCycle} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.23
 * 
 */
public class BillingCycleTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/BillingCycle.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayBillingCycles() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object billingCycles = getValue("#{billingCycles}");
                Assert.assertTrue(billingCycles instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingCycle> billingCyclesDataModel = (PaginationDataModel<BillingCycle>) billingCycles;

                // Check for the correct number of results
                Assert.assertEquals(billingCyclesDataModel.getRowCount(), 3);

                // Load data
                billingCyclesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertBillingCycle(billingCyclesDataModel, 2, "Billing_template_2");
                assertBillingCycle(billingCyclesDataModel, 3, "Billing_template_3");
                assertBillingCycle(billingCyclesDataModel, 4, "Billing_template_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterBillingCycles() throws Exception {
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object billingCycles = getValue("#{billingCycles}");
                Assert.assertTrue(billingCycles instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingCycle> billingCyclesDataModel = (PaginationDataModel<BillingCycle>) billingCycles;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("billingTemplateName", "Billing_template_2");

                // Load data
                billingCyclesDataModel.addFilters(filters);
                billingCyclesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(billingCyclesDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object billingCycles = getValue("#{billingCycles}");
                Assert.assertTrue(billingCycles instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingCycle> billingCyclesDataModel = (PaginationDataModel<BillingCycle>) billingCycles;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("billingTemplateName", "Billing_template_*");

                // Load data
                billingCyclesDataModel.addFilters(filters);
                billingCyclesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(billingCyclesDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditBillingCycle() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{billingCycleBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{billingCycle.billingTemplateName}", "Billing_template_mod");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{billingCycleBean.saveOrUpdate}"), "billingCycles");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{billingCycleBean.instance.billingTemplateName}"), null);

                Object billingCycles = getValue("#{billingCycles}");
                Assert.assertTrue(billingCycles instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingCycle> billingCyclesDataModel = (PaginationDataModel<BillingCycle>) billingCycles;

                // Check for the correct number of results
                Assert.assertEquals(billingCyclesDataModel.getRowCount(), 3);

                // Load data
                billingCyclesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertBillingCycle(billingCyclesDataModel, 2, "Billing_template_mod");
                assertBillingCycle(billingCyclesDataModel, 3, "Billing_template_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddBillingCycle() throws Exception {
        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{billingCycleBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{billingCycle.code}", "code_1");
                setValue("#{billingCycle.billingTemplateName}", "billingTempkateName_1");

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{billingCycleBean.saveOrUpdate}"), "billingCycles");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{billingCycleBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{billingCycleBean.instance.billingTemplateName}"), null);

                Object billingCycles = getValue("#{billingCycles}");
                Assert.assertTrue(billingCycles instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingCycle> billingCyclesDataModel = (PaginationDataModel<BillingCycle>) billingCycles;

                // Load data
                billingCyclesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                billingCyclesDataModel.forceRefresh();
                Assert.assertEquals(billingCyclesDataModel.getRowCount(), 4);
            }

        }.run();
    }

    /**
     * Check correct entity values from dataModel.
     * 
     * @param dataModel
     *            filtered data model
     * @param row
     *            Entities row (id)
     * @param invoiceTemplateName
     *            Entities invoiceTemplateName to compare with existing
     */
    private void assertBillingCycle(PaginationDataModel<BillingCycle> dataModel, long row, String billingTemplateName) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof BillingCycle);
        BillingCycle billingCycle = (BillingCycle) rowData;
        Assert.assertEquals(billingCycle.getBillingTemplateName(), billingTemplateName);
        Assert.assertTrue(billingCycle.getId() == row);
    }
}
