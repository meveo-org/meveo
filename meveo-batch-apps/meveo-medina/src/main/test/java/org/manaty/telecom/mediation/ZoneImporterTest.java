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

import org.testng.Assert;
import org.testng.annotations.Test;

public class ZoneImporterTest {

	private static final String TEST_IMPORT_FILE_NAME = "test/zones.csv";

	private static final String TEST_IMPORT_CORRUPTED_FILE_NAME = "test/zones_corrupted.csv";

	private static final String TEST_IMPORT_CONCRETE_FILE_NAME = "test/zones_concrete.csv";

	@Test(groups = "db")
	public void testZoneImporting() throws Exception {
		try {
			EntityManager em = MedinaPersistence.getEntityManager();
			Number countBefore = (Number) em.createQuery(
					"select count(*) from ZonningPlan").getSingleResult();
			ZoneImporter importer = new ZoneImporter(new File(
					TEST_IMPORT_FILE_NAME));
			em.getTransaction().begin();
			long imported = importer.importZones();
			em.getTransaction().commit();
			Number countAfter = (Number) em.createQuery(
					"select count(*) from ZonningPlan").getSingleResult();
			Assert.assertNotSame(imported, 0L);
			Assert.assertEquals(countAfter.longValue(), countBefore.longValue()
					+ imported);
		} catch (Exception e) {
			MedinaPersistence.getEntityManager().getTransaction().rollback();
			throw e;
		} finally {
			MedinaPersistence.closeEntityManager();
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class, groups = { "db" })
	public void testZoneImportingCorruptedFile() throws Exception {
		EntityManager em = MedinaPersistence.getEntityManager();
		ZoneImporter importer = new ZoneImporter(new File(
				TEST_IMPORT_CORRUPTED_FILE_NAME));
		try {
			em.getTransaction().begin();
			importer.importZones();
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

	@Test(groups = { "db" })
	public void testZoneImportingConcreteValues() throws Exception {
		try {
			EntityManager em = MedinaPersistence.getEntityManager();
			ZoneImporter importer = new ZoneImporter(new File(
					TEST_IMPORT_CONCRETE_FILE_NAME));
			Number countBefore = (Number) em.createQuery(
					"select count(*) from ZonningPlan").getSingleResult();
			em.getTransaction().begin();
			long imported = importer.importZones();
			em.getTransaction().commit();
			Number countAfter = (Number) em.createQuery(
					"select count(*) from ZonningPlan").getSingleResult();
			Number count1 = (Number) em
					.createQuery(
							"select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP1' and zp.plmnCode = '20280' and zp.zoneId = 'S1' and zp.zoneTypeId = 0")
					.getSingleResult();
			Number count2 = (Number) em
					.createQuery(
							"select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP2' and zp.plmnCode = '20281' and zp.zoneId = 'S2' and zp.zoneTypeId = 1")
					.getSingleResult();
			Number count3 = (Number) em
					.createQuery(
							"select count(*) from ZonningPlan zp where zp.offerCode = 'INGENICO_SANS_BACKUP3' and zp.plmnCode = '20282' and zp.zoneId = 'S3' and zp.zoneTypeId = 2")
					.getSingleResult();
			Assert.assertEquals(imported, 3L);
			Assert.assertEquals(countAfter.longValue()
					- countBefore.longValue(), imported);
			Assert.assertEquals(count1, 1L);
			Assert.assertEquals(count2, 1L);
			Assert.assertEquals(count3, 1L);
		} catch (Exception e) {
			MedinaPersistence.getEntityManager().getTransaction().rollback();
			throw e;
		} finally {
			MedinaPersistence.closeEntityManager();
		}
	}
}
