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

package org.meveo.api.function;

import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.jobs.TimerEntity;
import org.meveo.model.scripts.Function;
import org.meveo.service.job.JobExecutionService;
import org.meveo.service.job.JobInstanceService;
import org.meveo.service.job.TimerEntityService;
import org.meveo.service.script.ConcreteFunctionService;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.PathParam;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class FunctionApi {

    public static final String TEST_MODE = "test-mode";

    @Inject
    private JobInstanceService jobService;

    @Inject
    private JobExecutionService jobExecutionService;

    @Inject
    private ConcreteFunctionService concreteFunctionService;

    @Inject
    private TimerEntityService timerEntityService;

    public List<FunctionDto> list() {
        final List<Function> functions = concreteFunctionService.list();
        return functions.stream().map(e -> {
            final FunctionDto functionDto = new FunctionDto();
            functionDto.setCode(e.getCode());
            functionDto.setTestSuite(e.getTestSuite());
            functionDto.setInputs(e.getInputs());
            functionDto.setOutputs(e.getOutputs());
            return functionDto;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> execute(String code, Map<String, Object> inputs) throws BusinessException {
        return concreteFunctionService.getFunctionService(code).execute(code, inputs);
    }

    public String getTest(String code) {
        final Function function = concreteFunctionService.findByCode(code);
        return function.getTestSuite();
    }

    /**
     * Update test suite and schedule or re-schedule execution
     *
     * @param code Code of the function to update
     * @param file Test Suite content
     * @throws IOException if the file cannot be read
     */
    public void updateTest(String code, File file) throws BusinessException, IOException {
        final String testSuite = FileUtils.readFileToString(file, "UTF-8");
        final Function function = concreteFunctionService.findByCode(code);
        function.setTestSuite(testSuite);
        concreteFunctionService.update(function);

        final String testJobCode = getTestJobCode(code);
        JobInstance jobInstance = jobService.findByCode(testJobCode);

        // If job does not exists, create it, otherwise re-schedule it
        if (jobInstance == null) {
            jobInstance = new JobInstance();
            jobInstance.setJobCategoryEnum(JobCategoryEnum.TEST);
            jobInstance.setJobTemplate("FunctionTestJob");
            jobInstance.setCode(testJobCode);
            jobInstance.setParametres(code);

            TimerEntity timerEntity = timerEntityService.findByCode("Daily-midnight");
            jobInstance.setTimerEntity(timerEntity);
            jobService.create(jobInstance);
        }else{
            jobService.scheduleUnscheduleJob(jobInstance.getId());
        }

    }

    @Asynchronous
    public void startJob(@PathParam("code") String fnCode) throws BusinessException {
        JobInstance jobInstance = jobService.findByCode(getTestJobCode(fnCode));
        jobExecutionService.executeJob(jobInstance, null);
    }

    private static String getTestJobCode(String functionCode) {
        return "FunctionTestJob_" + functionCode;
    }


}
