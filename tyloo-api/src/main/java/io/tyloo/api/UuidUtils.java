package io.tyloo.api;

import java.nio.ByteBuffer;
import java.util.UUID;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:06 2019/12/4
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

