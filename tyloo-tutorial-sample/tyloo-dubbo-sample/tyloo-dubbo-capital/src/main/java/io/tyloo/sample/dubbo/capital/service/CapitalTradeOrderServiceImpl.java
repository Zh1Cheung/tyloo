package io.tyloo.sample.dubbo.capital.service;

import io.tyloo.api.Annotation.Tyloo;
import io.tyloo.sample.capital.domain.entity.CapitalAccount;
import io.tyloo.sample.capital.domain.entity.TradeOrder;
import io.tyloo.sample.capital.domain.repository.CapitalAccountRepository;
import io.tyloo.sample.capital.domain.repository.TradeOrderRepository;
import io.tyloo.sample.dubbo.capital.api.CapitalTradeOrderService;
import io.tyloo.sample.dubbo.capital.api.dto.CapitalTradeOrderDto;
import org.apache.commons.lang3.time.DateFormatUtils;

import io.tyloo.dubbo.context.DubboTransactionTransactionContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

/*
 *
 * 资金交易订单实现类
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 8:57 2019/12/5
 *
 */
@Service("capitalTradeOrderService")
public class CapitalTradeOrderServiceImpl implements CapitalTradeOrderService {

    @Autowired
    CapitalAccountRepository capitalAccountRepository;

    @Autowired
    TradeOrderRepository tradeOrderRepository;

    /**
     * 资金帐户交易订单记录
     *
     * @param tradeOrderDto
     * @return
     */
    @Override
    @Tyloo(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", tylooContextLoader = DubboTransactionTransactionContextLoader.class)
    @Transactional
    public String record(CapitalTradeOrderDto tradeOrderDto) {

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("capital try record called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));


        TradeOrder foundTradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());


        //检查是否记录了交易订单，如果是，直接返回成功；如果不是，新建交易订单，进行记录。
        if (foundTradeOrder == null) {

            TradeOrder tradeOrder = new TradeOrder(
                    tradeOrderDto.getSelfUserId(),
                    tradeOrderDto.getOppositeUserId(),
                    tradeOrderDto.getMerchantOrderNo(),
                    tradeOrderDto.getAmount()
            );

            try {
                tradeOrderRepository.insert(tradeOrder);
                //需要减少的账户金额（自己ID）
                CapitalAccount transferFromAccount = capitalAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());
                //减少账户金额预处理
                transferFromAccount.transferFrom(tradeOrderDto.getAmount());

                capitalAccountRepository.save(transferFromAccount);

            } catch (DataIntegrityViolationException e) {
                //当同时插入交易订单时可能发生此异常，如果发生，则忽略此插入操作。
            }
        }

        return "success";
    }


    @Transactional
    public void confirmRecord(CapitalTradeOrderDto tradeOrderDto) {
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("capital confirm record called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        //检查交易订单状态是否为 DRAFT ，如果是，直接返回，确保等幂性。
        if (tradeOrder != null && tradeOrder.getStatus().equals("DRAFT")) {
            tradeOrder.confirm();
            tradeOrderRepository.update(tradeOrder);
            //需要增加的账户金额（对方ID）
            CapitalAccount transferToAccount = capitalAccountRepository.findByUserId(tradeOrderDto.getOppositeUserId());
            //增加账户金额预处理
            transferToAccount.transferTo(tradeOrderDto.getAmount());

            capitalAccountRepository.save(transferToAccount);
        }
    }

    @Transactional
    public void cancelRecord(CapitalTradeOrderDto tradeOrderDto) {
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("capital cancel record called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        //检查交易订单状态是否为 DRAFT ，如果是，直接返回，确保等幂性。
        if (null != tradeOrder && "DRAFT".equals(tradeOrder.getStatus())) {
            tradeOrder.cancel();
            tradeOrderRepository.update(tradeOrder);
            //需要减少的账户金额（自己ID）
            CapitalAccount capitalAccount = capitalAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());
            //取消减少账户金额（转出）的操作
            capitalAccount.cancelTransfer(tradeOrderDto.getAmount());

            capitalAccountRepository.save(capitalAccount);
        }
    }
}
