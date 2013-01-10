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

import org.manaty.BaseIntegrationTest;
import org.meveo.service.selfcare.remote.SelfcareServiceRemote;
import org.testng.annotations.Test;

public class SelfcareServiceTest extends BaseIntegrationTest {

    @Test(enabled = false, groups = { "remote" })
    public void sendEmailsTest() throws Exception {
        this.initRemote();
        SelfcareServiceRemote selfcareService = (SelfcareServiceRemote) ctxRemote.lookup("SelfcareService/remote");
        selfcareService.sendPassword("tyshan@manaty.net");
        System.out.println(selfcareService.authenticate("tyshan@manaty.net", "7326"));
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/CustomerAccount.dbunit.xml"));
    }
}