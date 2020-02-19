
package io.tyloo.core.concurrent.threadlocal;

import io.tyloo.api.Context.TylooTransactionContext;


/*
 *
 * threadlocal÷–±£¥ÊTylooContext
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:20 2020/2/9
 *
 */

public final class TylooTransactionContextLocal {

    private static final ThreadLocal<TylooTransactionContext> CURRENT_LOCAL = new ThreadLocal<>();

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
    public void set(final TylooTransactionContext context) {
        CURRENT_LOCAL.set(context);
    }

    /**
     * get value.
     *
     * @return TccTransactionContext
     */
    public TylooTransactionContext get() {
        return CURRENT_LOCAL.get();
    }

    /**
     * clean threadLocal for gc.
     */
    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
