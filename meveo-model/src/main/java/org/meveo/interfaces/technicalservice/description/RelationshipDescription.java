package org.meveo.interfaces.technicalservice.description;

public interface RelationshipDescription extends TechnicalServiceDescription {

    /**
     * Source entity instance name of the relation
     */
    String getSource();

    /**
     * Target entity instance name of the relation
     */
    String getTarget();

}
