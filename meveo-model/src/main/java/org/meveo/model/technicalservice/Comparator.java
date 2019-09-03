package org.meveo.model.technicalservice;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

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
    private String cypherOperator;

    Comparator(String name, String cypherOperator) {
        this.name = name;
        this.cypherOperator = cypherOperator;
    }

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
    public String getCypherOperator() {
        return this.cypherOperator;
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
