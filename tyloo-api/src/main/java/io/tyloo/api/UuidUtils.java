package io.tyloo.api;

import java.nio.ByteBuffer;

import cn.hutool.core.lang.UUID;

/*
 *
 * @Author:Zh1Cheung zh1cheunglq@gmail.com
 * @Date: 15:06 2019/4/4
 *
 */

public class UuidUtils {

    public static byte[] uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID byteArrayToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}

