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
package org.manaty;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.SequenceRange;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.meveo.model.admin.Currency;
import org.meveo.model.admin.User;
import org.meveo.model.billing.BankCoordinates;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.BillingCycle;
import org.meveo.model.billing.InvoiceSubCategory;
import org.meveo.model.billing.ServiceInstance;
import org.meveo.model.billing.Wallet;
import org.meveo.model.catalog.RecurringChargeTemplate;
import org.meveo.model.crm.Customer;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.rating.UsageType;
import org.meveo.model.resource.OfferInstance;
import org.meveo.model.shared.Address;
import org.meveo.model.shared.Title;
import org.meveo.service.admin.local.CurrencyServiceLocal;
import org.meveo.service.billing.local.BillingAccountServiceLocal;
import org.meveo.service.billing.local.BillingCycleServiceLocal;
import org.meveo.service.billing.local.ServiceInstanceServiceLocal;
import org.meveo.service.billing.local.WalletServiceLocal;
import org.meveo.service.catalog.local.InvoiceSubCategoryServiceLocal;
import org.meveo.service.catalog.local.RecurringChargeTemplateServiceLocal;
import org.meveo.service.catalog.local.TitleServiceLocal;
import org.meveo.service.crm.local.CustomerServiceLocal;
import org.meveo.service.payments.local.CustomerAccountServiceLocal;
import org.meveo.service.rating.local.UsageTypeServiceLocal;
import org.meveo.service.resource.local.OfferInstanceServiceLocal;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;

/**
 * @author Gediminas Ubartas
 * @created 2010.07.12
 */
public abstract class BaseIntegrationTest extends DBUnitSeamTest {

    protected Context ctxRemote = null;

    @BeforeGroups(dependsOnGroups = { "remote" })
    public void initRemote() throws Exception {

        ctxRemote = BaseIntegrationTest.getInitialContext4EmbeddedServer();
    }

    @Override
    protected void editConfig(DatabaseConfig config) {
        super.editConfig(config);
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    }

    // Data required for data model wallk method (Filtering data in DataModel )
    public DataVisitor dataVisitor = new DataVisitor() {
        public void process(FacesContext context, Object rowKey, Object argument) throws IOException {
        }
    };

    public SequenceRange sequenceRange = new SequenceRange(0, 10);

    private Random random = new Random();

    protected Date getRandomDate() {
        return new Date(random.nextInt());
    }

    public BigDecimal getRandomBigDecimal() {
        return new BigDecimal(Math.random());
    }

    public boolean getRandomBoolean() {
        return random.nextBoolean();
    }

    public int getRandomInt() {
        return random.nextInt();
    }

    public long getRandomLong() {
        return random.nextLong();
    }

    public void login() {
        Credentials credentials = (Credentials) getInstance("org.jboss.seam.security.credentials");
        Identity identity = (Identity) getInstance("org.jboss.seam.security.identity");
        credentials.setUsername("MEVEO.ADMIN");
        credentials.setPassword("meveo.admin");
        identity.login();
        User user = (User) Contexts.getSessionContext().get("currentUser");
        Assert.assertNotNull(user, "Current user is null");       
        Assert.assertTrue(user.isOnlyOneProvider());
        Provider provider = (Provider) Contexts.getSessionContext().get("currentProvider");
        Assert.assertNotNull(provider, "Current provider is null");
    }

    // Loading entities from database
    public ServiceInstance loadServiceInstance(Long id) {
        ServiceInstanceServiceLocal serviceLocal = (ServiceInstanceServiceLocal) getInstance("serviceInstanceService");
        return serviceLocal.findById(id);
    }

    public BillingCycle loadBillingCycle(Long id) {
        BillingCycleServiceLocal serviceLocal = (BillingCycleServiceLocal) getInstance("billingCycleService");
        return serviceLocal.findById(id);
    }

    public Wallet loadWallet(Long id) {
        WalletServiceLocal serviceLocal = (WalletServiceLocal) getInstance("walletService");
        return serviceLocal.findById(id);
    }

    public UsageType loadUsageType(Long id) {
        UsageTypeServiceLocal serviceLocal = (UsageTypeServiceLocal) getInstance("usageTypeService");
        return serviceLocal.findById(id);
    }

    public OfferInstance loadOfferInstance(Long id) {
        OfferInstanceServiceLocal serviceLocal = (OfferInstanceServiceLocal) getInstance("offerInstanceService");
        return serviceLocal.findById(id);
    }

    public Currency loadCurrency(Long id) {
        CurrencyServiceLocal serviceLocal = (CurrencyServiceLocal) getInstance("currencyService");
        return serviceLocal.findById(id);
    }

    public Currency getSystemCurrency() {
        CurrencyServiceLocal serviceLocal = (CurrencyServiceLocal) getInstance("currencyService");
        return serviceLocal.getSystemCurrency();
    }

    public Customer loadCustomer(Long id) {
        CustomerServiceLocal serviceLocal = (CustomerServiceLocal) getInstance("customerService");
        return serviceLocal.findById(id);
    }
 
    public CustomerAccount loadCustomerAccount(Long id) {
        CustomerAccountServiceLocal serviceLocal = (CustomerAccountServiceLocal) getInstance("customerAccountService");
        return serviceLocal.findById(id);
    }
    
    public BillingAccount loadBillingAccount(Long id) {
        BillingAccountServiceLocal serviceLocal = (BillingAccountServiceLocal) getInstance("billingAccountService");
        return serviceLocal.findById(id);
    }

