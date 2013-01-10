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
import org.meveo.model.payments.CustomerAccount;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link CustomerAccount} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.23
 * 
 */
public class CustomerAccountTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/AllAccountHierarchy.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayCustomerAccounts() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object customerAccounts = getValue("#{customerAccounts}");
                Assert.assertTrue(customerAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<CustomerAccount> customerAccountDataModel = (PaginationDataModel<CustomerAccount>) customerAccounts;

                // Check for the correct number of results
                Assert.assertEquals(customerAccountDataModel.getRowCount(), 3);

                // Load data
                customerAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertCustommerAccount(customerAccountDataModel, 22, "name_2");
                assertCustommerAccount(customerAccountDataModel, 23, "name_3");
                assertCustommerAccount(customerAccountDataModel, 24, "name_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterCustomerAccounts() throws Exception {
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                login();
                Object customerAccounts = getValue("#{customerAccounts}");
                Assert.assertTrue(customerAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<CustomerAccount> customerAccountDataModel = (PaginationDataModel<CustomerAccount>) customerAccounts;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "code_2");

                // Load data
                customerAccountDataModel.addFilters(filters);
                customerAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(customerAccountDataModel.getRowCount(), 1);
            }

        }.run();
    }
    @Test(groups = { "integration", "filtering" })
    public void testFilterCustomerAccounts2() throws Exception {        
        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object customerAccounts = getValue("#{customerAccounts}");
                Assert.assertTrue(customerAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<CustomerAccount> customerAccountDataModel = (PaginationDataModel<CustomerAccount>) customerAccounts;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "code*");

                // Load data
                customerAccountDataModel.addFilters(filters);
                customerAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(customerAccountDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditCustomerAccount() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "22");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{customerAccountBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{customerAccount.name.firstName}", "name_modified");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{customerAccountBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{customerAccountBean.instance.name.title}"), null);
                Assert.assertEquals(getValue("#{customerAccountBean.instance.name.firstName}"), null);

                Object customerAccounts = getValue("#{customerAccounts}");
                Assert.assertTrue(customerAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<CustomerAccount> customerAccountDataModel = (PaginationDataModel<CustomerAccount>) customerAccounts;

                // Check for the correct number of results
                Assert.assertEquals(customerAccountDataModel.getRowCount(), 3);

                // Load data
                customerAccountDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertCustommerAccount(customerAccountDataModel, 22, "name_modified");
                assertCustommerAccount(customerAccountDataModel, 23, "name_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddCustomerAccount() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{customerAccountBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{customerAccount.name.title}", loadTitle((long) 1));
                setValue("#{customerAccount.name.firstName}", "name_1");
                setValue("#{customerAccount.code}", "code_1");
                setValue("#{customerAccount.customer}", loadCustomer((long) 2));

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{customerAccountBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object customerAccounts = getValue("#{customerAccounts}");
                Assert.assertTrue(customerAccounts instanceof PaginationDataModel<?>);
                PaginationDataModel<CustomerAccount> customerAccountDataModel = (PaginationDataModel<CustomerAccount>) customerAccounts;

                // Check for the correct number of results
                Assert.assertEquals(customerAccountDataModel.getRowCount(), 4);

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
    private void assertCustommerAccount(PaginationDataModel<CustomerAccount> dataModel, long row, String name) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof CustomerAccount);
        CustomerAccount customerAccount = (CustomerAccount) rowData;
        Assert.assertTrue(customerAccount.getId() == row);
        Assert.assertEquals(customerAccount.getName().getFirstName(), name);
        
    }
}
