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
import org.meveo.model.catalog.OfferTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link OfferTemplate} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.26
 * 
 */

public class OfferTemplateTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/OfferTemplate.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayOfferTemplate() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object offerTemplates = getValue("#{offerTemplates}");
                Assert.assertTrue(offerTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<OfferTemplate> offerTemplatesDataModel = (PaginationDataModel<OfferTemplate>) offerTemplates;

                // Check for the correct number of results
                Assert.assertEquals(offerTemplatesDataModel.getRowCount(), 3);

                // Load data
                offerTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertOfferTemplate(offerTemplatesDataModel, 2, "Code_2");
                assertOfferTemplate(offerTemplatesDataModel, 3, "Code_3");
                assertOfferTemplate(offerTemplatesDataModel, 4, "Code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterOfferTemplates() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object offerTemplates = getValue("#{offerTemplates}");
                Assert.assertTrue(offerTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<OfferTemplate> offerTemplatesDataModel = (PaginationDataModel<OfferTemplate>) offerTemplates;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_4");

                // Load data
                offerTemplatesDataModel.addFilters(filters);
                offerTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(offerTemplatesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object offerTemplates = getValue("#{offerTemplates}");
                Assert.assertTrue(offerTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<OfferTemplate> offerTemplatesDataModel = (PaginationDataModel<OfferTemplate>) offerTemplates;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Code_*");

                // Load data
                offerTemplatesDataModel.addFilters(filters);
                offerTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(offerTemplatesDataModel.getRowCount(), 3);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditOfferTemplate() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{offerTemplateBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{offerTemplate.code}", "Code_New");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{offerTemplateBean.saveOrUpdate}"), "offerTemplates");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{offerTemplateBean.instance.code}"), null);

                Object offerTemplates = getValue("#{offerTemplates}");
                Assert.assertTrue(offerTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<OfferTemplate> offerTemplatesDataModel = (PaginationDataModel<OfferTemplate>) offerTemplates;

                // Check for the correct number of results
                // TODO change row count to 4, because there s a bug
                Assert.assertEquals(offerTemplatesDataModel.getRowCount(), 3);

                // Load data
                offerTemplatesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                // TODO change YorkCommunity_2 to YorkCommunity_New
                assertOfferTemplate(offerTemplatesDataModel, 2, "Code_New");
                assertOfferTemplate(offerTemplatesDataModel, 3, "Code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddOfferTemolplate() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{offerTemplateBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{offerTemplate.code}", "CODE_1");

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{offerTemplateBean.saveOrUpdate}"), "offerTemplates");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Assert.assertEquals(getValue("#{offerTemplateBean.instance.code}"), null);

                Object offerTemplates = getValue("#{offerTemplates}");
                Assert.assertTrue(offerTemplates instanceof PaginationDataModel<?>);
                PaginationDataModel<OfferTemplate> offerTemplatesDataModel = (PaginationDataModel<OfferTemplate>) offerTemplates;

                // Check for the correct number of results
                // TODO change row count to 4, because there s a bug
                Assert.assertEquals(offerTemplatesDataModel.getRowCount(), 4);
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
    private void assertOfferTemplate(PaginationDataModel<OfferTemplate> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof OfferTemplate);
        OfferTemplate offerTemplate = (OfferTemplate) rowData;
        Assert.assertEquals(offerTemplate.getCode(), code);
        Assert.assertTrue(offerTemplate.getId() == row);
    }

}
