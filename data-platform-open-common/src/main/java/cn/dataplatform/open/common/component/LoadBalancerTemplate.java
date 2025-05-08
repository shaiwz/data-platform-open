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


@Slf4j
public class LoadBalancerTemplate {

    private final ILoadBalancer loadBalancer;

    /**
     * 在下一个服务器执行重试
     */
    private final RetryHandler retryHandler;

    public LoadBalancerTemplate(List<Server> serverList) {
        this(serverList, null);
    }

    /**
     * 构造负载均衡器
     *
     * @param serverList   服务器列表
     * @param retryHandler 重试处理器
     */
    public LoadBalancerTemplate(List<Server> serverList, RetryHandler retryHandler) {
        loadBalancer = LoadBalancerBuilder.newBuilder().buildFixedServerListLoadBalancer(serverList);
        this.retryHandler = Objects.requireNonNullElseGet(retryHandler, () ->
                new DefaultLoadBalancerRetryHandler(0, 1, true)
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
                .withLoadBalancer(loadBalancer)
                .withRetryHandler(retryHandler)
                .build()
                .submit(new ServerOperation<T>() {
                    @Override
                    public Observable<T> call(Server server) {
                        try {
                            return Observable.just(supplier.apply(server));
                        } catch (Exception e) {
                            log.warn("Failed to call server: " + server, e);
                            return Observable.error(e);
                        }
                    }
                }).toBlocking().first();
    }

    /**
     * 获取负载均衡器的统计信息
     *
     * @return r
     */
    public LoadBalancerStats getLoadBalancerStats() {
        return ((BaseLoadBalancer) loadBalancer).getLoadBalancerStats();
    }

}