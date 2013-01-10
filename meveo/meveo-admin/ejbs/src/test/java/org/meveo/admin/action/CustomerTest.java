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
import org.meveo.model.crm.Customer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Customer} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.11.23
 * 
 */
public class CustomerTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Customer.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayCustomers() throws Exception {

        new FacesRequest() {

            @Override
            protected void beforeRequest() {
                setParameter("results_form", "kuku");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                PaginationDataModel<Customer> customerDataModel = (PaginationDataModel<Customer>) getValue("#{customers}");

                // Check for the correct number of results
                Assert.assertEquals(customerDataModel.getRowCount(), 3);

                // Load data
                customerDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertCustommer(customerDataModel, 2, "code_2");
                assertCustommer(customerDataModel, 3, "code_3");
                assertCustommer(customerDataModel, 4, "code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterCustomerAccounts() throws Exception {
        Identity.setSecurityEnabled(false);
        new FacesRequest() {

            @Override
            protected void beforeRequest() {
                setParameter("results_form", "kuku");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                PaginationDataModel<Customer> customerDataModel = (PaginationDataModel<Customer>) getValue("#{customers}");

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "code_2");

                // Load data
                customerDataModel.addFilters(filters);
                customerDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(customerDataModel.getRowCount(), 1);
            }

        }.run();
    }

    
    @Test(groups = { "integration", "filtering" })
    public void testFilterCustomerAccounts2() throws Exception {
        new FacesRequest() {

            @Override
            protected void beforeRequest() {
                setParameter("results_form", "kuku");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                PaginationDataModel<Customer> customerDataModel = (PaginationDataModel<Customer>) getValue("#{customers}");

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "code*");

                // Load data
                customerDataModel.addFilters(filters);
                customerDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(customerDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditCustomerAccount() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{customerBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{customer.code}", "code_modified");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{customerBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{customerBean.instance.code}"), null);

                Object customers = getValue("#{customers}");
                Assert.assertTrue(customers instanceof PaginationDataModel<?>);
                PaginationDataModel<Customer> customerDataModel = (PaginationDataModel<Customer>) customers;

                // Check for the correct number of results
                Assert.assertEquals(customerDataModel.getRowCount(), 3);

                // Load data
                customerDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertCustommer(customerDataModel, 2, "code_modified");
                assertCustommer(customerDataModel, 3, "code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddCustomerAccount() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{customerBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{customer.description}", "description_1");
                setValue("#{customer.code}", "code_1");

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{customerBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object customers = getValue("#{customers}");
                Assert.assertTrue(customers instanceof PaginationDataModel<?>);
                PaginationDataModel<Customer> customerDataModel = (PaginationDataModel<Customer>) customers;

                // Check for the correct number of results
                Assert.assertEquals(customerDataModel.getRowCount(), 4);

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
    private void assertCustommer(PaginationDataModel<Customer> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof Customer);
        Customer customer = (Customer) rowData;
        Assert.assertEquals(customer.getCode(), code);
        Assert.assertTrue(customer.getId() == row);
    }
}
