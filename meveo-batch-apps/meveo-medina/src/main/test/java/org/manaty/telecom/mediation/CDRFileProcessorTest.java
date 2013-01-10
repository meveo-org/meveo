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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.manaty.model.mediation.RejectedCDR;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache.CacheTransaction;
import org.manaty.utils.FileFormat;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Integration tests.
 * 
 * @author Ignas
 *
 */
public class CDRFileProcessorTest {
    
	@Test(groups = { "db" })
	public void testProcessDigi() throws Exception {
		EntityManager em = MedinaPersistence.getEntityManager();
		CacheTransaction cacheTransaction = TransactionalMagicNumberCache.getInstance().getTransaction();
		
		em.getTransaction().begin();
		CDRFileProcessor processor = new CDRFileProcessor("test/digiCDRs.txt", FileFormat.TXT, cacheTransaction);
		FileProcessingContext fileProcessingContext = new FileProcessingContext();
		
		Query dataCounterUpQuery = em.createQuery("select counter1Up from UsageCounter uc where uc.id = 1002");
		Long counterUpBefore = (Long)dataCounterUpQuery.getSingleResult();

		Query dataCounterDownQuery = em.createQuery("select counter1Down from UsageCounter uc where uc.id = 1002");
		Long counterDownBefore = (Long)dataCounterDownQuery.getSingleResult();

		FileProcessingResult result = processor.process(fileProcessingContext);

		em.getTransaction().commit();
		cacheTransaction.commit();
		
		Assert.assertEquals(result.getParsedCount(), 1);
		Assert.assertEquals(result.getAcceptedCount(), 1);
		Assert.assertEquals(result.getRejectedCount(), 0);
		
		Assert.assertEquals((Long)dataCounterUpQuery.getSingleResult(), (Long)(counterUpBefore + 88));
		Assert.assertEquals((Long)dataCounterDownQuery.getSingleResult(), (Long)(counterDownBefore + 79));
	}

	@Test(groups = { "db" })
	public void testProcessRouter() throws Exception {
		EntityManager em = MedinaPersistence.getEntityManager();
		CacheTransaction cacheTransaction = TransactionalMagicNumberCache.getInstance().getTransaction();
		
		em.getTransaction().begin();
		CDRFileProcessor processor = new CDRFileProcessor("test/routerCDRs.router", FileFormat.ROUTER, cacheTransaction);
		FileProcessingContext fileProcessingContext = new FileProcessingContext();
		
		Query dataCounterUpQuery = em.createQuery("select counter1Up from UsageCounter uc where uc.id = 9009");
		Long counterUpBefore = (Long)dataCounterUpQuery.getSingleResult();

		Query dataCounterDownQuery = em.createQuery("select counter1Down from UsageCounter uc where uc.id = 9009");
		Long counterDownBefore = (Long)dataCounterDownQuery.getSingleResult();

		FileProcessingResult result = processor.process(fileProcessingContext);

		em.getTransaction().commit();
		cacheTransaction.commit();
		
		Assert.assertEquals(result.getParsedCount(), 6);
		Assert.assertEquals(result.getAcceptedCount(), 2);
		Assert.assertEquals(result.getRejectedCount(), 4);
		Assert.assertEquals(result.getUsageDATAVolume(), 22l);
		
		Assert.assertEquals((Long)dataCounterDownQuery.getSingleResult(), (Long)(counterDownBefore));
		Assert.assertEquals((Long)dataCounterUpQuery.getSingleResult(), (Long)(counterUpBefore + 22));

		// test rejected tickets
		Query rejectedCDRQuery = em.createQuery("from RejectedCDR rc where rc.fileName = 'test/routerCDRs.router'");
		@SuppressWarnings("unchecked")
        List<RejectedCDR> rejectedCDRs = (List<RejectedCDR>)rejectedCDRQuery.getResultList();
		Assert.assertEquals(rejectedCDRs.size(), 1);
		Assert.assertEquals(rejectedCDRs.get(0).getRejectionReason(), "NO_ACCESS");
		Assert.assertEquals(rejectedCDRs.get(0).getTicketData(), "2.2.2.2    4.4.4.4  137   137     udp       4930     12000     116");
	}
	
	@Test(groups = { "db" })
	public void testProcessRouterBothIPKnown() throws Exception {
		EntityManager em = MedinaPersistence.getEntityManager();
		CacheTransaction cacheTransaction = TransactionalMagicNumberCache.getInstance().getTransaction();
		
		em.getTransaction().begin();
		CDRFileProcessor processor = new CDRFileProcessor("test/routerCDRsIPKnown.router", FileFormat.ROUTER, cacheTransaction);
		FileProcessingContext fileProcessingContext = new FileProcessingContext();
		
		FileProcessingResult result = processor.process(fileProcessingContext);

		em.getTransaction().commit();
		cacheTransaction.commit();
		
		Assert.assertEquals(result.getParsedCount(), 4);
		Assert.assertEquals(result.getAcceptedCount(), 4);
		Assert.assertEquals(result.getRejectedCount(), 0);
		
		// 5.5.5.5
		Query dataCounterUpQuery = em.createQuery("select counter1 from UsageCounter uc where uc.id = 9010");
		Query dataCounterDownQuery = em.createQuery("select counter1Down from UsageCounter uc where uc.id = 9010");
		Assert.assertEquals((Long)dataCounterDownQuery.getSingleResult(), (Long)0l);
		Assert.assertEquals((Long)dataCounterUpQuery.getSingleResult(), (Long)1l);

		// 6.6.6.6
		dataCounterUpQuery = em.createQuery("select counter1Up from UsageCounter uc where uc.id = 9011");
		dataCounterDownQuery = em.createQuery("select counter1Down from UsageCounter uc where uc.id = 9011");
		Assert.assertEquals((Long)dataCounterDownQuery.getSingleResult(), (Long)1l);
		Assert.assertEquals((Long)dataCounterUpQuery.getSingleResult(), (Long)2l);

		// 7.7.7.7
		dataCounterUpQuery = em.createQuery("select counter1Up from UsageCounter uc where uc.id = 9012");
		dataCounterDownQuery = em.createQuery("select counter1Down from UsageCounter uc where uc.id = 9012");
		Assert.assertEquals((Long)dataCounterDownQuery.getSingleResult(), (Long)2l);
		Assert.assertEquals((Long)dataCounterUpQuery.getSingleResult(), (Long)0l);
	}
}
