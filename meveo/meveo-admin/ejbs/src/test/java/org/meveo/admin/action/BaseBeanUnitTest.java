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
//package org.meveo.admin.action;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.jboss.seam.international.StatusMessage;
//import org.jboss.seam.international.StatusMessages;
//import org.manaty.BaseUnitTest;
//import org.meveo.admin.action.billing.WalletBean;
//import org.meveo.admin.util.pagination.PaginationDataModel;
//import org.meveo.model.admin.User;
//import org.meveo.model.billing.Wallet;
//import org.meveo.service.billing.impl.WalletService;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
///**
// * Unit tests for {@link BaseBean}.
// * 
// * @author Ignas Lelys
// * @created May 27, 2010
// * 
// */
//public class BaseBeanUnitTest extends BaseUnitTest {
//
//    /**
//     * Tests entity initialization when there is no object id set as request
//     * parameter. That way it should just return new entity instance of the type
//     * with which BaseBean was extended.
//     */
//    @Test(groups = { "unit" })
//    public void testInitEntity() {
//        WalletBean extendedBaseBean = new WalletBean();
//        Object entity = extendedBaseBean.initEntity();
//        Assert.assertTrue(entity instanceof Wallet);
//        Assert.assertTrue(((Wallet) entity).getId() == null);
//    }
//
//    /**
//     * Tests initializing entity when there is object id request parameter
//     * injected. It should load Entity using Mock service.
//     * 
//     * @throws NoSuchFieldException
//     * @throws SecurityException
//     * @throws IllegalAccessException
//     * @throws IllegalArgumentException
//     */
//    @Test(groups = { "unit" })
//    public void testInitEntityWithObjectIdSet() throws SecurityException, NoSuchFieldException,
//            IllegalArgumentException, IllegalAccessException {
//        // access private fields through reflection
//        final Field objectIdField = BaseBean.class.getDeclaredField("objectId");
//        final Field walletServiceField = WalletBean.class.getDeclaredField("walletService");
//        objectIdField.setAccessible(true);
//        walletServiceField.setAccessible(true);
//
//        // set id = 1 and check return value of mock
//        BaseBean<Wallet> extendedBaseBean = new WalletBean();
//        objectIdField.set(extendedBaseBean, 1L);
//        walletServiceField.set(extendedBaseBean, new WalletServiceMock());
//        Object entity = extendedBaseBean.initEntity();
//        Assert.assertTrue(entity instanceof Wallet);
//        Assert.assertTrue(((Wallet) entity).getId().equals(1L));
//        Assert.assertTrue(((Wallet) entity).getName().equals("WalletId1"));
//
//        // set id = 1 and check return value of mock
//        objectIdField.set(extendedBaseBean, 2L);
//        entity = extendedBaseBean.initEntity();
//        Assert.assertTrue(entity instanceof Wallet);
//        Assert.assertTrue(((Wallet) entity).getId().equals(2L));
//        Assert.assertTrue(((Wallet) entity).getName().equals("WalletId2"));
//    }
//
//    @Test(groups = { "unit" })
//    public void testSaveOrUpdate() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
//            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        final Field objectIdField = BaseBean.class.getDeclaredField("objectId");
//        final Field walletServiceField = WalletBean.class.getDeclaredField("walletService");
//        final Field statusMessagesField = StatusMessages.class.getDeclaredField("messages");
//
//        objectIdField.setAccessible(true);
//        walletServiceField.setAccessible(true);
//        statusMessagesField.setAccessible(true);
//
//        BaseBean<Wallet> extendedBaseBean = new WalletBean();
//        extendedBaseBean.entities = new PaginationDataModel<Wallet>(new WalletService());
//        extendedBaseBean.statusMessages = newStatusMessages();
//        extendedBaseBean.log = newLog();
//
//        // set id = 1 - Update
//        objectIdField.set(extendedBaseBean, 1L);
//        walletServiceField.set(extendedBaseBean, new WalletServiceMock());
//        Object entity = extendedBaseBean.initEntity();
//        Assert.assertTrue(extendedBaseBean.saveOrUpdate((Wallet) entity).equals("wallets"));
//        Assert.assertTrue(getStatusMessage(extendedBaseBean).equals("update.successful"));
//
//        // set id = 0
//        objectIdField.set(extendedBaseBean, 0L);
//        walletServiceField.set(extendedBaseBean, new WalletServiceMock());
//        entity = extendedBaseBean.initEntity();
//        Assert.assertTrue(extendedBaseBean.saveOrUpdate((Wallet) entity).equals("wallets"));
//        Assert.assertTrue(getStatusMessage(extendedBaseBean).equals("save.successful"));
//        // TODO create new FacesContext and run saveOrUpdate method
//    }
//
//    /**
//     * Tests deleting one entity
//     * 
//     * @throws SecurityException
//     * @throws NoSuchFieldException
//     * @throws IllegalArgumentException
//     * @throws IllegalAccessException
//     * @throws NoSuchMethodException
//     * @throws InvocationTargetException
//     * 
//     */
//    @Test(groups = { "unit" })
//    public void testDeleteOneEntity() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
//            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        final Field objectIdField = BaseBean.class.getDeclaredField("objectId");
//        final Field walletServiceField = WalletBean.class.getDeclaredField("walletService");
//        final Field statusMessagesField = StatusMessages.class.getDeclaredField("messages");
//
//        objectIdField.setAccessible(true);
//        walletServiceField.setAccessible(true);
//        statusMessagesField.setAccessible(true);
//
//        BaseBean<Wallet> extendedBaseBean = new WalletBean();
//        extendedBaseBean.entities = new PaginationDataModel<Wallet>(new WalletService());
//        extendedBaseBean.statusMessages = newStatusMessages();
//        extendedBaseBean.log = newLog();
//
//        // set id = 1
//        objectIdField.set(extendedBaseBean, 1L);
//        walletServiceField.set(extendedBaseBean, new WalletServiceMock());
//        Object entity = extendedBaseBean.initEntity();
//        extendedBaseBean.delete(((Wallet) entity).getId());
//        Assert.assertTrue(getStatusMessage(extendedBaseBean).equals("delete.successful"));
//
//        // set id = 0 - non existing
//        objectIdField.set(extendedBaseBean, 3L);
//        walletServiceField.set(extendedBaseBean, new WalletServiceMock());
//        entity = extendedBaseBean.initEntity();
//        extendedBaseBean.delete(((Wallet) entity).getId());
//        Assert.assertTrue(getStatusMessage(extendedBaseBean).equals("error.delete.unexpected"));
//
//    }
//
//    /**
//     * Tests deleting list of entities
//     * 
//     * @throws SecurityException
//     * @throws NoSuchFieldException
//     * @throws IllegalArgumentException
//     * @throws IllegalAccessException
//     * @throws NoSuchMethodException
//     * @throws InvocationTargetException
//     * 
//     */
//    @Test(groups = { "unit" })
//    public void testDeleteMany() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
//            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        final Field objectIdField = BaseBean.class.getDeclaredField("objectId");
//        final Field walletServiceField = WalletBean.class.getDeclaredField("walletService");
//        final Field checkedField = BaseBean.class.getDeclaredField("checked");
//
//        objectIdField.setAccessible(true);
//        walletServiceField.setAccessible(true);
//
//        checkedField.setAccessible(true);
//
//        BaseBean<Wallet> extendedBaseBean = new WalletBean();
//        objectIdField.set(extendedBaseBean, 1L);
//        walletServiceField.set(extendedBaseBean, new WalletServiceMock());
//
//        extendedBaseBean.log = newLog();
//        extendedBaseBean.entities = new PaginationDataModel<Wallet>(new WalletService());
//        extendedBaseBean.statusMessages = newStatusMessages();
//
//        Map<Long, Boolean> checked = new HashMap<Long, Boolean>();
//
//        // List with existing entities
//        checked.put(1L, true);
//        checked.put(2L, true);
//
//        checkedField.set(extendedBaseBean, checked);
//        extendedBaseBean.deleteMany();
//        Assert.assertTrue(getStatusMessage(extendedBaseBean).equals("delete.entitities.successful"));
//
//        // List with one not existing entity
//        checked.clear();
//        checked.put(1L, true);
//        checked.put(0L, true);
//
//        checkedField.set(extendedBaseBean, checked);
//        extendedBaseBean.deleteMany();
//        Assert.assertTrue(getStatusMessage(extendedBaseBean).equals("error.delete.unexpected"));
//    }
//
//    /**
//     * Mocked WalletService with useful methods overridden.
//     * 
//     * @author Ignas Lelys
//     * @created May 26, 2010
//     * 
//     */
//    private class WalletServiceMock extends WalletService {
//
//        @Override
//        public Wallet findById(Long id, boolean refresh) {
//            Wallet wallet = new Wallet();
//            if (id == 1L) {
//                wallet.setId(1L);
//                wallet.setName("WalletId1");
//            } else if (id == 2L) {
//                wallet.setId(2L);
//                wallet.setName("WalletId2");
//            }
//            return wallet;
//        }
//
//        @Override
//        public Wallet findById(Long id, List<String> fetchFields, boolean refresh) {
//            return findById(id, refresh);
//        }
//
//        @Override
//        public void detach(Object entity) {
//            // do nothing
//        }
//
//        @Override
//        public void remove(Long id) {
//            Wallet wallet = findById(id);
//            if (wallet.getId() == null) {
//                throw new NullPointerException();
//
//            }
//        }
//
//        @Override
//        public void remove(Set<Long> ids) {
//            for (Long id : ids) {
//                Wallet wallet = findById(id);
//                if (wallet.getId() == null) {
//                    throw new NullPointerException();
//
//                }
//            }
//
//        }
//
//        @Override
//        public void remove(Wallet wallet) {
//            // do nothing
//        }
//
//        public void update(Wallet wallet, User updater) {
//            // do nothing
//        }
//
//        public void update(Wallet wallet) {
//            update(wallet, new User());
//        }
//
//        public void create(Wallet wallet) {
//            create(wallet, new User());
//        }
//
//        public void create(Wallet wallet, User creator) {
//            // do nothing
//        }
//    }
//
//    /**
//     * Returns message Severity from Entity messages
//     * 
//     * @throws NoSuchFieldException
//     * 
//     */
//    @SuppressWarnings("unchecked")
//    private String getStatusMessage(BaseBean<Wallet> extendedBaseBean) throws SecurityException, NoSuchMethodException,
//            IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
//        final Method getMessagesMethod = StatusMessages.class.getDeclaredMethod("getMessages");
//        final Field summaryTemplateField = StatusMessage.class.getDeclaredField("summaryTemplate");
//
//        summaryTemplateField.setAccessible(true);
//        getMessagesMethod.setAccessible(true);
//
//        List<StatusMessage> messageList = (List<StatusMessage>) getMessagesMethod
//                .invoke(extendedBaseBean.statusMessages);
//        Iterator iter = messageList.iterator();
//        StatusMessage message = null;
//        while (iter.hasNext())
//            message = (StatusMessage) iter.next();
//        return (String) summaryTemplateField.get(message);
//    }
//
//}
