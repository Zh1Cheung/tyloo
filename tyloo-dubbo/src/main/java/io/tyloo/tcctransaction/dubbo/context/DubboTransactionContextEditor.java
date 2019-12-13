package io.tyloo.tcctransaction.dubbo.context;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import io.tyloo.api.TransactionContext;
import io.tyloo.api.TransactionContextEditor;
import io.tyloo.tcctransaction.dubbo.constants.TransactionContextConstants;

import java.lang.reflect.Method;

/*
 * Dubbo 事务上下文编辑器实现
 *
 * 如果底层服务框架使用的是dubbo，可以设置transactionContextEditor为DubboTransactionContextEditor.class，使用dubbo隐式传参方式），
 * 通过 Dubbo 的隐式传参的方式，避免在 Dubbo Service 接口上声明 TransactionContext 参数，对接口产生一定的入侵
 * tyloo 通过 Dubbo Proxy 的机制，实现 `@Compensable` 属性自动生成
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 15:32 2019/12/4
 *
 */
public class DubboTransactionContextEditor implements TransactionContextEditor {
    @Override
    public TransactionContext get(Object target, Method method, Object[] args) {

        String context = RpcContext.getContext().getAttachment(TransactionContextConstants.TRANSACTION_CONTEXT);

        if (StringUtils.isNotEmpty(context)) {
            return JSON.parseObject(context, TransactionContext.class);
        }

        return null;
    }

    @Override
    public void set(TransactionContext transactionContext, Object target, Method method, Object[] args) {

        RpcContext.getContext().setAttachment(TransactionContextConstants.TRANSACTION_CONTEXT, JSON.toJSONString(transactionContext));
    }
}
