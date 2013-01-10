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
package org.manaty.utils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * SQL Utility methods.
 * 
 * @author Donatas Remeika
 * @created Mar 13, 2009
 */
public final class SQLUtil {

    /**
     * No need to instantiate.
     */
    private SQLUtil() {

    }

    private static final Logger logger = Logger.getLogger(SQLUtil.class.getName());

    /**
     * Get String value from ResultSet and close it.
     * 
     * @param rs
     *            ResultSet to get value from.
     * @return String value.
     * @throws SQLException
     */
    public static String getStringAndCloseResultSet(ResultSet rs) throws SQLException {
        try {
            if (rs.next()) {
                return rs.getString(1);
            }
        } finally {
            SQLUtil.closeResultSet(rs);
        }
        return null;
    }

    /**
     * Get Object array from ResultSet and close it.
     * 
     * @param rs
     *            ResultSet to get value from.
     * @return Object array.
     * @throws SQLException
     */
    public static Object[] getValuesAndCloseResultSet(ResultSet rs, @SuppressWarnings("rawtypes") Class... types) throws SQLException {
        try {
            if (rs.next()) {
                Object[] values = getResultObjectValues(rs, types);
                return values;
            }
        } finally {
            SQLUtil.closeResultSet(rs);
        }
        return null;
    }
    
    /**
     * Get List of Object arrays from ResultSet and close it.
     * 
     * @param rs
     *            ResultSet to get value from.
     * @return Object array.
     * @throws SQLException
     */
    public static List<Object[]> getListOfValuesAndCloseResultSet(ResultSet rs, @SuppressWarnings("rawtypes") Class... types) throws SQLException {
        List<Object[]> resultList = new ArrayList<Object[]>();
        try {
            while (rs.next()) {
                Object[] values = getResultObjectValues(rs, types);
                resultList.add(values);
            }
        } finally {
            SQLUtil.closeResultSet(rs);
        }
        return resultList;
    }
    
    /**
     * Get List of Object arrays from ResultSet and close it.
     * 
     * @param rs
     *            ResultSet to get value from.
     * @return Object array.
     * @throws SQLException
     */
    public static List<String> getListOfStringsAndCloseResultSet(ResultSet rs) throws SQLException {
        List<String> resultList = new ArrayList<String>();
        try {
            while (rs.next()) {
                Object[] values = getResultObjectValues(rs, String.class);
                resultList.add((String)values[0]);
            }
        } finally {
            SQLUtil.closeResultSet(rs);
        }
        return resultList;
    }
    
    /**
     * Helper method to bind values from result set to array.
     */
    @SuppressWarnings("rawtypes")
	private static Object[] getResultObjectValues(ResultSet rs, Class... types) throws SQLException {
        Object[] values = new Object[types.length];
        for (int i = 1; i <= types.length; i++) {
            Class type = types[i - 1];
            if (Long.class == type) {
                values[i - 1] = rs.getLong(i);
                if (rs.wasNull()) {
                    values[i - 1] = null;
                }
            } else if (BigDecimal.class == type) {
                values[i - 1] = rs.getBigDecimal(i);
                if (rs.wasNull()) {
                    values[i - 1] = null;
                }
            } else if (String.class == type) {
                values[i - 1] = rs.getString(i);
            } else if (Date.class == type) {
                values[i - 1] = rs.getDate(i);
            } else if (java.util.Date.class == type ) {
                values[i - 1] = rs.getDate(i);
            } else if (Timestamp.class == type) {
                values[i - 1] = rs.getTimestamp(i);
            } else if (Boolean.class == type ) {
                values[i - 1] = rs.getBoolean(i);
            } else {
                values[i - 1] = null;
            }
        }
        return values;
    }

    /**
     * Get Integer value from ResultSet and close it.
     * 
     * @param rs
     *            ResultSet to get value from.
     * @return Integer value.
     */
    public static Integer getIntegerAndCloseResultSet(ResultSet rs) {
        try {
            if (rs.next()) {
                int value = rs.getInt(1);
                return rs.wasNull() ? null : value;
            }
        } catch (SQLException e) {
            logger.severe("Could not get Integer from ResultSet"+ e.getMessage());
        } finally {
            SQLUtil.closeResultSet(rs);
        }
        return null;
    }

    /**
     * Close Statements.
     * 
     * @param statement
     *            Statements to close.
     */
    public static void closeStatements(Statement... statements) {
        for (Statement statement : statements) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.severe("Could not close Statement"+ e.getMessage());
                }
            }
        }
    }

    /**
     * Close ResultSet.
     * 
     * @param rs
     *            ResultSet to close.
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.severe("Could not close ResultSet"+ e.getMessage());
            }
        }
    }

    /**
     * Execute batches for PreparedStatements.
     * 
     * @param statements
     *            List of PreparedStatements.
     * @throws SQLException
     *             on database error.
     */
    public static void executeBatches(PreparedStatement... statements) throws SQLException {
        for (PreparedStatement statement : statements) {
            statement.executeBatch();
        }
    }
}
