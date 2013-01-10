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
package org.meveo.vertina.test.rating;


/**
 * @author Ignas Lelys
 * @created Jul 19, 2010
 *
 */
public class AbstractRatePlanTest {
    
//    @Test(groups = { "db" })
//    public void testIsTransactionAlreadyRated() {
//        AbstractRatePlan ratePlan = new AbstractRatePlanImpl();
//        // transaction in database
//        Assert.assertTrue(ratePlan.isTransactionAlreadyRated("1234567890"));
//        // no transaction with such magic number in database
//        Assert.assertFalse(ratePlan.isTransactionAlreadyRated("0987654321"));
//    }
//    
//    @Test(groups = { "db" })
//    public void testLoadRatedTransaction() {
//        AbstractRatePlan ratePlan = new AbstractRatePlanImpl();
//        // transaction in database
//        Transaction transaction = ratePlan.loadRatedTransaction("1234567890");
//        Assert.assertEquals((Long)transaction.getId(), (Long)1L);
//        Assert.assertEquals(transaction.getMagicNumber(), "1234567890");
//        Assert.assertEquals(transaction.getUsageAmount().toString(), "100");
//        Assert.assertEquals(transaction.getPrice().toString(), "255.5");
//    }
//    
//    @Test(groups = { "db" })
//    public void testPrerateWhenTransactionRated() {
//        AbstractRatePlan ratePlan = new AbstractRatePlanImpl();
//        VertinaTicketImpl ticket = new VertinaTicketImpl();
//        // transaction in database
//        ticket.setId("1234567890");
//        StepExecution<VertinaTicket> stepExecution = new StepExecution<VertinaTicket>(ticket, "name", new HashMap<String, Object>(), new TaskExecution<VertinaTicket>("name", new Date(), null));
//        Transaction transaction = ratePlan.prerate(stepExecution);
//        Assert.assertEquals((Long)transaction.getId(), (Long)1L);
//        Assert.assertEquals(transaction.getMagicNumber(), "1234567890");
//        Assert.assertEquals(transaction.getUsageAmount().toString(), "100");
//        Assert.assertEquals(transaction.getPrice().toString(), "255.5");
//    }
//    
//    @Test(groups = { "db" })
//    public void testPrerateWhenTransactionNotRated() {
//        AbstractRatePlan ratePlan = new AbstractRatePlanImpl();
//        VertinaTicketImpl ticket = new VertinaTicketImpl();
//        // transaction in database
//        ticket.setId("0987654321");
//        ticket.setUsageAmount(111L);
//        
//        Map<String, Object> contextParameters = new HashMap<String, Object>();
//        Subscription subscription = new Subscription();
////        subscription.setCustom1("custom1");
////        subscription.setCustom2("custom2");
////        subscription.setCustom3("custom3");
//        UsageType usageType = new UsageType();
//        usageType.setId(1L);
//        usageType.setCode("DATA");
//        contextParameters.put(VertinaConstants.SUBSCRIPTION_KEY, subscription);
//        contextParameters.put(VertinaConstants.USAGE_TYPE_KEY, usageType);
//        
//        StepExecution<VertinaTicket> stepExecution = new StepExecution<VertinaTicket>(ticket, "name", contextParameters, new TaskExecution<VertinaTicket>("name", new Date(), null));
//        
//        Transaction transaction = ratePlan.prerate(stepExecution);
//        Assert.assertEquals((Long)transaction.getId(), null);
//        Assert.assertEquals(transaction.getMagicNumber(), "0987654321");
//        Assert.assertEquals(transaction.getUsageAmount().toString(), "111");
//    }
//    
//    @Test(groups = { "db" })
//    public void testGetMatrix() {
//        AbstractRatePlan ratePlan = new AbstractRatePlanImpl();
//        Calendar time = Calendar.getInstance();
//        time.set(2010, Calendar.JANUARY, 2, 0, 0);
//        
//        MatrixDefinition dataMatrixDefinition = ratePlan.getMatrix("TEST_DATA", time.getTime());
//        Assert.assertEquals(dataMatrixDefinition.getName(), "TEST_DATA");
//        Assert.assertEquals(dataMatrixDefinition.getUsageType().getId(), (Long)1L);
//        Assert.assertEquals(dataMatrixDefinition.getDimension(), (Long)2L);
//        Assert.assertEquals(dataMatrixDefinition.getEntryType(), MatrixEntryType.NUMBER);
//        Assert.assertEquals(dataMatrixDefinition.getEntries().size(), 3);
//        
//        MatrixDefinition smsMatrixDefinition = ratePlan.getMatrix("TEST_SMS", time.getTime());
//        Assert.assertEquals(smsMatrixDefinition.getName(), "TEST_SMS");
//        Assert.assertEquals(smsMatrixDefinition.getUsageType().getId(), (Long)3L);
//        Assert.assertEquals(smsMatrixDefinition.getDimension(), (Long)3L);
//        Assert.assertEquals(smsMatrixDefinition.getEntryType(), MatrixEntryType.STRING);
//        Assert.assertEquals(smsMatrixDefinition.getEntries().size(), 2);
//    
//        // delete from database SMS matrix definition, but it still should be accessible because its in cache
//        // this way we test if caching works correctly
//        EntityManager em = MeveoPersistence.getEntityManager();
//        em.getTransaction().begin();
//        Query deleteEntriesQuery = em.createQuery("delete from MatrixEntry me where me.id = 201 or me.id = 202");
//        deleteEntriesQuery.executeUpdate();
//        Query deleteMatrixQuery = em.createQuery("delete from MatrixDefinition md where md.id = 200");
//        deleteMatrixQuery.executeUpdate();
//        em.getTransaction().commit();
//        
//        // still should be available
//        MatrixDefinition smsMatrixDefinitionFromCache = ratePlan.getMatrix("TEST_SMS", time.getTime());
//        Assert.assertEquals(smsMatrixDefinitionFromCache.getName(), "TEST_SMS");
//        Assert.assertEquals(smsMatrixDefinitionFromCache.getUsageType().getId(), (Long)3L);
//        Assert.assertEquals(smsMatrixDefinitionFromCache.getDimension(), (Long)3L);
//        Assert.assertEquals(smsMatrixDefinitionFromCache.getEntryType(), MatrixEntryType.STRING);
//        Assert.assertEquals(smsMatrixDefinitionFromCache.getEntries().size(), 2);
//
//    }
//    
//    /**
//     * Private rate plan class for testing purposes.
//     * 
//     * @author Ignas Lelys
//     * @created Jul 19, 2010
//     *
//     */
//    private class AbstractRatePlanImpl extends AbstractRatePlan {
//
//        @Override
//        public Transaction rate(Transaction transaction, StepExecution<VertinaTicket> stepExecution) {
//            throw new UnsupportedOperationException();
//        }
//        
//    }
//    
//    /**
//     * Private VertinaTicket implementation for testing purposes.
//     * 
//     * @author Ignas Lelys
//     * @created Jul 19, 2010
//     *
//     */
//    private class VertinaTicketImpl implements VertinaTicket {
//        
//        private String id;
//        private Long usageAmount;
//        
//        public void setId(String id) {
//            this.id = id;
//        }
//        @Override
//        public String getId() {
//            return id;
//        }
//        @Override
//        public String getAccessKey() {
//            return null;
//        }
//        @Override
//        public Object getSource() {
//            return null;
//        }
//        public void setUsageAmount(Long usageAmount) {
//            this.usageAmount = usageAmount;
//        }
//        @Override
//        public Long getUsageAmount() {
//            return usageAmount;
//        }
//        @Override
//        public String getUsageCode() {
//            return null;
//        }
//        @Override
//        public Date getUsageConsumptionStartDate() {
//            return null;
//        }
//    }

}
