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
import org.meveo.model.admin.Role;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Role} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.28
 * 
 */
public class RoleTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayRoles() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object roles = getValue("#{userRoles}");
                Assert.assertTrue(roles instanceof PaginationDataModel<?>);
                PaginationDataModel<Role> rolesDataModel = (PaginationDataModel<Role>) roles;

                // Check for the correct number of results
                Assert.assertEquals(rolesDataModel.getRowCount(), 3);

                // Load data
                rolesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertRole(rolesDataModel, 1, "meveo.admin");
                assertRole(rolesDataModel, 2, "gui.admin");
                assertRole(rolesDataModel, 3, "administrateur");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterUsers() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object roles = getValue("#{userRoles}");
                Assert.assertTrue(roles instanceof PaginationDataModel<?>);
                PaginationDataModel<Role> rolesDataModel = (PaginationDataModel<Role>) roles;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "meveo*");

                // Load data
                rolesDataModel.addFilters(filters);
                rolesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(rolesDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object roles = getValue("#{userRoles}");
                Assert.assertTrue(roles instanceof PaginationDataModel<?>);
                PaginationDataModel<Role> rolesDataModel = (PaginationDataModel<Role>) roles;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "*.admin");

                // Load data
                rolesDataModel.addFilters(filters);
                rolesDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(rolesDataModel.getRowCount(), 2);
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
    private void assertRole(PaginationDataModel<Role> dataModel, long row, String name) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof Role);
        Role role = (Role) rowData;
        Assert.assertEquals(role.getName(), name);
        Assert.assertTrue(role.getId() == row);
    }
}
