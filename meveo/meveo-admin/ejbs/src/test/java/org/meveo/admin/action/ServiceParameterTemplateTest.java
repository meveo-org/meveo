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
import org.meveo.model.catalog.ServiceParameterTemplate;

/**
 * Integration tests for {@link ServiceParameterTemplate} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.27
 * 
 */
public class ServiceParameterTemplateTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        // TODO Auto-generated method stub

    }

    // @Override
    // protected void prepareDBUnitOperations() {
    // beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
    // beforeTestOperations.add(new
    // DataSetOperation("dataSets/ServiceParameterTemplate.dbunit.xml"));
    // }
    //
    // @Test(groups = { "integration", "display" })
    // public void testDisplayServiceParameterTemplates() throws Exception {
    //
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Object serviceParameterTemplates =
    // getValue("#{serviceParameterTemplates}");
    // Assert.assertTrue(serviceParameterTemplates instanceof
    // PaginationDataModel<?>);
    // PaginationDataModel<ServiceParameterTemplate>
    // serviceParameterTemplatesDataModel =
    // (PaginationDataModel<ServiceParameterTemplate>)
    // serviceParameterTemplates;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceParameterTemplatesDataModel.getRowCount(), 3);
    //
    // // Load data
    // serviceParameterTemplatesDataModel.walk(null, dataVisitor, sequenceRange,
    // null);
    //
    // // Check for correct ordering
    // assertServiceParameterTemplates(serviceParameterTemplatesDataModel, 2,
    // "Code_2");
    // assertServiceParameterTemplates(serviceParameterTemplatesDataModel, 3,
    // "Code_3");
    // assertServiceParameterTemplates(serviceParameterTemplatesDataModel, 4,
    // "Code_4");
    //
    // }
    //
    // }.run();
    //
    // }
    //
    // @Test(groups = { "integration", "filtering" })
    // public void testFilterServiceParameterTemplates() throws Exception {
    // Identity.setSecurityEnabled(false);
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object serviceParameterTemplates =
    // getValue("#{serviceParameterTemplates}");
    // Assert.assertTrue(serviceParameterTemplates instanceof
    // PaginationDataModel<?>);
    // PaginationDataModel<ServiceParameterTemplate>
    // serviceParameterTemplatesDataModel =
    // (PaginationDataModel<ServiceParameterTemplate>)
    // serviceParameterTemplates;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // filters.put("name", "Name_2");
    //
    // // Load data
    // serviceParameterTemplatesDataModel.addFilters(filters);
    // serviceParameterTemplatesDataModel.walk(null, dataVisitor, sequenceRange,
    // null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceParameterTemplatesDataModel.getRowCount(), 1);
    // }
    //
    // }.run();
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object serviceParameterTemplates =
    // getValue("#{serviceParameterTemplates}");
    // Assert.assertTrue(serviceParameterTemplates instanceof
    // PaginationDataModel<?>);
    // PaginationDataModel<ServiceParameterTemplate>
    // serviceParameterTemplatesDataModel =
    // (PaginationDataModel<ServiceParameterTemplate>)
    // serviceParameterTemplates;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // filters.put("name", "Name_*");
    //
    // // Load data
    // serviceParameterTemplatesDataModel.addFilters(filters);
    // serviceParameterTemplatesDataModel.walk(null, dataVisitor, sequenceRange,
    // null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceParameterTemplatesDataModel.getRowCount(), 3);
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "editing" })
    // public void testEditServiceParameterTemplate() throws Exception {
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    // @Override
    // protected void beforeRequest() {
    // setParameter("objectId", "2");
    // setParameter("conversationPropagation", "join");
    // }
    //
    // protected void applyRequestValues() throws Exception {
    // login();
    // invokeAction("#{serviceParameterTemplateBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{serviceParameterTemplate.code}", "Code_New");
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{serviceParameterTemplateBean.saveOrUpdate}"),
    // "serviceParameterTemplates");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Assert.assertEquals(getValue("#{serviceParameterTemplateBean.instance.code}"),
    // null);
    // Assert.assertEquals(getValue("#{serviceParameterTemplateBean.instance.name}"),
    // null);
    // Assert.assertEquals(getValue("#{serviceParameterTemplateBean.instance.serviceTemplates}"),
    // null);
    //
    // Object serviceParameterTemplates =
    // getValue("#{serviceParameterTemplates}");
    // Assert.assertTrue(serviceParameterTemplates instanceof
    // PaginationDataModel<?>);
    // PaginationDataModel<ServiceParameterTemplate>
    // serviceParameterTemplatesDataModel =
    // (PaginationDataModel<ServiceParameterTemplate>)
    // serviceParameterTemplates;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceParameterTemplatesDataModel.getRowCount(), 3);
    //
    // // Load data
    // serviceParameterTemplatesDataModel.walk(null, dataVisitor, sequenceRange,
    // null);
    //
    // // Check for correct ordering
    // assertServiceParameterTemplates(serviceParameterTemplatesDataModel, 2,
    // "Code_New");
    // assertServiceParameterTemplates(serviceParameterTemplatesDataModel, 3,
    // "Code_3");
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "creating" })
    // public void testAddServiceParameterTemplate() throws Exception {
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
    // invokeAction("#{serviceParameterTemplateBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{serviceParameterTemplate.code}", "Code_1");
    // setValue("#{serviceParameterTemplate.name}", "Name_1");
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{serviceParameterTemplateBean.saveOrUpdate}"),
    // "serviceParameterTemplates");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Assert.assertEquals(getValue("#{serviceTemplateParameterBean.instance.code}"),
    // null);
    // Assert.assertEquals(getValue("#{serviceTemplateParameterBean.instance.name}"),
    // null);
    // Assert.assertEquals(getValue("#{serviceTemplateParameterBean.instance.serviceParameterTemplates}"),
    // null);
    //
    // Object serviceParameterTemplates =
    // getValue("#{serviceParameterTemplates}");
    // Assert.assertTrue(serviceParameterTemplates instanceof
    // PaginationDataModel<?>);
    // PaginationDataModel<ServiceParameterTemplate>
    // serviceParameterTemplatesDataModel =
    // (PaginationDataModel<ServiceParameterTemplate>)
    // serviceParameterTemplates;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(serviceParameterTemplatesDataModel.getRowCount(), 4);
    // }
    //
    // }.run();
    // }
    //
    // /**
    // * Check correct entity values from dataModel
    // *
    // * @param dataModel
    // * filtered data model
    // * @param row
    // * Entities row (id)
    // * @param code
    // * Entities code to compare with existing
    // */
    // private void
    // assertServiceParameterTemplates(PaginationDataModel<ServiceParameterTemplate>
    // dataModel, long row,
    // String code) {
    // dataModel.setRowKey(row);
    // Object rowData = dataModel.getRowData();
    // Assert.assertTrue(rowData instanceof ServiceParameterTemplate);
    // ServiceParameterTemplate serviceParameterTemplate =
    // (ServiceParameterTemplate) rowData;
    // Assert.assertEquals(serviceParameterTemplate.getCode(), code);
    // Assert.assertTrue(serviceParameterTemplate.getId() == row);
    // }
}
