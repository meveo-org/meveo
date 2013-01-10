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
package org.manaty.telecom.mediation.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.telecom.mediation.MedinaPersistence;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache.CacheTransaction;
import org.manaty.utils.MagicNumberConverter;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Tests for {@link TransactionalMagicNumberCache}.
 * 
 * @author Ignas Lelys
 * @created Mar 30, 2009
 */
public class TransactionalCacheManagerTest {

    private static final String DELETE_MEDINA_CDR = "DELETE FROM MEDINA_CDR";

    private static final String SELECT_MEDINA_CDR = "SELECT HASH FROM MEDINA_CDR";

    private static final String INSERT_MEDINA_CDR = "INSERT INTO MEDINA_CDR (ID, ANALYSIS_DATE, HASH) VALUES(:id, :date, :hash)";

    private List<byte[]> sampleHashes = new ArrayList<byte[]>();
    
    @BeforeGroups(groups = { "db" })
    public void init() {
        CDR cdr1 = new DATACDRWrapper(new BaseCDR.Builder().addNodeID("GGSN014").addIPBinV4Address("1.2.3.4").addRecordSequenceNumber("1")
                .addRecordOpeningTime(new Date()).build());
        CDR cdr2 = new DATACDRWrapper(new BaseCDR.Builder().addNodeID("GGSN014").addIPBinV4Address("1.2.3.4").addRecordSequenceNumber("2")
                .addRecordOpeningTime(new Date()).build());
        CDR cdr3 = new DATACDRWrapper(new BaseCDR.Builder().addNodeID("GGSN014").addIPBinV4Address("1.2.3.4").addRecordSequenceNumber("3")
                .addRecordOpeningTime(new Date()).build());
        CDR cdr4 = new DATACDRWrapper(new BaseCDR.Builder().addNodeID("GGSN014").addIPBinV4Address("1.2.3.4").addRecordSequenceNumber("4")
                .addRecordOpeningTime(new Date()).build());
        CDR cdr5 = new DATACDRWrapper(new BaseCDR.Builder().addNodeID("GGSN014").addIPBinV4Address("1.2.3.4").addRecordSequenceNumber("5")
                .addRecordOpeningTime(new Date()).build());
        CDR cdr6 = new DATACDRWrapper(new BaseCDR.Builder().addNodeID("GGSN014").addIPBinV4Address("1.2.3.4").addRecordSequenceNumber("6")
                .addRecordOpeningTime(new Date()).build());
        sampleHashes.add(cdr1.getMagicNumber());
        sampleHashes.add(cdr2.getMagicNumber());
        sampleHashes.add(cdr3.getMagicNumber());
        sampleHashes.add(cdr4.getMagicNumber());
        sampleHashes.add(cdr5.getMagicNumber());
        sampleHashes.add(cdr6.getMagicNumber());
    }
    
