package org.meveo.api.notification;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.notification.InboundRequestDto;
import org.meveo.api.dto.notification.InboundRequestsDto;
import org.meveo.api.dto.notification.NotificationDto;
import org.meveo.api.dto.notification.NotificationHistoriesDto;
import org.meveo.api.dto.notification.NotificationHistoryDto;
import org.meveo.api.dto.notification.WebNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.NotificationResponsesDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.cache.NotificationCacheContainerProvider;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.catalog.CounterTemplate;
import org.meveo.model.notification.InboundRequest;
import org.meveo.model.notification.Notification;
import org.meveo.model.notification.NotificationHistory;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.catalog.impl.CounterTemplateService;
import org.meveo.service.notification.InboundRequestService;
import org.meveo.service.notification.NotificationHistoryService;
import org.meveo.service.notification.NotificationService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.util.NullAwareBeanUtilsBean;
import org.primefaces.model.SortOrder;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10.0
 **/
@Stateless
public abstract class NotificationApi<E extends Notification, D extends NotificationDto> extends BaseCrudApi<E, D> {

	@Inject
	private CounterTemplateService counterTemplateService;

	@Inject
	protected ScriptInstanceService scriptInstanceService;
	
	@Inject
	protected NotificationCacheContainerProvider notificationCache;

	@Inject
	private NotificationService notificationService;

	@Inject
	private NotificationHistoryService notificationHistoryService;

	@Inject
	private InboundRequestService inboundRequestService;

	private Class<E> entityClass;
	private Class<D> dtoClass;

	public NotificationApi(Class<E> entityClass, Class<D> dtoClass) {

		super(entityClass, dtoClass);
		this.entityClass = entityClass;
		this.dtoClass = dtoClass;
	}

	@Override
	public E fromDto(D postData) throws MeveoApiException {
		return fromDto(postData, null);
	}

	@Override
	public E fromDto(D postData, E entity) throws MeveoApiException {

		if (StringUtils.isBlank(postData.getCode())) {
			missingParameters.add("code");
		}
		if (StringUtils.isBlank(postData.getClassNameFilter())) {
			missingParameters.add("classNameFilter");
		}
		if (StringUtils.isBlank(postData.getEventTypeFilter())) {
			missingParameters.add("eventTypeFilter");
		}
		if (StringUtils.isBlank(postData.getScriptInstanceCode())) {
			missingParameters.add("scriptInstanceCode");
		}

		handleMissingParametersAndValidate(postData);

		// check class
		try {
			Class.forName(postData.getClassNameFilter());
		} catch (Exception e) {
			throw new InvalidParameterException("classNameFilter", postData.getClassNameFilter());
		}

		ScriptInstance scriptInstance = null;
		if (!StringUtils.isBlank(postData.getScriptInstanceCode())) {
			scriptInstance = scriptInstanceService.findByCode(postData.getScriptInstanceCode());
			if (scriptInstance == null) {
				throw new EntityDoesNotExistsException(ScriptInstance.class, postData.getScriptInstanceCode());
			}
		}

		CounterTemplate counterTemplate = null;
		if (!StringUtils.isBlank(postData.getCounterTemplate())) {
			counterTemplate = counterTemplateService.findByCode(postData.getCounterTemplate());
			if (counterTemplate == null) {
				throw new EntityDoesNotExistsException(CounterTemplate.class, postData.getCounterTemplate());
			}
		}

		if (entity == null) {
			try {
				entity = entityClass.newInstance();

			} catch (InstantiationException | IllegalAccessException e) {
				throw new MeveoApiException("Failed to instantiate notification class=" + entityClass.getSimpleName());
			}
		}

		entity.setFunction(scriptInstance);
		entity.setCounterTemplate(counterTemplate);

		try {
			BeanUtilsBean beanUtilsBean = new NullAwareBeanUtilsBean();
			beanUtilsBean.copyProperties(entity, postData);
			entity.setParams(postData.getScriptParams());

		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new MeveoApiException("Unable to copy dto to entity. Make sure that the properties match.");
		}

		if (postData.isActive() != null) {
			entity.setActive(postData.isActive());

		} else {
			entity.setActive(true);
		}

		return entity;
	}

