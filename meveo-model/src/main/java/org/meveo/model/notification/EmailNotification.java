/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.model.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.admin.User;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Entity
@ModuleItem(value = "EmailNotification", path = "emailNotifications")
@ModuleItemOrder(203)
@Table(name="adm_notif_email")
public class EmailNotification extends Notification {
	
	private static final long serialVersionUID = -8948201462950547554L;

	@Column(name="email_from",length=1000)
	@Size(max=1000)
	private String emailFrom;
	
	@Column(name="email_to_el",length=2000)
	@Size(max=2000)
	private String emailToEl;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="adm_notif_email_list")
	private Set<String> emails = new HashSet<>();
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="adm_notif_email_user")
	private Set<User> users;
	
	@Column(name="email_subject",length=500,nullable=false)
	@NotNull
	@Size(max=500)
	private String subject;

	@Column(name="email_body", columnDefinition = "TEXT")
	private String body;

	@Column(name="email_html_body", columnDefinition = "TEXT")
	private String htmlBody;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="adm_notif_email_attach")
    private Set<String> attachmentExpressions = new HashSet<>();


	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getEmailToEl() {
		return emailToEl;
	}

	public void setEmailToEl(String emailToEl) {
		this.emailToEl = emailToEl;
	}

	public Set<String> getEmails() {
		return emails;
	}

	public void setEmails(Set<String> emails) {
		this.emails = emails;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public Set<String> getAttachmentExpressions() {
		return attachmentExpressions;
	}

	public void setAttachmentExpressions(Set<String> attachmentExpressions) {
		this.attachmentExpressions = attachmentExpressions;
    }

    @Override
    public String toString() {
        return String.format("EmailNotification [emailFrom=%s, emailToEl=%s, emails=%s,  subject=%s, attachmentExpressions=%s, notification=%s]", emailFrom, emailToEl,
            emails != null ? toString(emails) : null, subject, attachmentExpressions != null ? toString(attachmentExpressions) : null, super.toString());
    }

    private String toString(Collection<?> collection) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < 10; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }
}
