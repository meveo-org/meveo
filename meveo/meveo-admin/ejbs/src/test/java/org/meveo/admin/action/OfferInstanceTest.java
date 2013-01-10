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
import org.meveo.model.resource.OfferInstance;
import org.testng.Assert;

/**
 * Integration tests for {@link Offer} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.26
 * 
 */

public class OfferInstanceTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/OfferInstance.dbunit.xml"));

    }

    // @Test(groups = { "integration", "display" })
    // public void testDisplayOffers() throws Exception {
    //
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Object offers = getValue("#{offerInstances}");
    // Assert.assertTrue(offers instanceof PaginationDataModel<?>);
    // PaginationDataModel<OfferInstance> offersDataModel =
    // (PaginationDataModel<OfferInstance>) offers;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(offersDataModel.getRowCount(), 3);
    //
    // // Load data
    // offersDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // assertOffer(offersDataModel, 2, "Code_2");
    // assertOffer(offersDataModel, 3, "Code_3");
    // assertOffer(offersDataModel, 4, "Code_4");
    //
    // }
    //
    // }.run();
    //
    // }
    //
    // @Test(groups = { "integration", "filtering" })
    // public void testFilterOffers() throws Exception {
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object offers = getValue("#{offerInstances}");
    // Assert.assertTrue(offers instanceof PaginationDataModel<?>);
    // PaginationDataModel<OfferInstance> offersDataModel =
    // (PaginationDataModel<OfferInstance>) offers;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // filters.put("code", "Code_2");
    //
    // // Load data
    // offersDataModel.addFilters(filters);
    // offersDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(offersDataModel.getRowCount(), 1);
    // }
    //
    // }.run();
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object offers = getValue("#{offerInstances}");
    // Assert.assertTrue(offers instanceof PaginationDataModel<?>);
    // PaginationDataModel<OfferInstance> offersDataModel =
    // (PaginationDataModel<OfferInstance>) offers;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // filters.put("name", "Name*");
    //
    // // Load data
    // offersDataModel.addFilters(filters);
    // offersDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(offersDataModel.getRowCount(), 3);
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "editing" })
    // public void testEditOffer() throws Exception {
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
    // invokeAction("#{offerInstanceBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{offerInstance.code}", "Code_New");
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{offerInstanceBean.saveOrUpdate}"),
    // "offerInstances");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Assert.assertEquals(getValue("#{offerInstanceBean.instance.code}"),
    // null);
    // Assert.assertEquals(getValue("#{offerInstanceBean.instance.name}"),
    // null);
    // Assert.assertEquals(getValue("#{offerInstanceBean.instance.termDuration}"),
    // null);
    //
    // Object offers = getValue("#{offerInstances}");
    // Assert.assertTrue(offers instanceof PaginationDataModel<?>);
    // PaginationDataModel<OfferInstance> offersDataModel =
    // (PaginationDataModel<OfferInstance>) offers;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(offersDataModel.getRowCount(), 3);
    //
    // // Load data
    // offersDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // assertOffer(offersDataModel, 2, "Code_New");
    // assertOffer(offersDataModel, 3, "Code_3");
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "creating" })
    // public void testAddOffer() throws Exception {
    // new FacesRequest() {
    // @Override
    // protected void beforeRequest() {
    // setParameter("conversationPropagation", "begin");
    // }
    //
    // protected void applyRequestValues() throws Exception {
    // login();
    // invokeAction("#{offerInstanceBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{offerInstance.code}", "Code_1");
    // setValue("#{offerInstance.name}", "Name_1");
    // setValue("#{offerInstance.termDuration}", getRandomInt());
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{offerInstanceBean.saveOrUpdate}"),
    // "offerInstances");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Assert.assertEquals(getValue("#{offerInstanceBean.instance.code}"),
    // null);
    // Assert.assertEquals(getValue("#{offerInstanceBean.instance.name}"),
    // null);
    // Assert.assertEquals(getValue("#{offerInstanceBean.instance.termDuration}"),
    // null);
    //
    // Object offers = getValue("#{offerInstances}");
    // Assert.assertTrue(offers instanceof PaginationDataModel<?>);
    // PaginationDataModel<OfferInstance> offersDataModel =
    // (PaginationDataModel<OfferInstance>) offers;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(offersDataModel.getRowCount(), 4);
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
    private void assertOffer(PaginationDataModel<OfferInstance> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof OfferInstance);
        OfferInstance offer = (OfferInstance) rowData;
        Assert.assertEquals(offer.getCode(), code);
        Assert.assertTrue(offer.getId() == row);
    }

}
