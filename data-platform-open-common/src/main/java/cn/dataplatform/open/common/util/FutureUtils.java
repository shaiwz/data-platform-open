/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护，本软件受著作权法和国际版权条约保护，未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       举报渠道：
 *           - 法务邮箱：dingqw@shaiwz.com，761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.common.util;

import cn.hutool.core.collection.CollUtil;
import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 一句话功能简述
 *
 * @author dingqw
 * @date 2026/3/29
 * @since 1.0.0
 */
public class FutureUtils {


    /**
     * 等待Future完成，并正确处理异常
     *
     * @param future 待等待的Future对象
     */
    @SneakyThrows
    public static void awaitFuture(Future<?> future) {
        if (future == null) {
            return;
        }
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    /**
     * 取消Future
     *
     * @param futures Future列表
     */
    public static void cancelFutures(List<Future<?>> futures) {
        if (CollUtil.isEmpty(futures)) {
            return;
        }
        for (Future<?> future : futures) {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
        }
    }

}
