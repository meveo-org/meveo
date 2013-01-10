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
import org.meveo.model.catalog.RecurringChargeTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link RecurringChargeTemplate} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20011.05.20
 * 
 */

public class RecurringChargeTemplateTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/RecurringChargeTemplate.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayRecurringChargeTemplate() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object recurringChargeTemplates = getValue("#{recurringChargeTemplates}");
                Assert.assertTrue(recurringChargeTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<RecurringChargeTemplate> recurringChargeTemplatesDataModel = (PaginationDataModel<RecurringChargeTemplate>) recurringChargeTemplates;

                // Check for the correct number of results
                Assert.assertEquals(recurringChargeTemplatesDataModel.getRowCount(), 3);

                // Load data
                recurringChargeTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertRecurringChargeTemplate(recurringChargeTemplatesDataModel, 2, "Code_2");
                assertRecurringChargeTemplate(recurringChargeTemplatesDataModel, 3, "Code_3");
                assertRecurringChargeTemplate(recurringChargeTemplatesDataModel, 4, "Code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterRecurringChargeTemplates() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object recurringChargeTemplates = getValue("#{recurringChargeTemplates}");
                Assert.assertTrue(recurringChargeTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<RecurringChargeTemplate> recurringChargeTemplatesDataModel = (PaginationDataModel<RecurringChargeTemplate>) recurringChargeTemplates;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_4");

                // Load data
                recurringChargeTemplatesDataModel.addFilters(filters);
                recurringChargeTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(recurringChargeTemplatesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object recurringChargeTemplates = getValue("#{recurringChargeTemplates}");
                Assert.assertTrue(recurringChargeTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<RecurringChargeTemplate> recurringChargeTemplatesDataModel = (PaginationDataModel<RecurringChargeTemplate>) recurringChargeTemplates;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_*");

                // Load data
                recurringChargeTemplatesDataModel.addFilters(filters);
                recurringChargeTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(recurringChargeTemplatesDataModel.getRowCount(), 3);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditRecurringChargeTemplate() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{recurringChargeTemplateBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{recurringChargeTemplate.code}", "Code_New");

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{recurringChargeTemplateBean.saveOrUpdate}"),
                        "recurringChargeTemplates");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{recurringChargeTemplateBean.instance.code}"), null);

                Object recurringChargeTemplates = getValue("#{recurringChargeTemplates}");
                Assert.assertTrue(recurringChargeTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<RecurringChargeTemplate> recurringChargeTemplatesDataModel = (PaginationDataModel<RecurringChargeTemplate>) recurringChargeTemplates;

                // Check for the correct number of results
                // TODO change row count to 4, because there s a bug
                Assert.assertEquals(recurringChargeTemplatesDataModel.getRowCount(), 3);

                // Load data
                recurringChargeTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                // TODO change YorkCommunity_2 to YorkCommunity_New
                assertRecurringChargeTemplate(recurringChargeTemplatesDataModel, 2, "Code_New");
                assertRecurringChargeTemplate(recurringChargeTemplatesDataModel, 3, "Code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddRecurringChargeTemplate() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{recurringChargeTemplateBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{recurringChargeTemplate.code}", "CODE_1");
                setValue("#{recurringChargeTemplate.invoiceSubCategory}", loadInvoiceSubCategory((long) 2));

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{recurringChargeTemplateBean.saveOrUpdate}"),
                        "recurringChargeTemplates");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Assert.assertEquals(getValue("#{recurringChargeTemplateBean.instance.code}"), null);

                Object recurringChargeTemplates = getValue("#{recurringChargeTemplates}");
                Assert.assertTrue(recurringChargeTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<RecurringChargeTemplate> recurringChargeTemplatesDataModel = (PaginationDataModel<RecurringChargeTemplate>) recurringChargeTemplates;

                // Check for the correct number of results
                // TODO change row count to 4, because there s a bug
                Assert.assertEquals(recurringChargeTemplatesDataModel.getRowCount(), 4);
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
    private void assertRecurringChargeTemplate(PaginationDataModel<RecurringChargeTemplate> dataModel, long row,
            String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof RecurringChargeTemplate);
        RecurringChargeTemplate recurringChargeTemplate = (RecurringChargeTemplate) rowData;
        Assert.assertEquals(recurringChargeTemplate.getCode(), code);
        Assert.assertTrue(recurringChargeTemplate.getId() == row);
    }

}
