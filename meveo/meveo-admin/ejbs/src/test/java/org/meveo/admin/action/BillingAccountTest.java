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
import org.meveo.model.billing.BillingAccount;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link BillingAccount} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.23
 * 
 */
public class BillingAccountTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/AllAccountHierarchy.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayBillingAccounts() throws Exception {

        // Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object billingAccounts = getValue("#{billingAccounts}");
                Assert.assertTrue(billingAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingAccount> billingAccountDataModel = (PaginationDataModel<BillingAccount>) billingAccounts;

                // Check for the correct number of results
                Assert.assertEquals(billingAccountDataModel.getRowCount(), 3);

                // Load data
                billingAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertBillingAccount(billingAccountDataModel, 32, "code_2");
                assertBillingAccount(billingAccountDataModel, 33, "code_3");
                assertBillingAccount(billingAccountDataModel, 34, "code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterBillingAccounts() throws Exception {
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object billingAccounts = getValue("#{billingAccounts}");
                Assert.assertTrue(billingAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingAccount> billingAccountDataModel = (PaginationDataModel<BillingAccount>) billingAccounts;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "code_2");

                // Load data
                billingAccountDataModel.addFilters(filters);
                billingAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(billingAccountDataModel.getRowCount(), 1);
            }

        }.run();

    }
    @Test(groups = { "integration", "filtering" })
    public void testFilterBillingAccounts2() throws Exception {
        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object billingAccounts = getValue("#{billingAccounts}");
                Assert.assertTrue(billingAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingAccount> billingAccountDataModel = (PaginationDataModel<BillingAccount>) billingAccounts;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "code*");

                // Load data
                billingAccountDataModel.addFilters(filters);
                billingAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(billingAccountDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditBillingAccount() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "32");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{billingAccountBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{billingAccount.code}", "code_modified");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{billingAccountBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{billingAccountBean.instance.code}"), null);

                Object billingAccounts = getValue("#{billingAccounts}");
                Assert.assertTrue(billingAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingAccount> billingAccountDataModel = (PaginationDataModel<BillingAccount>) billingAccounts;

                // Check for the correct number of results
                Assert.assertEquals(billingAccountDataModel.getRowCount(), 3);

                // Load data
                billingAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertBillingAccount(billingAccountDataModel, 32, "code_modified");
                assertBillingAccount(billingAccountDataModel, 33, "code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddBillingAccount() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{billingAccountBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{billingAccount.billingCycle}", loadBillingCycle((long) 2));
                setValue("#{billingAccount.customerAccount}", loadCustomerAccount(22L));
                setValue("#{billingAccount.address}", getAddress());
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{billingAccountBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object billingAccounts = getValue("#{billingAccounts}");
                Assert.assertTrue(billingAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<BillingAccount> billingAccountDataModel = (PaginationDataModel<BillingAccount>) billingAccounts;

                // Check for the correct number of results
                Assert.assertEquals(billingAccountDataModel.getRowCount(), 4);

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
     * @param name
     *            Entities name to compare with existing
     */
    private void assertBillingAccount(PaginationDataModel<BillingAccount> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof BillingAccount);
        BillingAccount billingAccount = (BillingAccount) rowData;
        Assert.assertEquals(billingAccount.getCode(), code);
        Assert.assertTrue(billingAccount.getId() == row);
    }
}
