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
import org.meveo.model.admin.User;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link User} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.28
 * 
 */
public class UserTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayUsers() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();
                Object users = getValue("#{users}");
                Assert.assertTrue(users instanceof PaginationDataModel<?>);
                PaginationDataModel<User> usersDataModel = (PaginationDataModel<User>) users;

                // Check for the correct number of results
                Assert.assertEquals(usersDataModel.getRowCount(), 3);

                // Load data
                usersDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertUser(usersDataModel, 1, "MEVEO.ADMIN");
                assertUser(usersDataModel, 2, "GUI.ADMIN");
                assertUser(usersDataModel, 3, "admin");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterUsers() throws Exception {

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                login();

                Object users = getValue("#{users}");
                Assert.assertTrue(users instanceof PaginationDataModel<?>);
                PaginationDataModel<User> usersDataModel = (PaginationDataModel<User>) users;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("userName", "MEVEO*");

                // Load data
                usersDataModel.addFilters(filters);
                usersDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(usersDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object users = getValue("#{users}");
                Assert.assertTrue(users instanceof PaginationDataModel<?>);
                PaginationDataModel<User> usersDataModel = (PaginationDataModel<User>) users;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("userName", "*.ADMIN");

                // Load data
                usersDataModel.addFilters(filters);
                usersDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(usersDataModel.getRowCount(), 2);
            }

        }.run();
    }

    @Test(groups = { "integration", "editing" })
    public void testEditUser() throws Exception {

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{userBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{user.userName}", "name_modified");
                setValue("#{userBean.password}", "password");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{userBean.saveOrUpdate}"), null);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{userBean.instance.userName}"), null);

                Object users = getValue("#{users}");
                Assert.assertTrue(users instanceof PaginationDataModel<?>);
                PaginationDataModel<User> usersDataModel = (PaginationDataModel<User>) users;

                // Check for the correct number of results
                Assert.assertEquals(usersDataModel.getRowCount(), 3);
                // Load data
                usersDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertUser(usersDataModel, 2, "name_modified");
                assertUser(usersDataModel, 3, "admin");
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
     * @param userName
     *            Entities userName to compare with existing
     */
    private void assertUser(PaginationDataModel<User> dataModel, long row, String userName) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof User);
        User user = (User) rowData;
        Assert.assertEquals(user.getUserName(), userName);
        Assert.assertTrue(user.getId() == row);
    }
}
