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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.manaty.telecom.mediation.ConfigurationException;

/**
 * MD5 implementation of {@link MagicNumberCalculator}.
 * 
 * @author Ignas
 * @created Apr 20, 2009
 */
public class MD5MagicNumberCalculator extends MagicNumberCalculator {

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Calculates CDR identifier from provided fields using MD5 algorithm.
     */
    public byte[] calculate(byte[]... fields) {
        // when using md5 join fields should be
        // overrided to add separators between fields.
        byte[] data = joinFields(fields);
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] md5 = digest.digest(data);
            return md5;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not initialize MD5", e);
            throw new ConfigurationException("Could not initialize MD5", e);
        }
    }

    @Override
    public int getMagicNumberLenght() {
        return 16;
    }

}