    public RecurringChargeTemplate loadRecurringChargeTemplate(Long id) {
        RecurringChargeTemplateServiceLocal serviceLocal = (RecurringChargeTemplateServiceLocal) getInstance("recurringChargeTemplateService");
        return serviceLocal.findById(id);
    }
    public InvoiceSubCategory loadInvoiceSubCategory(Long id) {
        InvoiceSubCategoryServiceLocal serviceLocal = (InvoiceSubCategoryServiceLocal) getInstance("invoiceSubCategoryService");
        return serviceLocal.findById(id);
    }

    public Title loadTitle(Long id) {
        TitleServiceLocal titleService = (TitleServiceLocal) getInstance("titleService");
        return titleService.findById(id);

    }

    public BankCoordinates getBankCoordinates() {
        BankCoordinates bankCoordinates = new BankCoordinates();
        bankCoordinates.setAccountNumber("accountNumber");
        bankCoordinates.setBankCode("bankCode");
        bankCoordinates.setBranchCode("branchCode");
        bankCoordinates.setIban("iban");
        bankCoordinates.setKey("key");
        return bankCoordinates;
    }

    public Address getAddress() {
        Address address = new Address();
        address.setAddress1("address1");
        address.setAddress2("address2");
        address.setAddress3("address3");
        address.setCity("city");
        address.setCountry("country");
        address.setState("state");
        address.setZipCode("zipCode");
        return address;
    }

    /** ************************************************** */
    /* Seam helpers */
    /** ************************************************** */
    protected EntityManager getEntityManager() {
        EntityManager em = (EntityManager) getInstance("entityManager");
        return em;
    }

    protected abstract class ComponentTransactionalTest extends ComponentTest {
        protected void testComponents() throws Exception {
            new TransactionalTemplate() {
                @Override
                protected void body() throws Exception {
                    testTransactionalComponents();
                }
            }.run();
        }

        protected abstract void testTransactionalComponents() throws Exception;
    }

    protected abstract class TransactionalTemplate {
        protected abstract void body() throws Exception;

        public void run() throws Exception {
            assert getUserTransaction() != null;

            boolean txActive = getUserTransaction().getStatus() == Status.STATUS_ACTIVE;

            try {
                if (!txActive)
                    getUserTransaction().begin();
                body();
                if (!txActive)
                    getUserTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace();
                if (!txActive)
                    try {
                        getUserTransaction().rollback();
                    } catch (Exception f) {
                        f.printStackTrace();
                    }
                throw e;
            }

        }
    }

    protected void printTransactionStatus(String label) {
        int status = 0;
        Log log = Logging.getLog(BaseIntegrationTest.class);
        try {
            status = getUserTransaction().getStatus();
        } catch (SystemException e) {
            log.error("Failed to get transaction status", e);
        } catch (NamingException e) {
            log.error("Failed to get transaction status", e);
        }
        String result = null;

        switch (status) {
        case javax.transaction.Status.STATUS_ACTIVE:
            result = "ACTIVE";
            break;
        case javax.transaction.Status.STATUS_COMMITTED:
            result = "COMMITED";
            break;
        case javax.transaction.Status.STATUS_COMMITTING:
            result = "COMMITTING";
            break;
        case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
            result = "MARKED_ROLLBACK";
            break;
        case javax.transaction.Status.STATUS_NO_TRANSACTION:
            result = "NO_TRANSACTION";
            break;
        case javax.transaction.Status.STATUS_PREPARED:
            result = "PREPARED";
            break;
        case javax.transaction.Status.STATUS_PREPARING:
            result = "PREPARING";
            break;
        case javax.transaction.Status.STATUS_ROLLEDBACK:
            result = "ROLLEDBACK";
            break;
        case javax.transaction.Status.STATUS_ROLLING_BACK:
            result = "ROLLING_BACK";
            break;
        case javax.transaction.Status.STATUS_UNKNOWN:
            result = "UNKNOWN";
            break;
        }
        log.debug("{0} TRANSACTION STATUS : {1}", label, result);
    }

    /**
     * Obtain local interface of an object in JNDI from embedded server
     * 
     * @param nameEJB
     *            Full JNDI path to an object
     * @return Object instance
     */
    protected Object getLocalInterfaceFromEmbededServer(String nameEJB) {
        InitialContext ctx;

        try {
            ctx = BaseIntegrationTest.getInitialContext4EmbeddedServer();
            return ctx.lookup(nameEJB);

        } catch (NamingException e) {
            Log log = Logging.getLog(BaseIntegrationTest.class);
            if (e instanceof NameNotFoundException) {
                log.error("Error getting local interface for {0} in embedded jboss configuration - name not found",
                        nameEJB);
            } else if (e instanceof NoInitialContextException) {
                log
                        .error(
                                "Error getting local interface for {0} in embedded jboss configuration - not running in embedded jboss mode",
                                nameEJB);
            } else {
                log.error("Error getting local interface for {0} in embedded jboss configuration", e, nameEJB);
            }
        }

        return null;
    }

    /**
     * Get JNDI context for embedded Jboss server
     * 
     * @param serverName
     * @throws javax.naming.NamingException
     * @return
     */
    public static InitialContext getInitialContext4EmbeddedServer() throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.JBossRemotingContextFactory");
        properties.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        return new InitialContext(properties);
    }
}