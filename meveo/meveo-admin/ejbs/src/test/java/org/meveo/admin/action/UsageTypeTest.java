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
import org.meveo.model.rating.UsageType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link UsageType} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.27
 * 
 */
public class UsageTypeTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/UsageType.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayUsageTypes() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object usageTypes = getValue("#{usageTypes}");
                Assert.assertTrue(usageTypes instanceof PaginationDataModel<?>);
                PaginationDataModel<UsageType> usageTypesDataModel = (PaginationDataModel<UsageType>) usageTypes;

                // Check for the correct number of results
                Assert.assertEquals(usageTypesDataModel.getRowCount(), 3);

                // Load data
                usageTypesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertUsageTypes(usageTypesDataModel, 2, "Code_2");
                assertUsageTypes(usageTypesDataModel, 3, "Code_3");
                assertUsageTypes(usageTypesDataModel, 4, "Code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterUsageTypes() throws Exception {
        Identity.setSecurityEnabled(false);
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object usageTypes = getValue("#{usageTypes}");
                Assert.assertTrue(usageTypes instanceof PaginationDataModel<?>);
                PaginationDataModel<UsageType> usageTypesDataModel = (PaginationDataModel<UsageType>) usageTypes;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_2");

                // Load data
                usageTypesDataModel.addFilters(filters);
                usageTypesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(usageTypesDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object usageTypes = getValue("#{usageTypes}");
                Assert.assertTrue(usageTypes instanceof PaginationDataModel<?>);
                PaginationDataModel<UsageType> usageTypesDataModel = (PaginationDataModel<UsageType>) usageTypes;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_*");

                // Load data
                usageTypesDataModel.addFilters(filters);
                usageTypesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(usageTypesDataModel.getRowCount(), 3);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditUsageType() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{usageTypeBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{usageType.code}", "Code_New");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{usageTypeBean.saveOrUpdate}"), "usageTypes");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{usageTypeBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{usageTypeBean.instance.name}"), null);
                Assert.assertEquals(getValue("#{usageTypeBean.instance.metricName}"), null);
                Assert.assertEquals(getValue("#{usageTypeBean.instance.metricCode}"), null);

                Object usageTypes = getValue("#{usageTypes}");
                Assert.assertTrue(usageTypes instanceof PaginationDataModel<?>);
                PaginationDataModel<UsageType> usageTypesDataModel = (PaginationDataModel<UsageType>) usageTypes;

                // Check for the correct number of results
                Assert.assertEquals(usageTypesDataModel.getRowCount(), 3);

                // Load data
                usageTypesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertUsageTypes(usageTypesDataModel, 2, "Code_New");
                assertUsageTypes(usageTypesDataModel, 3, "Code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddUsageType() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{usageTypeBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{usageType.code}", "Code_1");
                setValue("#{usageType.name}", "Name_1");
                setValue("#{usageType.metricName}", "Metric_Name_1");
                setValue("#{usageType.metricCode}", "Metric_Code_1");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{usageTypeBean.saveOrUpdate}"), "usageTypes");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{usageTypeBean.instance.code}"), null);
                Assert.assertEquals(getValue("#{usageTypeBean.instance.name}"), null);
                Assert.assertEquals(getValue("#{usageTypeBean.instance.metricName}"), null);
                Assert.assertEquals(getValue("#{usageTypeBean.instance.metricCode}"), null);

                Object usageTypes = getValue("#{usageTypes}");
                Assert.assertTrue(usageTypes instanceof PaginationDataModel<?>);
                PaginationDataModel<UsageType> usageTypesDataModel = (PaginationDataModel<UsageType>) usageTypes;

                // Check for the correct number of results
                Assert.assertEquals(usageTypesDataModel.getRowCount(), 4);

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
    private void assertUsageTypes(PaginationDataModel<UsageType> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof UsageType);
        UsageType usageType = (UsageType) rowData;
        Assert.assertEquals(usageType.getCode(), code);
        Assert.assertTrue(usageType.getId() == row);
    }
}
