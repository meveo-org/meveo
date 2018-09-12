package org.meveo.model.quote;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BaseEntity;
import org.meveo.model.ExportIdentifier;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@ExportIdentifier({ "quote.code", "itemId" })
@Table(name = "ord_quote_item")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "ord_quote_item_seq"), })
public class QuoteItem extends BaseEntity {

    private static final long serialVersionUID = -6831399734977276174L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false, updatable = false)
    @NotNull
    private Quote quote;

    /**
     * Item id in the quote
     */
    @Column(name = "item_id", length = 10, nullable = false)
    @NotNull
    private String itemId;

    /**
     * Serialized quoteItem dto.
     */
    @Column(name = "source", nullable = false, columnDefinition = "TEXT")
    private String source;

    /**
     * Quote item processing status as defined by the workflow.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private QuoteStatusEnum status = QuoteStatusEnum.IN_PROGRESS;

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String oferItemSource) {
        this.source = oferItemSource;
    }

    public QuoteStatusEnum getStatus() {
        return status;
    }

    public void setStatus(QuoteStatusEnum status) {
        this.status = status;
    }


    /**
     * Interested in comparing quote items within the quote only
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof QuoteItem)) {
            return false;
        }

        QuoteItem other = (QuoteItem) obj;

        if (id != null && other.getId() != null && id.equals(other.getId())) {
            return true;
        }

        return StringUtils.compare(getItemId(), other.getItemId()) == 0;
    }
}