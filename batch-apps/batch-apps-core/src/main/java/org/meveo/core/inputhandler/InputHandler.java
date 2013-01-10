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
package org.meveo.core.inputhandler;

import org.meveo.core.inputloader.Input;

/**
 * @author Ignas Lelys
 * @created Apr 20, 2010
 * 
 */
public interface InputHandler<T> {

	/**
	 * Handles input.
	 * 
	 * @param input Input to handle. Can be file, jms message, entity from database etc.
	 * 
	 * @return Task execution. (T type is a ticket class).
	 * @throws Exception
	 */
	public TaskExecution<T> handleInput(Input input) throws Exception;

}
