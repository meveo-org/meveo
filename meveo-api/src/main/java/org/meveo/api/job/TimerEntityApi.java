package org.meveo.api.job;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.job.TimerEntityDto;
import org.meveo.api.dto.sql.SqlConfigurationDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.jobs.TimerEntity;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.job.TimerEntityService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Stateless
public class TimerEntityApi extends BaseCrudApi<TimerEntity, TimerEntityDto> {

    @Inject
    private TimerEntityService timerEntityService;
    
    public TimerEntityApi() {
    	super(TimerEntity.class, TimerEntityDto.class);
    }

    public TimerEntity create(TimerEntityDto timerEntityDto) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(timerEntityDto.getCode()) || StringUtils.isBlank(timerEntityDto.getHour()) || StringUtils.isBlank(timerEntityDto.getMinute())
                || StringUtils.isBlank(timerEntityDto.getSecond()) || StringUtils.isBlank(timerEntityDto.getYear()) || StringUtils.isBlank(timerEntityDto.getMonth())
                || StringUtils.isBlank(timerEntityDto.getDayOfMonth()) || StringUtils.isBlank(timerEntityDto.getDayOfWeek())) {

            if (StringUtils.isBlank(timerEntityDto.getHour())) {
                missingParameters.add("hour");
            }
            if (StringUtils.isBlank(timerEntityDto.getMinute())) {
                missingParameters.add("minute");
            }
            if (StringUtils.isBlank(timerEntityDto.getSecond())) {
                missingParameters.add("second");
            }
            if (StringUtils.isBlank(timerEntityDto.getYear())) {
                missingParameters.add("year");
            }
            if (StringUtils.isBlank(timerEntityDto.getMonth())) {
                missingParameters.add("month");
            }
            if (StringUtils.isBlank(timerEntityDto.getDayOfMonth())) {
                missingParameters.add("dayOfMonth");
            }
            if (StringUtils.isBlank(timerEntityDto.getDayOfWeek())) {
                missingParameters.add("dayOfWeek");
            }

            handleMissingParameters();

        }

        

        if (timerEntityService.findByCode(timerEntityDto.getCode()) != null) {
            throw new EntityAlreadyExistsException(TimerEntity.class, timerEntityDto.getCode());
        }

        TimerEntity timerEntity = TimerEntityDto.fromDTO(timerEntityDto, null);

        timerEntityService.create(timerEntity);

        return timerEntity;
    }

    public TimerEntity update(TimerEntityDto timerEntityDto) throws MeveoApiException, BusinessException {

        String timerEntityCode = timerEntityDto.getCode();
        

        if (StringUtils.isBlank(timerEntityCode)) {
            missingParameters.add("Code");
            handleMissingParameters();
        }

        TimerEntity timerEntity = timerEntityService.findByCode(timerEntityCode);
        if (timerEntity == null) {
            throw new EntityDoesNotExistsException(TimerEntity.class, timerEntityCode);
        }

        timerEntity = TimerEntityDto.fromDTO(timerEntityDto, timerEntity);

        timerEntity = timerEntityService.update(timerEntity);

        return timerEntity;
    }

    @Override
    public TimerEntity createOrUpdate(TimerEntityDto timerEntityDto) throws MeveoApiException, BusinessException {

        if (timerEntityService.findByCode(timerEntityDto.getCode()) == null) {
            return create(timerEntityDto);
        } else {
            return update(timerEntityDto);
        }
    }

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public TimerEntityDto find(String timerEntityCode) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException {
        TimerEntityDto result = new TimerEntityDto();
        if (StringUtils.isBlank(timerEntityCode)) {
            missingParameters.add("code");
            handleMissingParameters();
        }
        TimerEntity timerEntity = timerEntityService.findByCode(timerEntityCode);
        if (timerEntity == null) {
            throw new EntityDoesNotExistsException(timerEntityCode.getClass(), timerEntityCode);
        }
        result = new TimerEntityDto(timerEntity);

        return result;
    }
    
    public void remove(String timerEntityCode) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(timerEntityCode)) {
            missingParameters.add("code");
            handleMissingParameters();
        }
        TimerEntity timerEntity = timerEntityService.findByCode(timerEntityCode);
        if (timerEntity == null) {
            throw new EntityDoesNotExistsException(timerEntityCode.getClass(), timerEntityCode);
        }

        timerEntityService.remove(timerEntity);
    }

	@Override
	public TimerEntityDto toDto(TimerEntity entity) {
		return new TimerEntityDto(entity);
	}

	@Override
	public TimerEntity fromDto(TimerEntityDto dto) throws MeveoApiException {
		return TimerEntityDto.fromDTO(dto, null);
	}

	@Override
	public IPersistenceService<TimerEntity> getPersistenceService() {
		return timerEntityService;
	}

	@Override
	public boolean exists(TimerEntityDto dto) {
		var entity = timerEntityService.findByCode(dto.getCode());
		return entity != null;
	}
	
	@Override
	public void remove(TimerEntityDto dto) throws MeveoApiException, BusinessException {
		var entity = timerEntityService.findByCode(dto.getCode());
		if(entity != null) {
			timerEntityService.remove(entity);
		}
	}

	public List<TimerEntityDto> list(){
        return timerEntityService.list()
            .stream()
            .map(TimerEntityDto::new)
            .collect(Collectors.toList());
    }
}