    @Test(groups = { "db" })
    public void testLoadCache() throws Exception {
        insertSampleDataToDatabase();
        TransactionalMagicNumberCache cacheManager = TransactionalMagicNumberCache.getInstance();
        
        final Method loadCacheMethod = TransactionalMagicNumberCache.class.getDeclaredMethod("loadCache");
        loadCacheMethod.setAccessible(true);
        
        loadCacheMethod.invoke(cacheManager);

        int lenght = cacheManager.getCache().toArray().length;
        if (lenght != sampleHashes.size()) {
            Assert.fail();
        }
        for (int i = 0; i < lenght; i++) {
            Assert.assertTrue(cacheManager.contains((byte[])sampleHashes.get(i)));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(groups = { "db" })
    public void testPersistCache() throws Exception {
        
        deleteHashesFromDatabase();
        
        TransactionalMagicNumberCache cacheManager = TransactionalMagicNumberCache.getInstance();
        
        final Field insertQuery = TransactionalMagicNumberCache.class.getDeclaredField("INSERT_MEDINA_CDR");
        insertQuery.setAccessible(true);
        insertQuery.set(cacheManager, "INSERT INTO MEDINA_CDR (ID, ANALYSIS_DATE, HASH) VALUES(null, SYSDATE, ?)");
        
        cacheManager.getCache().clear();
        CacheTransaction transaction =  TransactionalMagicNumberCache.getInstance().getTransaction();
        transaction.addToCache(MagicNumberConverter.convertToArray("112233445566778899aabbccddeefff0"));
        transaction.addToCache(MagicNumberConverter.convertToArray("112233445566778899aabbccddeefff1"));
        transaction.addToCache(MagicNumberConverter.convertToArray("112233445566778899aabbccddeefff2"));
        transaction.addToCache(MagicNumberConverter.convertToArray("112233445566778899aabbccddeefff3"));
        transaction.addToCache(MagicNumberConverter.convertToArray("112233445566778899aabbccddeefff4"));
        cacheManager.persistCacheTransaction(transaction);
        transaction.commit();

        EntityManager em = MedinaPersistence.getEntityManager();
        Query query = em.createNativeQuery(SELECT_MEDINA_CDR);
        List<String> persistedHashesInHexForm = query.getResultList();
        List<byte[]> persistedHashes = new ArrayList<byte[]>();
        for (String hash : persistedHashesInHexForm) {
            persistedHashes.add(MagicNumberConverter.convertToArray(hash));
        }
        
        int lenght = cacheManager.getCache().toArray().length;
        if (lenght != persistedHashes.size()) {
            Assert.fail();
        }
        for (int i = 0; i < lenght; i++) {
            Assert.assertEquals((byte[])cacheManager.getCache().toArray()[i], (byte[])persistedHashes.get(i));
        }
    }

    private void insertSampleDataToDatabase() throws Exception {
        try {
	        deleteHashesFromDatabase();
	        
	        MedinaPersistence.getEntityManager().getTransaction().begin();
	        EntityManager em = MedinaPersistence.getEntityManager();
	        // insert sample data to database
	        Query query = em.createNativeQuery(INSERT_MEDINA_CDR);
	        int id = 0;
	        for (byte[] hash : sampleHashes) {
	            id++;
	            query.setParameter("id", id);
	            query.setParameter("date", new Date());
	            query.setParameter("hash", MagicNumberConverter.convertToString(hash));
	            query.executeUpdate();
	        }
	        MedinaPersistence.getEntityManager().getTransaction().commit();
        } catch (Exception e) {
        	MedinaPersistence.getEntityManager().getTransaction().rollback();
        	throw e;
		} finally {
			MedinaPersistence.closeEntityManager();
		}
    }
    
    private void deleteHashesFromDatabase() throws Exception {
    	try {
	        MedinaPersistence.getEntityManager().getTransaction().begin();
	        // delete everything from database
	        MedinaPersistence.getEntityManager().createNativeQuery(DELETE_MEDINA_CDR).executeUpdate();
	        MedinaPersistence.getEntityManager().getTransaction().commit();
    	} catch (Exception e) {
        	MedinaPersistence.getEntityManager().getTransaction().rollback();
        	throw e;
		} finally {
			MedinaPersistence.closeEntityManager();
		}
    }
    
    @Test(groups = { "unit" })
    public void testCacheTransactionContains() {
        CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(100L).addUploadedDataVolume(1L).addOriginPLMN("20820")
                .addServedMSISDN("10000" + 6).addNodeID("GGSN" + 11).addRecordSequenceNumber(String.valueOf(6))
                .addIPBinV4Address("101.23.78." + 6).addDuration(6 + 100L).addRecordOpeningTime(new Date())
                .addAccessPointNameNI("iphone").build();
        
        CDR dataCDR = new DATACDRWrapper(cdr);
        byte[] magicNumber = dataCDR.getMagicNumber();
        
        CacheTransaction transaction1 = TransactionalMagicNumberCache.getInstance().getTransaction();
        CacheTransaction transaction2 = TransactionalMagicNumberCache.getInstance().getTransaction();
        
        transaction1.addToCache(magicNumber);
        Assert.assertFalse(transaction2.addToCache(magicNumber));
        
        transaction1.rollback();
        transaction2.rollback();
    }
    
    @Test(groups = { "unit" })
    public void testCacheTransactionCommit() {
        
        CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(100L).addUploadedDataVolume(1L).addOriginPLMN("20820")
                .addServedMSISDN("10000" + 6).addNodeID("GGSN" + 11).addRecordSequenceNumber(String.valueOf(6))
                .addIPBinV4Address("101.23.78." + 6).addDuration(6 + 100L).addRecordOpeningTime(new Date())
                .addAccessPointNameNI("iphone").build();
        CDR dataCDR = new DATACDRWrapper(cdr);
        byte[] magicNumber = dataCDR.getMagicNumber();
        
        CacheTransaction transaction1 = TransactionalMagicNumberCache.getInstance().getTransaction();
        
        transaction1.addToCache(magicNumber);
        Assert.assertFalse(TransactionalMagicNumberCache.getInstance().getCache().contains(magicNumber));
        transaction1.commit();
        Assert.assertTrue(TransactionalMagicNumberCache.getInstance().getCache().contains(magicNumber));
        
        CacheTransaction transaction2 = TransactionalMagicNumberCache.getInstance().getTransaction();
        Assert.assertFalse(transaction2.addToCache(magicNumber));
        transaction2.rollback();
    }

}
