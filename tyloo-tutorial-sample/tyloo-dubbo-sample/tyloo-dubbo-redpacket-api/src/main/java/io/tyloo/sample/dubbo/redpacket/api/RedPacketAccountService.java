package io.tyloo.sample.dubbo.redpacket.api;

import java.math.BigDecimal;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 9:26 2019/12/5
 *
 */

public interface RedPacketAccountService {
    BigDecimal getRedPacketAccountByUserId(long userId);
}
