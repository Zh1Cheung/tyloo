package io.tyloo.sample.redpacket.infrastructure.dao;

import io.tyloo.sample.redpacket.domain.entity.RedPacketAccount;


public interface RedPacketAccountDao {

    RedPacketAccount findByUserId(long userId);

    int update(RedPacketAccount redPacketAccount);
}
