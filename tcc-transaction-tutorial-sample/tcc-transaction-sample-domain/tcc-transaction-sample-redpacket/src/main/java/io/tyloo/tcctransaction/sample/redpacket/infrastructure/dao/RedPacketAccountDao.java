package io.tyloo.tcctransaction.sample.redpacket.infrastructure.dao;

import io.tyloo.tcctransaction.sample.redpacket.domain.entity.RedPacketAccount;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:31 2019/12/5
 *
 */
public interface RedPacketAccountDao {

    RedPacketAccount findByUserId(long userId);

    int update(RedPacketAccount redPacketAccount);
}
