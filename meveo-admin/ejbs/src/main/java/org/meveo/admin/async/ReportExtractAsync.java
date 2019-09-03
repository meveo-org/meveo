package org.meveo.admin.async;

import org.meveo.admin.job.UnitReportExtractJobBean;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.security.MeveoUser;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.service.job.JobExecutionService;

import javax.ejb.*;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Edward P. Legaspi
 * @version %I%, %G%
 * @since 5.0
 * @lastModifiedVersion 5.0
 **/
@Stateless
public class ReportExtractAsync {

    @Inject
    private UnitReportExtractJobBean unitReportExtractJobBean;

    @Inject
    private JobExecutionService jobExecutionService;
    
    @Inject
    private CurrentUserProvider currentUserProvider;

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Future<String> launchAndForget(List<Long> ids, JobExecutionResultImpl result, Date startDate, Date endDate, MeveoUser lastCurrentUser) {
        
        currentUserProvider.reestablishAuthentication(lastCurrentUser);
        
        for (Long id : ids) {
            if (!jobExecutionService.isJobRunningOnThis(result.getJobInstance())) {
                break;
            }
            unitReportExtractJobBean.execute(result, id, startDate, endDate);
        }
        return new AsyncResult<>("OK");
    }

}
