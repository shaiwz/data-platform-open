/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护。
 *       未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       法务邮箱：761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.web;

import cn.hutool.extra.spring.SpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动类
 *
 * @author dingqianwen
 * @date 2025/1/2
 * @since 1.0.0
 */
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan({"cn.dataplatform.open.web.store.mapper"})
@SpringBootApplication(scanBasePackages = {"cn.dataplatform"},
        exclude = {
                ElasticsearchRestClientAutoConfiguration.class,
                FreeMarkerAutoConfiguration.class
        }
)
@Import({SpringUtil.class})
public class WebApp {

    public static void main(String[] args) {
        // 暂时还不完整，待整理完毕后可运行
        //if (true) {
        //    throw new UnsupportedOperationException("待整理完毕，暂不支持");
        //}
        SpringApplication.run(WebApp.class, args);
        System.out.println("""
                 __       ___          __            ___  ___  __   __       \s
                |  \\  /\\   |   /\\  __ |__) |     /\\   |  |__  /  \\ |__)  |\\/|\s
                |__/ /~~\\  |  /~~\\    |    |___ /~~\\  |  |    \\__/ |  \\  |  |\s
                """);
    }

}
