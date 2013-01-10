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
package org.meveo.service;

import org.jboss.seam.Component;
import org.manaty.BaseIntegrationTest;
import org.meveo.model.crm.Customer;
import org.meveo.service.api.dto.AddressDTO;
import org.meveo.service.api.remote.ApiServiceRemote;
import org.meveo.service.crm.local.CustomerServiceLocal;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiServiceTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
    }

    @Test(groups = { "remote" })
    public void createCustomerTest() throws Exception {

        this.initRemote();
        ApiServiceRemote apiService = (ApiServiceRemote) ctxRemote.lookup("ApiService/remote");

        AddressDTO address = new AddressDTO("address1 line", "address2 line", "address3 line", "1234", "Vilnius", "Lietuva", "VA");
        apiService.createCustomer("apiCust1", "API customer 1", "MYBRAND", "PRO", address, "12", "23", "PROV2");

        new ComponentTest() {
            @Override
            protected void testComponents() throws Exception {

                CustomerServiceLocal customerService = (CustomerServiceLocal) Component.getInstance("customerService");
                Customer customer = customerService.findByCode("APICUST1");
                Assert.assertNotNull(customer, "Customer should be created");
                Assert.assertEquals(customer.getDescription(), "API customer 1");

            }
        }.run();
    }
}