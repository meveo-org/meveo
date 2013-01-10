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
package org.manaty;

import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;

/**
 * Basae Unit test methods.
 * 
 * @author Gediminas Ubartas
 * @created June 25, 2010
 * 
 */
public class BaseUnitTest {

    /**
     * Log class implementation
     * 
     * @author Gediminas Ubartas
     * @created June 22, 2010
     * 
     */
    public Log newLog() {

        Log log = new Log() {
            public void warn(Object object, Throwable t, Object... params) {
                // TODO Auto-generated method stub
            }

            public void warn(Object object, Object... params) {
                // TODO Auto-generated method stub
            }

            public void trace(Object object, Throwable t, Object... params) {
                // TODO Auto-generated method stub
            }

            public void trace(Object object, Object... params) {
                // TODO Auto-generated method stub

            }

            public boolean isWarnEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean isTraceEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean isInfoEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean isFatalEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean isErrorEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean isDebugEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public void info(Object object, Throwable t, Object... params) {
                // TODO Auto-generated method stub

            }

            public void info(Object object, Object... params) {
                // TODO Auto-generated method stub

            }

            public void fatal(Object object, Throwable t, Object... params) {
                // TODO Auto-generated method stub

            }

            public void fatal(Object object, Object... params) {
                // TODO Auto-generated method stub

            }

            public void error(Object object, Throwable t, Object... params) {
                // TODO Auto-generated method stub

            }

            public void error(Object object, Object... params) {
                // TODO Auto-generated method stub

            }

            public void debug(Object object, Throwable t, Object... params) {
                // TODO Auto-generated method stub

            }

            public void debug(Object object, Object... params) {
                // TODO Auto-generated method stub

            }
        };
        return log;
    }

    public StatusMessages newStatusMessages() {
        StatusMessages statusMessages = new StatusMessages() {
            private static final long serialVersionUID = 1L;
        };
        return statusMessages;
    }

}
