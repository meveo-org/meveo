package org.meveo.cache;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.meveo.commons.utils.ParamBean;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.*;

/**
 * Provides cache related services (loading, update) for simple user message related operations
 * 
 * @author Farhan Munir
 * @lastModifiedVersion 5.0
 * 
 */
@Singleton
public class UserMessageCacheContainerProvider implements Serializable { // CacheContainerProvider, Serializable {

    private static final long serialVersionUID = -2866707126175429823L;

    @Inject
    protected Logger log;

    //@EJB
    //private ScriptNotificationService notificationService;

    private ParamBean paramBean = ParamBean.getInstance();

    private static boolean useUserMessageCache = true;

    private EmbeddedCacheManager cacheContainer;

    public static final String MEVEO_USER_MESSAGE_CACHE = "meveo-user-message";

    /**
     * TODO: username we are using for refering message ownership in session is assumed to be unique, need to double check
     * cacheKeyStr will just be a username.
     */
    @Resource(lookup = "java:jboss/infinispan/cache/meveo/meveo-user-message")
    private Cache<String, List<String>> userMessageCache;

    @Inject
    @CurrentUser
    protected MeveoUser currentUser;

    static {
        ParamBean tmpParamBean = ParamBean.getInstance();
        useUserMessageCache = Boolean.parseBoolean(tmpParamBean.getProperty("cache.cacheUserMessage", "true"));
    }

    @PostConstruct
    protected void init() {
        InitialContext initialContext = null;
        try {
            initialContext = new InitialContext();
            cacheContainer = (EmbeddedCacheManager) initialContext.lookup("java:jboss/infinispan/container/meveo");
        } catch (NamingException e) {
            throw new RuntimeException("Cannot instantiate cache container", e);
        }

        try {
            userMessageCache = cacheContainer.getCache(MEVEO_USER_MESSAGE_CACHE, true);
            try {
                // Check caches integrity
                userMessageCache.values();
            } catch (Exception e) {
                log.warn("Failed to populate User Message caches. They will be cleaned");
            }
        } catch(Exception e1) {
            log.error("Failed to populate user message caches.", e1);
        }
    }


    /**
     * Add user message to a cache.
     * 
     * @param message to add
     * @param userName key for the message
     */
    // @Lock(LockType.WRITE)
    public void addUserMessageToCache(String userName, String message) {

        if (!useUserMessageCache) {
            return;
        }
        log.trace("Adding message {} to user message cache under key {}", message, userName);
        try {
            Optional<List<String>> oldUserMessages = Optional.ofNullable(
                            userMessageCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(userName)
            );
            List<String> userMessages = new ArrayList<>();
            if (oldUserMessages.isPresent()) {
                userMessages.addAll(oldUserMessages.get());
            }
            userMessages.add(message);
            userMessageCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(userName, userMessages);

        } catch (Exception e) {
            log.error("Failed to add user Message {} to cache under key {}", message, userName);
        }
    }

    /**
     * get all user messages from cache.
     *
     * @param userName key for the message
     */
    // @Lock(LockType.WRITE)
    public Optional<List<String>> getAllUserMessagesFromCache(String userName) {
        if (!useUserMessageCache) {
            return null;
        }
        log.trace("getting all user messages under key {}", userName);
        return Optional.ofNullable(
                    userMessageCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(userName)
        );
    }

    /**
     * Remove user messages from cache.
     *
     * @param userName
     * @param messages List<String></String> to remove
     */
    public void removeUserMessagesFromCache(String userName, List<String> messages) {

        if (!useUserMessageCache) {
            return;
        }

        log.trace("Removing {} usermessages from userMessageCache under key {}", messages.size(), userName);
        List<String> userMessagesOld = userMessageCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(userName);

        if (userMessagesOld != null && !userMessagesOld.isEmpty()) {
            List<String> userMessages = new ArrayList<>(userMessagesOld);
            boolean removed = userMessages.removeAll(messages);
            if (removed) {
                // Remove cached value altogether if no value are left in the list
                if (userMessages.isEmpty()) {
                    userMessageCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(userName);
                } else {
                    userMessageCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(userName, userMessages);
                }
                log.trace("Removed {} usermessages from userMessageCache cache under key {}", messages.size(), userName);
            }
        }
    }

    /**
     * Update message in cache
     * 
     * @param message to update
     */
    public void updateMessageInCache(String userName, String message) {

        if (!useUserMessageCache) {
            return;
        }

        removeUserMessagesFromCache(userName, Arrays.asList(message));
        addUserMessageToCache(userName, message);
    }

    /**
     * Get a summary of cached information
     * 
     * @return A list of a map containing cache information with cache name as a key and cache as a value
     */
    // @Override
    @SuppressWarnings("rawtypes")
    public Map<String, Cache> getCaches() {
        Map<String, Cache> summaryOfCaches = new HashMap<String, Cache>();
        summaryOfCaches.put(userMessageCache.getName(), userMessageCache);

        return summaryOfCaches;
    }

    /**
     * Refresh cache by name. Removes current provider's data from cache and populates it again
     * 
     * @param cacheName Name of cache to refresh or null to refresh all caches
     */
    // @Override
    @Asynchronous
    public void refreshCache(String cacheName) {

        if (cacheName == null || cacheName.equals(userMessageCache.getName()) || cacheName.contains(userMessageCache.getName())) {
            userMessageCache.clear();
            userMessageCache = cacheContainer.getCache(MEVEO_USER_MESSAGE_CACHE, true);
        }
    }



}