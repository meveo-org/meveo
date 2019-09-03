package org.meveo.model.order;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BaseEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.shared.Address;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@ExportIdentifier({ "order.code", "itemId" })
@Table(name = "ord_order_item")
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "ord_order_item_seq"), })
public class OrderItem extends BaseEntity {

    private static final long serialVersionUID = -6831399734977276174L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @NotNull
    private Order order;

    /**
     * Item id in the order
     */
    @Column(name = "item_id", length = 10, nullable = false)
    @NotNull
    private String itemId;

    /**
     * Action requested on a product or product offer
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 10, nullable = false)
    @NotNull
    private OrderItemActionEnum action;

    /**
     * Serialized orderItem dto.
     */
    @Column(name = "source", nullable = false, columnDefinition = "TEXT")
    private String source;

    /**
     * Order item processing status as defined by the workflow.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private OrderStatusEnum status = OrderStatusEnum.IN_CREATION;

    @Embedded
    private Address shippingAddress = new Address();

    @Transient
    private Object orderItemDto;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public OrderItemActionEnum getAction() {
        return action;
    }

    public void setAction(OrderItemActionEnum action) {
        this.action = action;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String orderItemSource) {
        this.source = orderItemSource;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    /**
     * @return the shippingAddress
     */
    public Address getShippingAddress() {
        return shippingAddress;
    }

    /**
     * @param shippingAddress the shippingAddress to set
     */
    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Object getOrderItemDto() {
        return orderItemDto;
    }

    public void setOrderItemDto(Object orderItemDto) {
        this.orderItemDto = orderItemDto;
    }

    /**
     * Interested in comparing order items within the order only
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof OrderItem)) {
            return false;
        }

        OrderItem other = (OrderItem) obj;

        if (id != null && other.getId() != null && id.equals(other.getId())) {
            return true;
        }

        return StringUtils.compare(getItemId(), other.getItemId()) == 0;
    }
}