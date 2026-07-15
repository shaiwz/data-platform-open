package cn.dataplatform.open.common.component;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.RetryHandler;
import com.netflix.loadbalancer.*;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
@Slf4j
public class LoadBalancerTemplate {

    private final ILoadBalancer loadBalancer;

    /**
     * 在下一个服务器执行重试
     */
    private final RetryHandler retryHandler;

    /**
     * 构造负载均衡器
     *
     * @param serverList 服务器列表
     */
    public LoadBalancerTemplate(List<Server> serverList) {
        this(serverList, null);
    }

    /**
     * 构造负载均衡器
     *
     * @param serverList   服务器列表
     * @param retryHandler 重试处理器,当为null时，执行默认重试策略
     */
    public LoadBalancerTemplate(List<Server> serverList, RetryHandler retryHandler) {
        this.loadBalancer = LoadBalancerBuilder.newBuilder().buildFixedServerListLoadBalancer(serverList);
        this.retryHandler = Objects.requireNonNullElseGet(retryHandler, () ->
                {
                    // 在下一个服务器执行重试，最多执行3次，或者最小节点数
                    int retryNextServer = Math.min(serverList.size(), 3);
                    return new DefaultLoadBalancerRetryHandler(0, retryNextServer, true);
                }
        );
    }

    /**
     * 调用负载均衡器
     *
     * @param supplier s
     * @return 返回值
     */
    public <T> T call(Function<Server, T> supplier) {
        return LoadBalancerCommand.<T>builder()
                .withLoadBalancer(this.loadBalancer)
                .withRetryHandler(this.retryHandler)
                .build()
                .submit(server -> {
                    try {
                        return Observable.just(supplier.apply(server));
                    } catch (Exception e) {
                        log.warn("调用负载均衡器失败, 服务器: {}", server, e);
                        return Observable.error(e);
                    }
                }).toBlocking().first();
    }

    /**
     * 获取负载均衡器的统计信息
     *
     * @return r
     */
    public LoadBalancerStats getLoadBalancerStats() {
        if (this.loadBalancer instanceof BaseLoadBalancer baseLoadBalancer) {
            return baseLoadBalancer.getLoadBalancerStats();
        }
        throw new IllegalStateException("负载均衡器不是 BaseLoadBalancer 类型, 无法获取统计信息");
    }

    /**
     * 关闭
     */
    public void shutdown() {
        if (this.loadBalancer instanceof BaseLoadBalancer baseLoadBalancer) {
            baseLoadBalancer.shutdown();
        }
    }


    /**
     * 获取所有服务器
     *
     * @return 服务器列表
     */
    public List<Server> getAllServers() {
        return this.loadBalancer.getAllServers();
    }

}