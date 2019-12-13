package io.tyloo.api;

/*
 *
 * 事务状态枚举
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 12:05 2019/12/4
 *
 */
public enum TransactionStatus {
    /**
     * 尝试中:1.
     */
    TRYING(1),
    /**
     * 确认中:2.
     */
    CONFIRMING(2),

    /**
     * 取消中:3.
     */
    CANCELLING(3);

    private int id;

    TransactionStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TransactionStatus valueOf(int id) {

        switch (id) {
            case 1:
                return TRYING;
            case 2:
                return CONFIRMING;
            default:
                return CANCELLING;
        }
    }

}
