package io.tyloo.unittest.entity;

/*
 *
 * 账户状态
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:30 2019/12/5
 *
 */
public enum AccountStatus {
    /**
     * 常规状态：1
     **/
    NORMAL(1),
    /**
     * 转账中：2
     **/
    TRANSFERING(2);

    private int id;

    AccountStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
