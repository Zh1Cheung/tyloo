package io.tyloo.tcctransaction.sample.capital.domain.entity;


import io.tyloo.tcctransaction.sample.exception.InsufficientBalanceException;

import java.math.BigDecimal;

/*
 *
 * 资金账户余额
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:28 2019/12/5
 *
 */
public class CapitalAccount {

    private long id;

    private long userId;

    private BigDecimal balanceAmount;

    private BigDecimal transferAmount = BigDecimal.ZERO;

    public long getUserId() {
        return userId;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * 减少账户金额（转出）预处理
     *
     * @param amount
     */
    public void transferFrom(BigDecimal amount) {
        //减少账户金额
        this.balanceAmount = this.balanceAmount.subtract(amount);

        if (BigDecimal.ZERO.compareTo(this.balanceAmount) > 0) {
            throw new InsufficientBalanceException();
        }
        //增加转移金额
        transferAmount = transferAmount.add(amount.negate());
    }

    /**
     * 增加账户金额（转入）预处理
     *
     * @param amount
     */
    public void transferTo(BigDecimal amount) {
        //增加账户金额
        this.balanceAmount = this.balanceAmount.add(amount);
        //增加转移金额
        transferAmount = transferAmount.add(amount);
    }

    /**
     * 取消减少账户金额（转出）的操作
     *
     * @param amount
     */
    public void cancelTransfer(BigDecimal amount) {
        //增加账户金额
        transferTo(amount);
    }
}
