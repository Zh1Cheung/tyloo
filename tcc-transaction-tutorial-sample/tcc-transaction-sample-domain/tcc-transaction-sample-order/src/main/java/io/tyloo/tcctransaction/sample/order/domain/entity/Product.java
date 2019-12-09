package io.tyloo.tcctransaction.sample.order.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/*
 *
 * 商品表
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
public class Product implements Serializable{
    private long productId;

    private long shopId;

    private String productName;

    private BigDecimal price;

    public Product() {
    }

    public Product(long productId, long shopId, String productName, BigDecimal price) {
        this.productId = productId;
        this.shopId = shopId;
        this.productName = productName;
        this.price = price;
    }

    public long getProductId() {
        return productId;
    }

    public long getShopId() {
        return shopId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
