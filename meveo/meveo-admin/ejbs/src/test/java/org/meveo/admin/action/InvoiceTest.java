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
import org.meveo.model.billing.Invoice;

/**
 * Integration tests for {@link Invoice} entity.
 * 
 * @author Gediminas Ubartas
 * @created 20010.07.27
 * 
 */
public class InvoiceTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Wallet.dbunit.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Invoice.dbunit.xml"));
    }

    //
    // @Test(groups = { "integration", "display" })
    // public void testDisplayMatricDefinitions() throws Exception {
    //
    // Identity.setSecurityEnabled(false);
    //
    // new FacesRequest() {
    //
    // @SuppressWarnings("unchecked")
    // @Override
    // protected void renderResponse() throws Exception {
    // Object invoices = getValue("#{invoices}");
    // Assert.assertTrue(invoices instanceof PaginationDataModel<?>);
    // PaginationDataModel<Invoice> invoicesDataModel =
    // (PaginationDataModel<Invoice>) invoices;
    //
    // // Check for the correct number of results
    // Assert.assertEquals(invoicesDataModel.getRowCount(), 3);
    //
    // // Load data
    // invoicesDataModel.walk(null, dataVisitor, sequenceRange, null);
    //
    // // Check for correct ordering
    // assertInvoices(invoicesDataModel, 2, "Matrix_Definition_2");
    // assertInvoices(invoicesDataModel, 3, "Matrix_Definition_3");
    // assertInvoices(invoicesDataModel, 4, "Matrix_Definition_4");
    //
    // }
    //
    // }.run();
    //
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
    private void assertInvoices(PaginationDataModel<Invoice> dataModel, long row, String code) {
        /*
         * dataModel.setRowKey(row); Object rowData = dataModel.getRowData();
         * Assert.assertTrue(rowData instanceof Invoice); Invoice invoice =
         * (Invoice) rowData; Assert.assertEquals(invoice.getCode(), code);
         * Assert.assertTrue(invoice.getId() == row);
         */
    }
}
