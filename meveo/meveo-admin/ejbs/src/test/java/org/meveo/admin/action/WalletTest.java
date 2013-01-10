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
import org.meveo.model.billing.Wallet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration tests for {@link Wallet} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.12
 * 
 */

public class WalletTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Wallet.dbunit.xml"));
    }

    @Test(groups = { "integration", "display" })
    public void testDisplayWallet() throws Exception {

        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Object wallets = getValue("#{wallets}");
                Assert.assertTrue(wallets instanceof PaginationDataModel<?>);
                PaginationDataModel<Wallet> walletsDataModel = (PaginationDataModel<Wallet>) wallets;

                // Check for the correct number of results
                Assert.assertEquals(walletsDataModel.getRowCount(), 3);

                // Load data
                walletsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertWallet(walletsDataModel, 2, "PRINCIPAL_2");
                assertWallet(walletsDataModel, 3, "PRINCIPAL_3");
                assertWallet(walletsDataModel, 4, "PRINCIPAL_4");

            }

        }.run();

    }

    @Test(groups = { "integration", "filtering" })
    public void testFilterWallets() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object wallets = getValue("#{wallets}");
                Assert.assertTrue(wallets instanceof PaginationDataModel<?>);
                PaginationDataModel<Wallet> walletsDataModel = (PaginationDataModel<Wallet>) wallets;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "PRINCIPAL_2");

                // Load data
                walletsDataModel.addFilters(filters);
                walletsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(walletsDataModel.getRowCount(), 1);
            }

        }.run();
        new FacesRequest() {

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {

                Object wallets = getValue("#{wallets}");
                Assert.assertTrue(wallets instanceof PaginationDataModel<?>);
                PaginationDataModel<Wallet> walletsDataModel = (PaginationDataModel<Wallet>) wallets;

                // Adding filter parameters
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("name", "PRINCIPAL*");

                // Load data
                walletsDataModel.addFilters(filters);
                walletsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for the correct number of results
                Assert.assertEquals(walletsDataModel.getRowCount(), 3);
            }

        }.run();

    }

    @Test(groups = { "integration", "editing" })
    public void testEditWallet() throws Exception {
        Identity.setSecurityEnabled(false);

        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("objectId", "2");
                setParameter("conversationPropagation", "join");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{walletBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{wallet.name}", "PRINCIPAL_NEW_NAME");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{walletBean.saveOrUpdate}"), "wallets");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{walletBean.instance.name}"), "PRINCIPAL");

                Object wallets = getValue("#{wallets}");
                Assert.assertTrue(wallets instanceof PaginationDataModel<?>);
                PaginationDataModel<Wallet> walletsDataModel = (PaginationDataModel<Wallet>) wallets;

                // Check for the correct number of results
                Assert.assertEquals(walletsDataModel.getRowCount(), 3);

                // Load data
                walletsDataModel.walk(null, dataVisitor, sequenceRange, null);

                // Check for correct ordering
                assertWallet(walletsDataModel, 2, "PRINCIPAL_NEW_NAME");
                assertWallet(walletsDataModel, 3, "PRINCIPAL_3");
            }

        }.run();
    }

    @Test(groups = { "integration", "creating" })
    public void testAddWallet() throws Exception {
        new FacesRequest() {
            @Override
            protected void beforeRequest() {
                setParameter("conversationPropagation", "begin");
            }

            protected void applyRequestValues() throws Exception {
                login();
                invokeAction("#{walletBean.init}");
            }

            @Override
            protected void updateModelValues() throws Exception {
                setValue("#{wallet.name}", "PRINCIPAL_1");
            }

            @Override
            protected void invokeApplication() throws Exception {
                Assert.assertEquals(invokeAction("#{walletBean.saveOrUpdate}"), "wallets");
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                Assert.assertEquals(getValue("#{walletBean.instance.name}"), "PRINCIPAL");

                Object wallets = getValue("#{wallets}");
                Assert.assertTrue(wallets instanceof PaginationDataModel<?>);
                PaginationDataModel<Wallet> walletsDataModel = (PaginationDataModel<Wallet>) wallets;

                // Check for the correct number of results
                walletsDataModel.forceRefresh();
                Assert.assertEquals(walletsDataModel.getRowCount(), 4);
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
    private void assertWallet(PaginationDataModel<Wallet> dataModel, long row, String name) {
        dataModel.setRowKey(row);
        Object rowData = dataModel.getRowData();
        Assert.assertTrue(rowData instanceof Wallet);
        Wallet wallet = (Wallet) rowData;
        Assert.assertEquals(wallet.getName(), name);
        Assert.assertTrue(wallet.getId() == row);
    }

}
