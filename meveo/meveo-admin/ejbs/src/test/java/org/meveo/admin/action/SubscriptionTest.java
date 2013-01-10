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
import org.meveo.model.billing.Subscription;

/**
 * Integration tests for {@link Subscription} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.27
 * 
 */
public class SubscriptionTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Subscription.dbunit.xml"));
    }

    // @Test(groups = { "integration", "display" })
    // public void testDisplaySubscriptions() throws Exception {
    //
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Object subscriptions = getValue("#{subscriptions}");
    // Assert.assertTrue(subscriptions instanceof PaginationDataModel<?>);
    // PaginationDataModel<Subscription> subscriptionsDataModel =
    // (PaginationDataModel<Subscription>) subscriptions;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(subscriptionsDataModel.getRowCount(), 3);
    //
    // // Load data
    // subscriptionsDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // assertSubscription(subscriptionsDataModel, 2, "Code_2");
    // assertSubscription(subscriptionsDataModel, 3, "Code_3");
    // assertSubscription(subscriptionsDataModel, 4, "Code_4");
    //
    // }
    //
    // }.run();
    //
    // }
    //
    // @Test(groups = { "integration", "filtering" })
    // public void testFilterSubscriptions() throws Exception {
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object subscriptions = getValue("#{subscriptions}");
    // Assert.assertTrue(subscriptions instanceof PaginationDataModel<?>);
    // PaginationDataModel<Subscription> subscriptionsDataModel =
    // (PaginationDataModel<Subscription>) subscriptions;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // // filters.put("offerCode", "Offer_Code_3");
    //
    // // Load data
    // subscriptionsDataModel.addFilters(filters);
    // subscriptionsDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(subscriptionsDataModel.getRowCount(), 1);
    // }
    //
    // }.run();
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    //
    // Object subscriptions = getValue("#{subscriptions}");
    // Assert.assertTrue(subscriptions instanceof PaginationDataModel<?>);
    // PaginationDataModel<Subscription> subscriptionsDataModel =
    // (PaginationDataModel<Subscription>) subscriptions;
    //
    // // Adding filter parameters
    // Map<String, Object> filters = new HashMap<String, Object>();
    // //filters.put("offerCode", "Offer_Code*");
    // // Load data
    // subscriptionsDataModel.addFilters(filters);
    // subscriptionsDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for the correct number of results
    // Assert.assertEquals(subscriptionsDataModel.getRowCount(), 3);
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "editing" })
    // public void testEditSubscription() throws Exception {
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
    // invokeAction("#{subscriptionBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // //setValue("#{subscription.offerCode}", "offerCode2_New");
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{subscriptionBean.saveOrUpdate}"),
    // "subscriptions");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Assert.assertEquals(getValue("#{subscriptionBean.instance.code}"), null);
    // //
    // Assert.assertEquals(getValue("#{subscriptionBean.instance.offerCode}"),
    // null);
    //
    // Object subscriptions = getValue("#{subscriptions}");
    // Assert.assertTrue(subscriptions instanceof PaginationDataModel<?>);
    // PaginationDataModel<Subscription> subscriptionsDataModel =
    // (PaginationDataModel<Subscription>) subscriptions;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(subscriptionsDataModel.getRowCount(), 3);
    //
    // // Load data
    // subscriptionsDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // // assertSubscription(subscriptionsDataModel, 2, "offerCode2_New");
    // assertSubscription(subscriptionsDataModel, 3, "Offer_Code_3");
    // }
    //
    // }.run();
    // }
    //
    // @Test(groups = { "integration", "creating" })
    // public void testAddSubscription() throws Exception {
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
    // invokeAction("#{subscriptionBean.init}");
    // }
    //
    // @Override
    // protected void updateModelValues() throws Exception {
    // setValue("#{subscription.code}", "Code_1");
    // //setValue("#{subscription.offerCode}", "offerCode_1");
    // setValue("#{subscription.description}", "Description_1");
    //
    // }
    //
    // @Override
    // protected void invokeApplication() throws Exception {
    // Assert.assertEquals(invokeAction("#{subscriptionBean.saveOrUpdate}"),
    // "subscriptions");
    // }
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Assert.assertEquals(getValue("#{subscriptionBean.instance.code}"), null);
    // //Assert.assertEquals(getValue("#{subscriptionBean.instance.offerCode}"),
    // null);
    //
    // Object subscriptions = getValue("#{subscriptions}");
    // Assert.assertTrue(subscriptions instanceof PaginationDataModel<?>);
    // PaginationDataModel<Subscription> subscriptionsDataModel =
    // (PaginationDataModel<Subscription>) subscriptions;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(subscriptionsDataModel.getRowCount(), 4);
    //
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
    // private void assertSubscription(PaginationDataModel<Subscription>
    // dataModel, long row, String offerCode) {
    // dataModel.setRowKey(row);
    // Object rowData = dataModel.getRowData();
    // Assert.assertTrue(rowData instanceof Subscription);
    // Subscription subscription = (Subscription) rowData;
    // // Assert.assertEquals(subscription.getOfferCode(), offerCode);
    // Assert.assertTrue(subscription.getId() == row);
    // }
}
