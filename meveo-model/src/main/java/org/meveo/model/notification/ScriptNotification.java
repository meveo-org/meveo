package org.meveo.model.notification;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Entity
@ModuleItem(value = "ScriptNotification", path = "scriptNotifications")
@ModuleItemOrder(200)
@Table(name = "adm_script_notification")
public class ScriptNotification extends Notification {

    private static final long serialVersionUID = -2527123286118840886L;    
}