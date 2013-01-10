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

import org.manaty.BaseIntegrationTest;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.rating.MatrixDefinition;
import org.meveo.model.rating.MatrixEntryType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link MatrixDefinition} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.26
 * 
 */
public class MatrixDefinitionTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/MatrixDefinition.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayMatrixDefinitions() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object matrixes = getValue("#{matrixes}");
                Assert.assertTrue(matrixes instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixDefinition> matrixesDataModel = (PaginationDataModel<MatrixDefinition>) matrixes;

                // Check for the correct number of results
                Assert.assertEquals(matrixesDataModel.getRowCount(), 3);

                // Load data
                matrixesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertMatrixes(matrixesDataModel, 2, "Matrix_Definition_2");
                assertMatrixes(matrixesDataModel, 3, "Matrix_Definition_3");
                assertMatrixes(matrixesDataModel, 4, "Matrix_Definition_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterMatrixDefinitions() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object matrixes = getValue("#{matrixes}");
                Assert.assertTrue(matrixes instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixDefinition> matrixesDataModel = (PaginationDataModel<MatrixDefinition>) matrixes;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "Matrix_Definition_2");

                // Load data
                matrixesDataModel.addFilters(filters);
                matrixesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(matrixesDataModel.getRowCount(), 1);
            }

        }.run();

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object matrixes = getValue("#{matrixes}");
                Assert.assertTrue(matrixes instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixDefinition> matrixesDataModel = (PaginationDataModel<MatrixDefinition>) matrixes;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("entryType", MatrixEntryType.STRING);

                // Load data
                matrixesDataModel.addFilters(filters);
                matrixesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(matrixesDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object matrixes = getValue("#{matrixes}");
                Assert.assertTrue(matrixes instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixDefinition> matrixesDataModel = (PaginationDataModel<MatrixDefinition>) matrixes;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("entryType", MatrixEntryType.NUMBER);

                // Load data
                matrixesDataModel.addFilters(filters);
                matrixesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(matrixesDataModel.getRowCount(), 2);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditMatrixDefinition() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{matrixBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{matrix.name}", "Matrix_Definition_New_Name");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{matrixBean.saveOrUpdate}"), "matrixes");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{matrixBean.instance.activated}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.name}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.dimension}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.usageType}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.entryType}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.startDate}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.endDate}"), null);

                Object matrixes = getValue("#{matrixes}");
                Assert.assertTrue(matrixes instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixDefinition> matrixesDataModel = (PaginationDataModel<MatrixDefinition>) matrixes;

                // Check for the correct number of results
                Assert.assertEquals(matrixesDataModel.getRowCount(), 3);

                // Load data
                matrixesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertMatrixes(matrixesDataModel, 2, "Matrix_Definition_New_Name");
                assertMatrixes(matrixesDataModel, 3, "Matrix_Definition_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddMatrixDefinition() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{matrixBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {

                setValue("#{matrix.activated}", getRandomBoolean());
                setValue("#{matrix.name}", "Matrix_Definition_1");
                setValue("#{matrix.dimension}", Long.valueOf(2));
                setValue("#{matrix.entryType}", MatrixEntryType.STRING);
                setValue("#{matrix.startDate}", getRandomDate());
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{matrixBean.saveOrUpdate}"), "matrixes");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{matrixBean.instance.activated}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.name}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.dimension}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.usageType}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.entryType}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.startDate}"), null);
                Assert.assertEquals(getValue("#{matrixBean.instance.endDate}"), null);

                Object matrixes = getValue("#{matrixes}");
                Assert.assertTrue(matrixes instanceof PaginationDataModel<?>);
                PaginationDataModel<MatrixDefinition> matrixesDataModel = (PaginationDataModel<MatrixDefinition>) matrixes;

                // Check for the correct number of results
                Assert.assertEquals(matrixesDataModel.getRowCount(), 4);
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
    private void assertMatrixes(PaginationDataModel<MatrixDefinition> dataModel, long row, String name) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof MatrixDefinition);
        MatrixDefinition matrixDefinition = (MatrixDefinition) rowData;
        Assert.assertEquals(matrixDefinition.getName(), name);
        Assert.assertTrue(matrixDefinition.getId() == row);
    }
}
