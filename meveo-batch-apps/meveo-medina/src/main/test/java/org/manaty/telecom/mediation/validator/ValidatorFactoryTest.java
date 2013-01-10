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
package org.manaty.telecom.mediation.validator;

import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ValidatorFactory.
 * 
 * @author Donatas Remeika
 * @created Mar 18, 2009
 */
public class ValidatorFactoryTest {

    @Test(groups = { "unit" })
    public void testThatFactoryReturnsCorrectValidators() {
        Validator validator = ValidatorFactory.getValidator(CDRType.DATA);
        Assert.assertEquals(validator.getClass(), DATAValidator.class);

        validator = ValidatorFactory.getValidator(CDRType.SMS);
        Assert.assertEquals(validator.getClass(), SMSValidator.class);

        validator = ValidatorFactory.getValidator(CDRType.ROAMING_SMS_IN);
        Assert.assertEquals(validator.getClass(), SMSValidator.class);

        validator = ValidatorFactory.getValidator(CDRType.ROAMING_SMS_OUT);
        Assert.assertEquals(validator.getClass(), SMSValidator.class);

        validator = ValidatorFactory.getValidator(CDRType.ROAMING_DATA);
        Assert.assertEquals(validator.getClass(), DATAValidator.class);
    }

}
