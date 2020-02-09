
package io.tyloo.tcctransaction.concurrent.threadlocal;

import io.tyloo.api.Context.TylooContext;


/*
 *
 * threadlocal÷–±£¥ÊTylooContext
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:20 2020/2/9
 *
 */

public final class TylooTransactionContextLocal {

    private static final ThreadLocal<TylooContext> CURRENT_LOCAL = new ThreadLocal<>();

    private static final TylooTransactionContextLocal TRANSACTION_CONTEXT_LOCAL = new TylooTransactionContextLocal();

    private TylooTransactionContextLocal() {

    }

    /**
     * singleton TransactionContextLocal.
     *
     * @return this
     */
    public static TylooTransactionContextLocal getInstance() {
        return TRANSACTION_CONTEXT_LOCAL;
    }

    /**
     * set value.
     *
     * @param context context
     */
    public void set(final TylooContext context) {
        CURRENT_LOCAL.set(context);
    }

    /**
     * get value.
     *
     * @return TccTransactionContext
     */
    public TylooContext get() {
        return CURRENT_LOCAL.get();
    }

    /**
     * clean threadLocal for gc.
     */
    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
