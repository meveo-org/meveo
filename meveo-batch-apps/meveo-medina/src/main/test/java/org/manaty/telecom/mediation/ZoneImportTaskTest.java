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
package org.manaty.telecom.mediation;

import java.io.File;

import javax.persistence.EntityManager;

import org.manaty.utils.ListUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ZoneImportTask.
 * 
 * @author Ignas
 * @created May 11, 2009
 */
public class ZoneImportTaskTest {

    private static final String TEST_IMPORT_TASK_FILES_DIR = "test/zoneimport/";

    @Test(groups = { "db" })
    public void testExecute() {
        EntityManager em = MedinaPersistence.getEntityManager();
        ZoneImportTask importTask = new ZoneImportTask();
        Number countBefore = (Number) em.createQuery("select count(*) from ZonningPlan").getSingleResult();
        importTask.execute("test/zoneimport/", ListUtils.createList(".csv1"));
        // rename imported file back
        File importFile = new File(TEST_IMPORT_TASK_FILES_DIR + "zones.csv1.processing.imported");
        importFile.renameTo(new File(TEST_IMPORT_TASK_FILES_DIR + "zones.csv1"));

        Number countAfter = (Number) em.createQuery("select count(*) from ZonningPlan").getSingleResult();
        Number count1 = (Number)em.createQuery("select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP_CSV1' and zp.plmnCode = '20620' and zp.zoneId = 'S1' and zp.zoneTypeId = 2").getSingleResult();
        Number count2 = (Number)em.createQuery("select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP_CSV1' and zp.plmnCode = '20601' and zp.zoneId = 'S1' and zp.zoneTypeId = 2").getSingleResult();
        Number count3 = (Number)em.createQuery("select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP_CSV1' and zp.plmnCode = '23806' and zp.zoneId = 'S2' and zp.zoneTypeId = 2").getSingleResult();
        Number count4 = (Number)em.createQuery("select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP_CSV1' and zp.plmnCode = '23820' and zp.zoneId = 'S2' and zp.zoneTypeId = 2").getSingleResult();

        Assert.assertEquals(countAfter.longValue() - countBefore.longValue(), 4L);
        Assert.assertEquals(count1.longValue(), 1L);
        Assert.assertEquals(count2.longValue(), 1L);
        Assert.assertEquals(count3.longValue(), 1L);
        Assert.assertEquals(count4.longValue(), 1L);
    }

    /**
     * No error when duplicated line in import file exists. 
     */
    @Test(groups = { "db" })
    public void testExecuteWithDuplicated() {
        EntityManager em = MedinaPersistence.getEntityManager();
        ZoneImportTask importTask = new ZoneImportTask();
        Number countBefore = (Number) em.createQuery("select count(*) from ZonningPlan").getSingleResult();
        importTask.execute(TEST_IMPORT_TASK_FILES_DIR, ListUtils.createList(".csv2"));
        // rename imported file back
        File importFile = new File(TEST_IMPORT_TASK_FILES_DIR + "zones_duplicated.csv2.processing.imported");
        importFile.renameTo(new File(TEST_IMPORT_TASK_FILES_DIR + "zones_duplicated.csv2"));

        Number countAfter = (Number) em.createQuery("select count(*) from ZonningPlan").getSingleResult();
        Number count = (Number)em.createQuery("select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_CSV2' and zp.plmnCode = '20620' and zp.zoneId = 'S1' and zp.zoneTypeId = 2").getSingleResult();
        Assert.assertEquals(countAfter.longValue() - countBefore.longValue(), 3L);
        Assert.assertEquals(count.longValue(), 2L);
    }

}
