package io.tyloo.sample.http.redpacket.api;

import java.math.BigDecimal;


public interface RedPacketAccountService {
    BigDecimal getRedPacketAccountByUserId(long userId);
}
