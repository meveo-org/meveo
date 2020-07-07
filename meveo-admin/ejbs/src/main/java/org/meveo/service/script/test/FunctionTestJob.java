/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.service.script.test;


import java.io.IOException;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.service.job.Job;

/**
 * Job that executes a JMeter test suite
 * 
 * @author clement.bareth
 * @since 6.5.0
 * @version 6.10.0
 */
@Stateless
public class FunctionTestJob extends Job {

    @Inject
    private JMeterService jMeterService;

    @Override
    protected void execute(JobExecutionResultImpl result, JobInstance jobInstance, Map<String, Object> params) throws BusinessException {
        final String code = jobInstance.getParametres();

        try {
            final TestResult sampleResults = jMeterService.executeTest(code);
            sampleResults.getSampleResults().forEach(sampleResult -> registerResult(result, sampleResult));
            
            result.addReport(" Response data : " + sampleResults.getResponsData() + "\n");

        } catch (IOException e) {
            result.registerError(e.toString());
        }
        
    }

    private void registerResult(JobExecutionResultImpl result, SampleResult sampleResult) {
        if (sampleResult.isSuccess()) {
            result.registerSucces();
        } else if(sampleResult.getFailureMessage() != null && sampleResult.getFailureMessage().startsWith("[WARN]")) {
			result.registerWarning(sampleResult.getFailureMessage());
    	} else {
    		result.registerError(sampleResult.getName() + " : " + sampleResult.getFailureMessage());
    	}
    }

    @Override
    public JobCategoryEnum getJobCategory() {
        return JobCategoryEnum.TEST;
    }
}
