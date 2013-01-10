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



/**
 * Tests for MediatorTask class.
 * 
 * @author Donatas Remeika
 * @created Mar 5, 2009
 */
public class MediatorTaskTest {
	
//	/**
//	 * Tests if PA_MATCHINGS were successfully inserted even if main transaction
//	 * rollbacked.
//	 * 
//	 * If test fails, then after fixing it make sure that
//	 * test/tickets_behavior_after_rollback.txt files exists, and is not
//	 * renamed.
//	 * 
//	 * @throws Exception
//	 */
//	@Test(groups = { "db" })
//	public void testInsertPAMatchingAfterRollback() throws Exception {
//
//		try {
//			TransactionalMagicNumberCache.getInstance().getCache().clear();
//			MediatorTask mediatorTask = new MediatorTask() {
//				@Override
//				protected boolean updateCDRFile(CDRFile cdrFile,
//						FileProcessingResult result) {
//					throw new RuntimeException(
//							"Test behaviour when exception will force database rollback");
//				}
//			};
//			mediatorTask.execute(new File(
//					TEST_TICKETS_BEHAVIOUR_AFTER_ROLLBACK_FILE));
//
//			EntityManager em = MedinaPersistence.getEntityManager();
//			Query query = em
//					.createNativeQuery("SELECT count(*) FROM PA_MATCHING_T WHERE "
//							+ "PA_ID = 3001");
//			Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
//					1);
//
//			query = em
//					.createNativeQuery("SELECT count(*) FROM PA_MATCHING_T WHERE "
//							+ "PA_ID = 3002");
//			Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
//					1);
//
//		} finally {
//			// rename imported file back
//			File file = new File(TEST_TICKETS_BEHAVIOUR_AFTER_ROLLBACK_FILE
//					+ ".processing.failed");
//			file.renameTo(new File(TEST_TICKETS_BEHAVIOUR_AFTER_ROLLBACK_FILE));
//
//			try {
//				// Delete inserted PAMAtchings for other tests
//				EntityManager deleteManager = MedinaPersistence
//						.getEntityManager();
//				deleteManager.getTransaction().begin();
//				// delete PAMatching that could be inserted by other tests
//				Query deleteQuery = deleteManager
//						.createQuery("delete from PAMatching where accessPointId = 3001 or accessPointId = 3002");
//				deleteQuery.executeUpdate();
//				deleteManager.getTransaction().commit();
//			} catch (Exception e) {
//				MedinaPersistence.getEntityManager().getTransaction()
//						.rollback();
//				throw e;
//			} finally {
//				MedinaPersistence.closeEntityManager();
//			}
//		}
//	}

}
