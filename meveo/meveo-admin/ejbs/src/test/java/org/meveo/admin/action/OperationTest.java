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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.security.Identity;
import org.manaty.BaseIntegrationTest;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.billing.Operation;
import org.meveo.model.billing.OperationTypeEnum;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Operation} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.12
 * 
 */
public class OperationTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Wallet.dbunit.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Operation.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayOperations() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object operations = getValue("#{operations}");
                Assert.assertTrue(operations instanceof PaginationDataModel<?>);
                PaginationDataModel<Operation> operationsDataModel = (PaginationDataModel<Operation>) operations;

                // Check for the correct number of results
                Assert.assertEquals(operationsDataModel.getRowCount(), 3);

                // Load data
                operationsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertOperation(operationsDataModel, 2, "code_2");
                assertOperation(operationsDataModel, 3, "code_3");
                assertOperation(operationsDataModel, 4, "code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterOperations() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object operations = getValue("#{operations}");
                Assert.assertTrue(operations instanceof PaginationDataModel<?>);
                PaginationDataModel<Operation> operationsDataModel = (PaginationDataModel<Operation>) operations;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("accountingCode", "accountingCode_2");

                // Load data
                operationsDataModel.addFilters(filters);
                operationsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(operationsDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object operations = getValue("#{operations}");
                Assert.assertTrue(operations instanceof PaginationDataModel<?>);
                PaginationDataModel<Operation> operationsDataModel = (PaginationDataModel<Operation>) operations;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("type", OperationTypeEnum.DEBIT);

                // Load data
                operationsDataModel.addFilters(filters);
                operationsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(operationsDataModel.getRowCount(), 1);

            }
        }.run();

        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object operations = getValue("#{operations}");
                Assert.assertTrue(operations instanceof PaginationDataModel<?>);
                PaginationDataModel<Operation> operationsDataModel = (PaginationDataModel<Operation>) operations;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("type", OperationTypeEnum.CREDIT);

                // Load data
                operationsDataModel.addFilters(filters);
                operationsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(operationsDataModel.getRowCount(), 2);

            }
        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditOperation() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{operationBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{operation.code}", "new_code_2");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{operationBean.saveOrUpdate}"), "operations");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{operationBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.accountingCode}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.type}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.amount}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.previousBalance}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.resultingBalance}"), null);

                Object operations = getValue("#{operations}");
                Assert.assertTrue(operations instanceof PaginationDataModel<?>);
                PaginationDataModel<Operation> operationsDataModel = (PaginationDataModel<Operation>) operations;

                // Check for the correct number of results
                Assert.assertEquals(operationsDataModel.getRowCount(), 3);

                // Load data
                operationsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertOperation(operationsDataModel, 2, "new_code_2");
                assertOperation(operationsDataModel, 3, "code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddOperation() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{operationBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{operation.code}", "code_1");
                setValue("#{operation.accountingCode}", "accountingCode_1");
                setValue("#{operation.type}", OperationTypeEnum.CREDIT);
                setValue("#{operation.amount}", new BigDecimal(23));
                setValue("#{operation.previousBalance}", getRandomBigDecimal());
                setValue("#{operation.resultingBalance}", getRandomBigDecimal());
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{operationBean.saveOrUpdate}"), "operations");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{operationBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.accountingCode}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.type}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.amount}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.previousBalance}"), null);
                Assert.assertEquals(getValue("#{operationBean.instance.resultingBalance}"), null);

                Object operations = getValue("#{operations}");

                Assert.assertTrue(operations instanceof PaginationDataModel<?>);
                PaginationDataModel<Operation> operationsDataModel = (PaginationDataModel<Operation>) operations;

                // Check for the correct number of results'
                operationsDataModel.forceRefresh();
                Assert.assertEquals(operationsDataModel.getRowCount(), 4);
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
    private void assertOperation(PaginationDataModel<Operation> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof Operation);
        Operation operation = (Operation) rowData;
        Assert.assertEquals(operation.getCode(), code);
        Assert.assertTrue(operation.getId() == row);
    }
}
