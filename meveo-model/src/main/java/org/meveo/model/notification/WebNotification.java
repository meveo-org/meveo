package org.meveo.model.notification;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;

import javax.persistence.*;

/**
 * Notification used to send Server Sent Event to user connected to meveo webapps
 * the name, comment and data are sent as
 *   id: either timestamp or a UUID depending on the idStrategy
 *   event: <code>
 *   data: <data>
 *   : <description>
 *
 *   if the publicationAllowed flag is true the web clients can broadcast messages to the web notification channel
 *
 *   is the persistHistory flag is true then all messages are persisted
 */
@Entity
@ModuleItem("WebNotification")
@ModuleItemOrder(204)
@Table(name = "adm_notif_webnotification")
public class WebNotification  extends Notification {

    @Column(name="id_strategy")
    @Enumerated(EnumType.STRING)
    private WebNotificationIdStrategyEnum idStrategy = WebNotificationIdStrategyEnum.UUID;

    @Column(name="notif_data_el", columnDefinition = "TEXT")
    private String dataEL;

    @Column(name = "publication_allowed")
    @Type(type="numeric_boolean")
    @ColumnDefault("0")
    private boolean publicationAllowed;

    @Column(name = "persist_history")
    @Type(type="numeric_boolean")
    @ColumnDefault("0")
    private boolean persistHistory;


    @Override
    public String toString() {
        return String.format("WebNotification [name=%s, comment=%s, emails=%s,  dataEL=%s, idStrategy=%s," +
                        " publicationAllowed=%s, persistHistory=%s, notification=%s]",
                getCode(),getDescription(),getDataEL(),getIdStrategy(),
                isPublicationAllowed(),isPersistHistory(),super.toString());
    }

    public String getDataEL() {
        return dataEL;
    }

    public void setDataEL(String dataEL) {
        this.dataEL = dataEL;
    }


    public WebNotificationIdStrategyEnum getIdStrategy() {
        return idStrategy;
    }

    public void setIdStrategy(WebNotificationIdStrategyEnum idStrategy) {
        this.idStrategy = idStrategy;
    }

    public boolean isPublicationAllowed() {
        return publicationAllowed;
    }

    public void setPublicationAllowed(boolean publicationAllowed) {
        this.publicationAllowed = publicationAllowed;
    }


    public boolean isPersistHistory() {
        return persistHistory;
    }

    public void setPersistHistory(boolean persistHistory) {
        this.persistHistory = persistHistory;
    }
}