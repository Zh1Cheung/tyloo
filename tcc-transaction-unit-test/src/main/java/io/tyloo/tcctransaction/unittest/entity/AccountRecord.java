package io.tyloo.tcctransaction.unittest.entity;

/*
 *
 * 账户记录
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:29 2019/12/5
 *
 */
public class AccountRecord {
    /**
     * 账户ID
     **/
    private long accountId;
    /**
     * 余额
     **/
    private volatile int balanceAmount;
    /**
     * 状态ID,默认：1
     **/
    private volatile int statusId = AccountStatus.NORMAL.getId();

    public AccountRecord(long accountId, int balanceAmount) {
        this.accountId = accountId;
        this.balanceAmount = balanceAmount;
    }

    public long getAccountId() {
        return accountId;
    }

    public int getBalanceAmount() {
        return balanceAmount;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public void setBalanceAmount(int balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public int getStatusId() {
        return statusId;
    }
}
