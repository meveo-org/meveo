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
import org.meveo.model.shared.Title;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Title} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20011.05.18
 * 
 */
public class TitleTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Title.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayTitles() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object objects = getValue("#{titles}");
                Assert.assertTrue(objects instanceof PaginationDataModel<?>);
                PaginationDataModel<Title> objectDataModel = (PaginationDataModel<Title>) objects;

                // Check for the correct number of results
                Assert.assertEquals(objectDataModel.getRowCount(), 3);

                // Load data
                objectDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertObject(objectDataModel, 2, "TITLE_code_2");
                assertObject(objectDataModel, 3, "TITLE_code_3");
                assertObject(objectDataModel, 4, "TITLE_code_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterTitles() throws Exception {
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object objects = getValue("#{titles}");
                Assert.assertTrue(objects instanceof PaginationDataModel<?>);
                PaginationDataModel<Title> objectDataModel = (PaginationDataModel<Title>) objects;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "Title_code_2");

                // Load data
                objectDataModel.addFilters(filters);
                objectDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(objectDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                login();
                Object objects = getValue("#{titles}");
                Assert.assertTrue(objects instanceof PaginationDataModel<?>);
                PaginationDataModel<Title> objectDataModel = (PaginationDataModel<Title>) objects;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("code", "TITLE_code_*");

                // Load data
                objectDataModel.addFilters(filters);
                objectDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(objectDataModel.getRowCount(), 3);

            }
        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditTitle() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{titleBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{title.code}", "TITLE_mod");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{titleBean.saveOrUpdate}"), "titles");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{titleBean.instance.code}"), null);

                Object objects = getValue("#{titles}");
                Assert.assertTrue(objects instanceof PaginationDataModel<?>);
                PaginationDataModel<Title> objectDataModel = (PaginationDataModel<Title>) objects;

                // Check for the correct number of results
                Assert.assertEquals(objectDataModel.getRowCount(), 3);

                // Load data
                objectDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertObject(objectDataModel, 2, "TITLE_mod");
                assertObject(objectDataModel, 3, "TITLE_code_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddTitle() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{titleBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{title.code}", "TITLE_1");
                setValue("#{title.isCompany}", true);
                

            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{titleBean.saveOrUpdate}"), "titles");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{titleBean.instance.code}"), null);

                Object objects = getValue("#{titles}");
                Assert.assertTrue(objects instanceof PaginationDataModel<?>);
                PaginationDataModel<Title> objectDataModel = (PaginationDataModel<Title>) objects;
                // Check for the correct number of results'
                objectDataModel.forceRefresh();
                Assert.assertEquals(objectDataModel.getRowCount(), 4);

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
    private void assertObject(PaginationDataModel<Title> dataModel, long row, String code) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof Title);
        Title object = (Title) rowData;
        Assert.assertEquals(object.getCode(), code);
        Assert.assertTrue(object.getId() == row);
    }
}
