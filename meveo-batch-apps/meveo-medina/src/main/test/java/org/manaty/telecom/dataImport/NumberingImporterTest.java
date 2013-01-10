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
package org.manaty.telecom.dataImport;

import java.io.File;

import javax.persistence.EntityManager;

import org.manaty.telecom.mediation.MedinaPersistence;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for numbering plans.
 * 
 * @author Ignas Lelys
 * @created 2009.12.01
 */
public class NumberingImporterTest {
    
    private static final String TEST_IMPORT_FILE_NAME = "test/numberingPlans.csv";

    private static final String TEST_IMPORT_CORRUPTED_FILE_NAME = "test/numberingPlans_corrupted.csv";

    @Test(groups = "db")
    public void testNumberingImporting() throws Exception {
        EntityManager em = MedinaPersistence.getEntityManager();
        Number countBefore = (Number) em.createQuery("select count(*) from NumberingPlan").getSingleResult();
        NumberingImporter importer = new NumberingImporter(new File(TEST_IMPORT_FILE_NAME));
        long imported = 0;
        try {
	        em.getTransaction().begin();
	        imported = importer.importNumberingPlans();
	        em.getTransaction().commit();
        } catch (Exception e) {
        	MedinaPersistence.getEntityManager().getTransaction().rollback();
        	throw e;
        }
        Number countAfter = (Number) em.createQuery("select count(*) from NumberingPlan").getSingleResult();
        Number count1 = (Number)em.createQuery("select count(*) from NumberingPlan np where np.phonePrefix = '370' and np.phoneNumber = null and np.phoneNumberRegexp = null and np.offerCode = 'SOME_OFFER_CODE' and np.zoneId = 'Lithuania' and np.cdrType = 'VOICE' and np.outgoing = true").getSingleResult();
        Number count2 = (Number)em.createQuery("select count(*) from NumberingPlan np where np.phonePrefix = '370' and np.phoneNumber = null and np.phoneNumberRegexp = null and np.offerCode = null and np.zoneId = 'Lithuania' and np.cdrType = 'VOICE' and np.outgoing = true").getSingleResult();
        Number count3 = (Number)em.createQuery("select count(*) from NumberingPlan np where np.phonePrefix = '33' and np.phoneNumber = null and np.phoneNumberRegexp = null and np.offerCode = 'SOME_OFFER_CODE2' and np.zoneId = 'FRANCE' and np.cdrType = 'VOICE' and np.outgoing = true and np.specialNumberType = 'SMS_PLUS'").getSingleResult();
        Number count4 = (Number)em.createQuery("select count(*) from NumberingPlan np where np.phonePrefix = null and np.phoneNumber = '33888888888' and np.phoneNumberRegexp = null and np.offerCode = 'SOME_OFFER_CODE2' and np.zoneId = 'FRANCE' and np.cdrType = 'VOICE' and np.outgoing = true and np.specialNumberType = 'DATA_PLUS'").getSingleResult();
        Number count5 = (Number)em.createQuery("select count(*) from NumberingPlan np where np.phonePrefix = null and np.phoneNumber = null and np.phoneNumberRegexp = '338888*' and np.offerCode = 'SOME_OFFER_CODE3' and np.zoneId = 'FRANCE' and np.cdrType = 'VOICE' and np.outgoing = false").getSingleResult();
        Number count6 = (Number)em.createQuery("select count(*) from NumberingPlan np where np.phonePrefix = null and np.phoneNumber = null and np.phoneNumberRegexp = '338888*' and np.offerCode = null and np.zoneId = 'FRANCE' and np.cdrType = 'SMS' and np.outgoing = false").getSingleResult();
        Assert.assertNotSame(imported, 0L);
        Assert.assertEquals(count1.longValue(), 1L);
        Assert.assertEquals(count2.longValue(), 1L);
        Assert.assertEquals(count3.longValue(), 1L);
        Assert.assertEquals(count4.longValue(), 1L);
        Assert.assertEquals(count5.longValue(), 1L);
        Assert.assertEquals(count6.longValue(), 1L);
        Assert.assertEquals(countAfter.longValue(), countBefore.longValue() + imported);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = { "db" })
    public void testImporterImportingCorruptedFile() throws Exception {
        EntityManager em = MedinaPersistence.getEntityManager();
        NumberingImporter importer = new NumberingImporter(new File(TEST_IMPORT_CORRUPTED_FILE_NAME));
        try {
            em.getTransaction().begin();
            importer.importNumberingPlans();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

}
