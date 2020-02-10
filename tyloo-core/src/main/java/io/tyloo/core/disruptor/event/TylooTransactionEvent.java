

package io.tyloo.core.disruptor.event;

import io.tyloo.api.common.TylooTransaction;
import lombok.Data;

import java.io.Serializable;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:16 2020/2/9
 *
 */
@Data
public class TylooTransactionEvent implements Serializable {

    private TylooTransaction tylooTransaction;

    private int type;

    /**
     * help gc.
     */
    public void clear() {
        tylooTransaction = null;
    }
}
