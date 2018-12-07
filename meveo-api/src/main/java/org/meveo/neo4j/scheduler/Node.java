package org.meveo.neo4j.scheduler;

import java.util.Map;

public class Node extends EntityToPersist {


    public Node(String code, String name, Map<String, Object> values) {
        super(code, name, values);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

}
