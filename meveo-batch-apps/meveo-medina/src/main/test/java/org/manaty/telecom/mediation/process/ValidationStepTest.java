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
package org.manaty.telecom.mediation.process;

import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.telecom.mediation.context.MediationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ValidationStep.
 * 
 * @author Donatas Remeika
 * @created May 6, 2009
 */
public class ValidationStepTest {
	
    @Test(groups = { "unit" })
    public void testNoValidator() {
        CDR cdr = new BaseCDR.Builder().build();
        ValidationStep step = new ValidationStep(null);
        MediationContext context = new MediationContext(cdr, null, null);
        boolean success = step.execute(context);
        Assert.assertTrue(success);
    }
    
}
