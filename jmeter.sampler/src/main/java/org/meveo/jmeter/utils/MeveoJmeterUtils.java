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

package org.meveo.jmeter.utils;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.jmeter.function.FunctionManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MeveoJmeterUtils {

    public static String getTestPlanName(){
        final GuiPackage instance = GuiPackage.getInstance();
        final String testPlanFile = instance.getTestPlanFile();
        return getTestPlanName(new File(testPlanFile));
    }

    public static String getTestPlanName(File tempFile){
        SearchByClass<TestPlan> testPlans = new SearchByClass<>(TestPlan.class);
        try {
            HashTree tree = SaveService.loadTree(tempFile);
            tree.traverse(testPlans);
            Collection testPlansRes = testPlans.getSearchResults();
            TestPlan testPlan = (TestPlan) testPlansRes.toArray()[0];
            return testPlan.getName();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public static Arguments getDefaultInputs(String code) {
        final List<Argument> collect = FunctionManager.getFunctions()
                .stream()
                .filter(functionDto -> functionDto.getCode().equals(code))
                .map(FunctionDto::getInputs)
                .flatMap(Collection::stream)
                .map(functionIO -> new Argument(functionIO.getName(), null))
                .collect(Collectors.toList());

        final Arguments arguments = new Arguments();
        arguments.setArguments(collect);
        return arguments;
    }

    public static Arguments getDefaultInputs() {
        return getDefaultInputs(MeveoJmeterUtils.getTestPlanName());
    }
}