	@Override
	public D toDto(E entity) throws MeveoApiException {

		try {
			D dto = dtoClass.newInstance();
			BeanUtilsBean beanUtilsBean = new NullAwareBeanUtilsBean();
			beanUtilsBean.copyProperties(dto, entity);
			dto.setScriptParams(entity.getParams());

			if (entity.getCounterTemplate() != null) {
				dto.setCounterTemplate(entity.getCounterTemplate().getCode());
			}

			if (entity.getFunction() != null) {
				dto.setScriptInstanceCode(entity.getFunction().getCode());
			}

			return dto;

		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new MeveoApiException("Failed to instantiate notification dto class=" + dtoClass.getSimpleName());
		}
	}

	public E create(D postData) throws MeveoApiException, BusinessException {

		E notif = fromDto(postData);

		if (getPersistenceService().findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(entityClass.getSimpleName(), postData.getCode());
		}

		getPersistenceService().create(notif);

		return notif;
	}

	public E update(D postData) throws MeveoApiException, BusinessException {

		E entity = getPersistenceService().findByCode(postData.getCode());
		
		if (entity == null) {
			throw new EntityDoesNotExistsException(entityClass.getSimpleName(), postData.getCode());
		}
		
		// Remove notification from cache before the update,
		// otherwise if the type of event has changed the old value will remain in the cache
		notificationCache.removeNotificationFromCache(entity);

		fromDto(postData, entity);

		getPersistenceService().update(entity);

		return entity;
	}

	@Override
	public E createOrUpdate(D postData) throws MeveoApiException, BusinessException {

		if (getPersistenceService().findByCode(postData.getCode()) != null) {
			return update(postData);

		} else {
			return create(postData);
		}
	}

	@Override
	public D find(String code) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {

		E entity = getPersistenceService().findByCode(code);
		if (entity == null) {
			throw new EntityDoesNotExistsException(entityClass, code);
		}
		return toDto(entity);
	}

	@Override
	public boolean exists(D dto) {
		return !Objects.isNull(notificationService.findByCode(dto.getCode()));
	}
	
	@Override
	public void remove(D dto) throws MeveoApiException, BusinessException {
		this.remove(dto.getCode());
	}

	public void remove(String notificationCode) throws BusinessException, MeveoApiException {
		E entity = getPersistenceService().findByCode(notificationCode);
		if (entity != null) {
			getPersistenceService().remove(entity);
		}
	}

	public List<D> listByPage(NotificationResponsesDto<D> result, PagingAndFiltering pagingAndFiltering)
			throws MeveoApiException {

		if (pagingAndFiltering == null) {
			pagingAndFiltering = new PagingAndFiltering();
		}

		PaginationConfiguration paginationConfig = toPaginationConfiguration("id", SortOrder.ASCENDING, null,
				pagingAndFiltering, WebNotificationDto.class);

		long totalCount = getPersistenceService().count(paginationConfig);

		result.setPaging(pagingAndFiltering);
		result.getPaging().setTotalNumberOfRecords((int) totalCount);

		List<D> webNotificationDtos = new ArrayList<>();
		if (totalCount > 0) {
			List<E> webNotifications = getPersistenceService().list(paginationConfig);
			if (webNotifications != null) {
				for (E entity : webNotifications) {
					webNotificationDtos.add(toDto(entity));
				}
			}
		}

		return webNotificationDtos;
	}

	public NotificationHistoriesDto listNotificationHistory() {

		NotificationHistoriesDto result = new NotificationHistoriesDto();

		List<NotificationHistory> notificationHistories = notificationHistoryService.list();
		if (notificationHistories != null) {
			result.setNotificationHistory(notificationHistories.stream().map(e -> new NotificationHistoryDto(e))
					.collect(Collectors.toList()));
		}

		return result;
	}

	public InboundRequestsDto listInboundRequest() {

		InboundRequestsDto result = new InboundRequestsDto();

		List<InboundRequest> inboundRequests = inboundRequestService.list();
		if (inboundRequests != null) {
			result.setInboundRequest(
					inboundRequests.stream().map(e -> new InboundRequestDto(e)).collect(Collectors.toList()));
		}

		return result;
	}

}
