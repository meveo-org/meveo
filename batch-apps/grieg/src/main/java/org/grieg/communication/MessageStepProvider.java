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
package org.grieg.communication;

import org.grieg.communication.ticket.MessageTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.validator.Validator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class MessageStepProvider implements Provider<AbstractProcessStep<MessageTicket>> {

    // TODO move ticket validation logic from MessageProcess step to dedicated
    // validator, and add validation step.
    @SuppressWarnings("unused")
    private final Validator<MessageTicket> validator;

    private final MeveoConfig meveoConfig;

    @Inject
    public MessageStepProvider(Validator<MessageTicket> validator, MeveoConfig meveoConfig) {
        this.validator = validator;
        this.meveoConfig = meveoConfig;
    }

    @Override
    public AbstractProcessStep<MessageTicket> get() {
        return new MessageProcess(null, meveoConfig);
    }

}
