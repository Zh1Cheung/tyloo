

package io.tyloo.api.Enums;

/*
 * 事务类型
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:28 2019/12/4
 *
 */
public enum TransactionType {

    /**
     * 主事务:1.
     */
    ROOT(1),

    /**
     * 分支事务:2.
     */
    BRANCH(2);

    int id;

    TransactionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TransactionType valueOf(int id) {
        switch (id) {
            case 1:
                return ROOT;
            case 2:
                return BRANCH;
            default:
                return null;
        }
    }

}
