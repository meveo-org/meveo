package org.meveo.model.storage;

import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.AuditableEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.15
 */
@Entity
@Table(name = "remote_repository", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "remote_repository_seq"), })
@ModuleItem(value = "RemoteRepository", path = "remoteRepositories")
@ModuleItemOrder(50)
@ExportIdentifier({ "code"})
public class RemoteRepository extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
