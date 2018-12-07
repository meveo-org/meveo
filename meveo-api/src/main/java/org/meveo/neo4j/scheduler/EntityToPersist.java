package org.meveo.neo4j.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public abstract class EntityToPersist {

    private String code;
    private Map<String, Object> values;
    private String name;

    public EntityToPersist(String code, String name, Map<String, Object> values) {
        this.code = code;
        this.values = Optional.ofNullable(values).orElseGet(HashMap::new);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityToPersist that = (EntityToPersist) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(values, that.values) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, values, name);
    }
}
