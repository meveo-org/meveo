package org.meveo.model.notification;

public enum WebNotificationIdStrategyEnum {
    UUID,
    TIMESTAMP;
    public String getLabel() {
        return this.getClass().getSimpleName() + "." + this.name();
    }
}
