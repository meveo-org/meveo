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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.security.Identity;
import org.manaty.BaseIntegrationTest;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.catalog.RecurringChargeTemplate;
import org.meveo.model.catalog.ServiceTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link ServiceTemplate} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20011.05.20
 * 
 */

public class ServiceTemplateTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/ServiceTemplate.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayServiceTemplate() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object serviceTemplates = getValue("#{serviceTemplates}");
                Assert.assertTrue(serviceTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceTemplate> serviceTemplatesDataModel = (PaginationDataModel<ServiceTemplate>) serviceTemplates;

                // Check for the correct number of results
                Assert.assertEquals(serviceTemplatesDataModel.getRowCount(), 3);

                // Load data
                serviceTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertServiceTemplate(serviceTemplatesDataModel, 2, "Code_2");
                assertServiceTemplate(serviceTemplatesDataModel, 3, "Code_3");
                assertServiceTemplate(serviceTemplatesDataModel, 4, "Code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterServiceTemplates() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object serviceTemplates = getValue("#{serviceTemplates}");
                Assert.assertTrue(serviceTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceTemplate> serviceTemplatesDataModel = (PaginationDataModel<ServiceTemplate>) serviceTemplates;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_4");

                // Load data
                serviceTemplatesDataModel.addFilters(filters);
                serviceTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(serviceTemplatesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object serviceTemplates = getValue("#{serviceTemplates}");
                Assert.assertTrue(serviceTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceTemplate> serviceTemplatesDataModel = (PaginationDataModel<ServiceTemplate>) serviceTemplates;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_*");

                // Load data
                serviceTemplatesDataModel.addFilters(filters);
                serviceTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(serviceTemplatesDataModel.getRowCount(), 3);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditServiceTemplate() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{serviceTemplateBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{serviceTemplate.code}", "Code_New");

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{serviceTemplateBean.saveOrUpdate}"), "serviceTemplates");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{serviceTemplateBean.instance.code}"), null);

                Object serviceTemplates = getValue("#{serviceTemplates}");
                Assert.assertTrue(serviceTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceTemplate> serviceTemplatesDataModel = (PaginationDataModel<ServiceTemplate>) serviceTemplates;

                // Check for the correct number of results
                // TODO change row count to 4, because there s a bug
                Assert.assertEquals(serviceTemplatesDataModel.getRowCount(), 3);

                // Load data
                serviceTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                // TODO change YorkCommunity_2 to YorkCommunity_New
                assertServiceTemplate(serviceTemplatesDataModel, 2, "Code_New");
                assertServiceTemplate(serviceTemplatesDataModel, 3, "Code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddServiceTemplate() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{serviceTemplateBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{serviceTemplate.code}", "CODE_1");
                List<RecurringChargeTemplate> recurringCharges = new ArrayList<RecurringChargeTemplate>();
                recurringCharges.add(loadRecurringChargeTemplate((long) 2));
                setValue("#{serviceTemplate.recurringCharges}", recurringCharges);
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{serviceTemplateBean.saveOrUpdate}"), "serviceTemplates");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Assert.assertEquals(getValue("#{serviceTemplateBean.instance.code}"), null);

                Object serviceTemplates = getValue("#{serviceTemplates}");
                Assert.assertTrue(serviceTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceTemplate> serviceTemplatesDataModel = (PaginationDataModel<ServiceTemplate>) serviceTemplates;

                // Check for the correct number of results
                // TODO change row count to 4, because there s a bug
                Assert.assertEquals(serviceTemplatesDataModel.getRowCount(), 4);
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
    private void assertServiceTemplate(PaginationDataModel<ServiceTemplate> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof ServiceTemplate);
        ServiceTemplate serviceTemplate = (ServiceTemplate) rowData;
        Assert.assertEquals(serviceTemplate.getCode(), code);
        Assert.assertTrue(serviceTemplate.getId() == row);
    }

}
