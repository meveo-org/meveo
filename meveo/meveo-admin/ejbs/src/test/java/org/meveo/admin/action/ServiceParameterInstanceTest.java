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
import org.meveo.model.billing.ServiceParameterInstance;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link ServiceParameter} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.26
 * 
 */
public class ServiceParameterInstanceTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/ServiceParameter.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayServiceParametes() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object serviceParameters = getValue("#{serviceParameterInstances}");
                Assert.assertTrue(serviceParameters instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceParameterInstance> serviceParameterDataModel = (PaginationDataModel<ServiceParameterInstance>) serviceParameters;

                // Check for the correct number of results
                Assert.assertEquals(serviceParameterDataModel.getRowCount(), 3);

                // Load data
                serviceParameterDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertServiceParameter(serviceParameterDataModel, 2, "Name_2");
                assertServiceParameter(serviceParameterDataModel, 3, "Name_3");
                assertServiceParameter(serviceParameterDataModel, 4, "Name_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterServiceParameters() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object serviceParameters = getValue("#{serviceParameterInstances}");
                Assert.assertTrue(serviceParameters instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceParameterInstance> serviceParameterDataModel = (PaginationDataModel<ServiceParameterInstance>) serviceParameters;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "Name_2");

                // Load data
                serviceParameterDataModel.addFilters(filters);
                serviceParameterDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(serviceParameterDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object serviceParameters = getValue("#{serviceParameterInstances}");
                Assert.assertTrue(serviceParameters instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceParameterInstance> serviceParameterDataModel = (PaginationDataModel<ServiceParameterInstance>) serviceParameters;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "Name_*");

                // Load data
                serviceParameterDataModel.addFilters(filters);
                serviceParameterDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(serviceParameterDataModel.getRowCount(), 3);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditSrviceParameter() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{serviceParameterInstanceBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{serviceParameterInstance.name}", "Name_New");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{serviceParameterInstanceBean.saveOrUpdate}"),
                        "serviceParameterInstances");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{serviceParameterInstanceBean.instance.value}"), null);
                Assert.assertEquals(getValue("#{serviceParameterInstanceBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{serviceParameterInstanceBean.instance.name}"), null);

                Object serviceParameters = getValue("#{serviceParameterInstances}");
                Assert.assertTrue(serviceParameters instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceParameterInstance> serviceParameterDataModel = (PaginationDataModel<ServiceParameterInstance>) serviceParameters;

                // Check for the correct number of results
                Assert.assertEquals(serviceParameterDataModel.getRowCount(), 3);

                // Load data
                serviceParameterDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertServiceParameter(serviceParameterDataModel, 2, "Name_New");
                assertServiceParameter(serviceParameterDataModel, 3, "Name_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddServiceParameter() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{serviceParameterInstanceBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{serviceParameterInstance.code}", "Code_1");
                setValue("#{serviceParameterInstance.value}", "Value_1");
                setValue("#{serviceParameterInstance.name}", "Name_1");

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{serviceParameterInstanceBean.saveOrUpdate}"),
                        "serviceParameterInstances");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{serviceParameterInstanceBean.instance.value}"), null);
                Assert.assertEquals(getValue("#{serviceParameterInstanceBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{serviceParameterInstanceBean.instance.name}"), null);

                Object serviceParameters = getValue("#{serviceParameterInstances}");
                Assert.assertTrue(serviceParameters instanceof PaginationDataModel<?>);
                PaginationDataModel<ServiceParameterInstance> serviceParameterDataModel = (PaginationDataModel<ServiceParameterInstance>) serviceParameters;

                // Check for the correct number of results
                Assert.assertEquals(serviceParameterDataModel.getRowCount(), 4);

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
     * @param neme
     *            Entities name to compare with existing
     */
    private void assertServiceParameter(PaginationDataModel<ServiceParameterInstance> dataModel, long row, String name) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof ServiceParameterInstance);
        ServiceParameterInstance serviceParameter = (ServiceParameterInstance) rowData;
        Assert.assertEquals(serviceParameter.getName(), name);
        Assert.assertTrue(serviceParameter.getId() == row);
    }
}
