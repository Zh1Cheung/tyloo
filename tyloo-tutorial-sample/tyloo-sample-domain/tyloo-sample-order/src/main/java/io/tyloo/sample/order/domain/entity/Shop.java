package io.tyloo.sample.order.domain.entity;

/*
 *
 * 商店表
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:30 2019/12/5
 *
 */
public class Shop {

    private long id;

    private long ownerUserId;

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public long getId() {
        return id;
    }
}
