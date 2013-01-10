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
package org.myevo.rating.guice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.ValidationStep;
import org.meveo.core.validator.Validator;
import org.myevo.rating.process.steps.LocatePriceStep;
import org.myevo.rating.process.steps.LocateSubscriptionStep;
import org.myevo.rating.process.steps.TransactionCreationStep;
import org.myevo.rating.ticket.EDRTicket;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class StepsProvider implements Provider<AbstractProcessStep<EDRTicket>> {

    private final Validator<EDRTicket> validator;
    private final MeveoConfig meveoConfig;
    
    private List<String> steps;
    private String configLocation = "steps.conf";

    @Inject
    public StepsProvider(Validator<EDRTicket> validator, MeveoConfig meveoConfig) {
        this.validator = validator;
        this.meveoConfig = meveoConfig;
    }

    public AbstractProcessStep<EDRTicket> get() {
    	AbstractProcessStep<EDRTicket> transactionCreationStep = new TransactionCreationStep(null, meveoConfig);
    	AbstractProcessStep<EDRTicket> locatePriceStep = new LocatePriceStep(transactionCreationStep, meveoConfig);
    	AbstractProcessStep<EDRTicket> locateSubscriptionStep = new LocateSubscriptionStep(locatePriceStep, meveoConfig);
    	ValidationStep<EDRTicket> validationStep = new ValidationStep<EDRTicket>(
    			(AbstractProcessStep<EDRTicket>) locateSubscriptionStep, meveoConfig, validator);
    	/*
    	Object currentStep = null;
    	
    	steps = new ArrayList<String>();
    	Object currentObject = null;
    	BufferedReader reader = null;
		try {		
			reader = new BufferedReader(new FileReader(getConfigLocation()));

			String currentLine = "";
			while ((currentLine = reader.readLine()) != null) {
				if(currentLine.trim().length() == 0) continue;
				
				Class clazz = Class.forName(currentLine);
				Constructor constructor = clazz.getConstructor(AbstractProcessStep.class, MeveoConfig.class);
				
				currentObject = constructor.newInstance(currentObject, meveoConfig);
				
				steps.add(currentObject.getClass().getSimpleName());
			}
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	ValidationStep<EDRTicket> validationStep = new ValidationStep<EDRTicket>(
    			(AbstractProcessStep<EDRTicket>) currentStep, meveoConfig, validator);
    	*/
    	return validationStep;
    }

	public List<String> getSteps() {
		return steps;
	}

	public void setSteps(List<String> steps) {
		this.steps = steps;
	}

	public String getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}
}