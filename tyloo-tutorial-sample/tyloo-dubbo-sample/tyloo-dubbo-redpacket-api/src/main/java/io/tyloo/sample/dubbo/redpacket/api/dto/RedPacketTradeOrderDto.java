package io.tyloo.sample.dubbo.redpacket.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:26 2019/12/5
 *
 */
public class RedPacketTradeOrderDto implements Serializable {

    private static final long serialVersionUID = 4747014387277477558L;

    private long selfUserId;

    private long oppositeUserId;

    private String orderTitle;

    private String merchantOrderNo;

    private BigDecimal amount;

    public long getSelfUserId() {
        return selfUserId;
    }

    public void setSelfUserId(long selfUserId) {
        this.selfUserId = selfUserId;
    }

    public long getOppositeUserId() {
        return oppositeUserId;
    }

    public void setOppositeUserId(long oppositeUserId) {
        this.oppositeUserId = oppositeUserId;
    }

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }

    public String getMerchantOrderNo() {
        return merchantOrderNo;
    }

    public void setMerchantOrderNo(String merchantOrderNo) {
        this.merchantOrderNo = merchantOrderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
