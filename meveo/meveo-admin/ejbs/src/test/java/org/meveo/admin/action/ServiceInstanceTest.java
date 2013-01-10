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

import org.manaty.BaseIntegrationTest;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.billing.ServiceInstance;
import org.testng.Assert;

/**
 * Integration tests for {@link ServiceInstance} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.26
 * 
 */
public class ServiceInstanceTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/ServiceInstance.dbunit.xml"));
    }

    // @Test(groups = { "integration", "display" })
    // public void testDisplayServices() throws Exception {
    //
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Object services = getValue("#{serviceInstances}");
    // Assert.assertTrue(services instanceof PaginationDataModel<?>);
    // PaginationDataModel<ServiceInstance> serviceDataModel =
    // (PaginationDataModel<ServiceInstance>) services;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceDataModel.getRowCount(), 3);
    //
    // // Load data
    // serviceDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // assertService(serviceDataModel, 2, "Code_2");
    // assertService(serviceDataModel, 3, "Code_3");
    // assertService(serviceDataModel, 4, "Code_4");
    //
    // }
    //
    // }.run();
    //
    // }
    //
    // @Test(groups = { "integration", "filtering" })
    // public void testFilterServices() throws Exception {
    // Identity.setSecurityEnabled(false);
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object services = getValue("#{serviceInstances}");
    // Assert.assertTrue(services instanceof PaginationDataModel<?>);
    // PaginationDataModel<ServiceInstance> serviceDataModel =
    // (PaginationDataModel<ServiceInstance>) services;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // filters.put("code", "Code_2");
    //
    // // Load data
    // serviceDataModel.addFilters(filters);
    // serviceDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceDataModel.getRowCount(), 1);
    // }
    //
    // }.run();
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object services = getValue("#{serviceInstances}");
    // Assert.assertTrue(services instanceof PaginationDataModel<?>);
    // PaginationDataModel<ServiceInstance> serviceDataModel =
    // (PaginationDataModel<ServiceInstance>) services;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // filters.put("code", "Code*");
    //
    // // Load data
    // serviceDataModel.addFilters(filters);
    // serviceDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceDataModel.getRowCount(), 3);
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "editing" })
    // public void testEditService() throws Exception {
    // Identity.setSecurityEnabled(false);
    // new FacesRequest() {
    // @Override
    // protected void beforeRequest() {
    // setParameter("objectId", "2");
    // setParameter("conversationPropagation", "join");
    // }
    //
    // protected void applyRequestValues() throws Exception {
    // login();
    // invokeAction("#{serviceInstanceBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{serviceInstance.code}", "Code_new");
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{serviceInstanceBean.saveOrUpdate}"),
    // "serviceInstances");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Assert.assertEquals(getValue("#{serviceInstanceBean.instance.code}"),
    // null);
    //
    // Object services = getValue("#{serviceInstances}");
    // Assert.assertTrue(services instanceof PaginationDataModel<?>);
    // PaginationDataModel<ServiceInstance> serviceDataModel =
    // (PaginationDataModel<ServiceInstance>) services;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceDataModel.getRowCount(), 3);
    //
    // // Load data
    // serviceDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // assertService(serviceDataModel, 2, "Code_new");
    // assertService(serviceDataModel, 3, "Code_3");
    //
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "creating" })
    // public void testAddService() throws Exception {
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    // @Override
    // protected void beforeRequest() {
    // setParameter("conversationPropagation", "begin");
    // }
    //
    // protected void applyRequestValues() throws Exception {
    // login();
    // invokeAction("#{serviceInstanceBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{serviceInstance.code}", "Code_1");
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{serviceInstanceBean.saveOrUpdate}"),
    // "serviceInstances");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Assert.assertEquals(getValue("#{serviceInstanceBean.instance.code}"),
    // null);
    //
    // Object services = getValue("#{serviceInstances}");
    // Assert.assertTrue(services instanceof PaginationDataModel<?>);
    // PaginationDataModel<ServiceInstance> serviceDataModel =
    // (PaginationDataModel<ServiceInstance>) services;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceDataModel.getRowCount(), 4);
    //
    // }
    //
    // }.run();
    // }

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
    @SuppressWarnings("unused")
    private void assertService(PaginationDataModel<ServiceInstance> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof ServiceInstance);
        ServiceInstance service = (ServiceInstance) rowData;
        Assert.assertEquals(service.getCode(), code);
        Assert.assertTrue(service.getId() == row);
    }
}
