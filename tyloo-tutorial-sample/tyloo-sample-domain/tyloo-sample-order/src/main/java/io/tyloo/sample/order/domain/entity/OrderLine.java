package io.tyloo.sample.order.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/*
 *
 * 订单明细
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
public class OrderLine implements Serializable {

    private static final long serialVersionUID = 2300754647209250837L;
    private long id;

    private long productId;

    private int quantity;

    private BigDecimal unitPrice;

    public OrderLine() {

    }

    public OrderLine(Long productId, Integer quantity,BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public long getId() {
        return id;
    }
}
