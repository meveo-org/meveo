/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.connector;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 * Comparators that can be used in cypher
 * @author Clément Bareth
 */
public enum Comparator {

    GREATER_THAN("&gt;",">"),
    LOWER_THAN("&lt;","<"),
    EQUALS("=","="),
    LOWER_OR_EQUAL("&le:","<="),
    GREATER_OR_EQUAL("&ge;",">="),
    DIFFERENT("&lt;&gt","<>"),
    STARTS_WITH("STARTS WITH", "STARTS WITH"),
    ENDS_WITH("ENDS WITH", "ENDS WITH"),
    CONTAINS("CONTAINS", "CONTAINS");

    /**
     * Name used to deserialize enum
     */
    private String name;

    /**
     * Corresponding syntax in cypher
     */
    private String operator;

    Comparator(String name, String operator){
        this.name = name;
        this.operator = operator;
    }

    /**
     * Find the {@link Comparator} that has the provided name
     * @param name Name of the {@link Comparator}
     * @return The corresponding {@link Comparator}
     */
    @JsonCreator
    public static Comparator fromName(String name) {
        return Stream.of(Comparator.values())
                .filter(e -> e.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return The name used to deserialize enum
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The corresponding syntax in cypher
     */
    public String getOperator() {
        return this.operator;
    }

    /**
     * Apply the comparator on the left operand and compares it with right operand
     *
     * @param left Base value to compare
     * @param right Comparison value
     * @return <code>true</code> if the statement is true
     */
    public boolean compare(String left, String right) {
        switch (this) {
            case EQUALS:
                return left.equals(right);
            case CONTAINS:
                return left.contains(right);
            case DIFFERENT:
                return !left.equals(right);
            case ENDS_WITH:
                return left.endsWith(right);
            case STARTS_WITH:
                return left.startsWith(right);
            case LOWER_THAN:
                return Integer.parseInt(left) < Integer.parseInt(right);
            case GREATER_THAN:
                return Integer.parseInt(left) > Integer.parseInt(right);
            case LOWER_OR_EQUAL:
                return Integer.parseInt(left) <= Integer.parseInt(right);
            case GREATER_OR_EQUAL:
                return Integer.parseInt(left) >= Integer.parseInt(right);
            default:
                return false;
        }
    }
}
