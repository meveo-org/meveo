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
import org.meveo.model.rating.MatrixEntry;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link MatrixEntry} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.26
 * 
 */
public class MatrixEntryTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/MatrixEntry.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayMatrixEntries() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object matrixEntries = getValue("#{matrixEntries}");
                Assert.assertTrue(matrixEntries instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixEntry> matrixEntriesDataModel = (PaginationDataModel<MatrixEntry>) matrixEntries;

                // Check for the correct number of results
                Assert.assertEquals(matrixEntriesDataModel.getRowCount(), 3);

                // Load data
                matrixEntriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertMatrixEntries(matrixEntriesDataModel, 2, "Coordinates_2");
                assertMatrixEntries(matrixEntriesDataModel, 3, "Coordinates_3");
                assertMatrixEntries(matrixEntriesDataModel, 4, "Coordinates_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterMatrixEntries() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object matrixEntries = getValue("#{matrixEntries}");
                Assert.assertTrue(matrixEntries instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixEntry> matrixEntriesDataModel = (PaginationDataModel<MatrixEntry>) matrixEntries;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("coordinates", "Coordinates_2");

                // Load data
                matrixEntriesDataModel.addFilters(filters);
                matrixEntriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(matrixEntriesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object matrixEntries = getValue("#{matrixEntries}");
                Assert.assertTrue(matrixEntries instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixEntry> matrixEntriesDataModel = (PaginationDataModel<MatrixEntry>) matrixEntries;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("coordinates", "Coordinates_*");
                filters.put("value", "*");

                // Load data
                matrixEntriesDataModel.addFilters(filters);
                matrixEntriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(matrixEntriesDataModel.getRowCount(), 3);
            }

        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditMatrixEntry() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{matrixEntryBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{matrixEntry.coordinates}", "Coordinates_New");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{matrixEntryBean.saveOrUpdate}"), "matrixEntries");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{matrixEntryBean.instance.coordinates}"), null);
                Assert.assertEquals(getValue("#{matrixEntryBean.instance.value}"), null);
                Assert.assertEquals(getValue("#{matrixEntryBean.instance.matrixDefinition}"), null);

                Object matrixEntries = getValue("#{matrixEntries}");
                Assert.assertTrue(matrixEntries instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixEntry> matrixEntriesDataModel = (PaginationDataModel<MatrixEntry>) matrixEntries;

                // Check for the correct number of results
                Assert.assertEquals(matrixEntriesDataModel.getRowCount(), 3);

                // Load data
                matrixEntriesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertMatrixEntries(matrixEntriesDataModel, 2, "Coordinates_New");
                assertMatrixEntries(matrixEntriesDataModel, 3, "Coordinates_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddMatrixEntry() throws Exception {
        Identity.setSecurityEnabled(false);
        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{matrixEntryBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{matrixEntry.coordinates}", "Coordinates_1");
                setValue("#{matrixEntry.value}", "value_1");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{matrixEntryBean.saveOrUpdate}"), "matrixEntries");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{matrixEntryBean.instance.coordinates}"), null);
                Assert.assertEquals(getValue("#{matrixEntryBean.instance.value}"), null);
                Assert.assertEquals(getValue("#{matrixEntryBean.instance.matrixDefinition}"), null);

                Object matrixEntries = getValue("#{matrixEntries}");
                Assert.assertTrue(matrixEntries instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixEntry> matrixEntriesDataModel = (PaginationDataModel<MatrixEntry>) matrixEntries;

                // Check for the correct number of results
                Assert.assertEquals(matrixEntriesDataModel.getRowCount(), 4);

                // Load data
                matrixEntriesDataModel.walk(null, dataVisitor, sequenceRange, null);

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
     *            Entities coordinates to compare with existing
     */
    private void assertMatrixEntries(PaginationDataModel<MatrixEntry> dataModel, long row, String coordinates) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof MatrixEntry);
        MatrixEntry matrixEntry = (MatrixEntry) rowData;
        Assert.assertEquals(matrixEntry.getCoordinates(), coordinates);
        Assert.assertTrue(matrixEntry.getId() == row);
    }

}
