package io.tyloo.tcctransaction.unittest.entity;

/*
 *
 * 子账户
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:31 2019/12/5
 *
 */
public class SubAccount {
    /** 子账户ID **/
    private long id;
    /** 余额 **/
    private volatile int balanceAmount;
    /** 状态ID,默认：1 **/
    private volatile int status = AccountStatus.NORMAL.getId();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(int balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public SubAccount(long id, int balanceAmount) {
        this.id = id;
        this.balanceAmount = balanceAmount;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
