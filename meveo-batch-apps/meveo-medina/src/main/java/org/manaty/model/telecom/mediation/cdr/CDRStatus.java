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
package org.manaty.model.telecom.mediation.cdr;

/**
 * CDR status enum.
 * 
 * @author seb
 * @created Aug 6, 2012
 */
public enum CDRStatus {
    ONGOING,
    INVALID_FORMAT,
    DUPLICATE,
    UNKNOWN_ORIGIN_ACCESS,
    UNKNOWN_CDR_TYPE,
    UNAUTHORIZED_ACCESS,
    NO_ACCESS,
    IGNORED,
    AGGREGATED
}
