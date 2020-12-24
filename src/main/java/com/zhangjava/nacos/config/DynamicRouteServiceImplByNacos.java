package com.zhangjava.nacos.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 监听nacos配置文件的变化
 *
 * @author zhangxu
 * @date 2020/12/23 11:48
 */
@Slf4j
@Component
public class DynamicRouteServiceImplByNacos implements ApplicationRunner {
    @Autowired
    private DynamicRouteServiceI dynamicRouteServiceI;

    private ConfigService configService;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String addr;
    @Value("${gateway.router.nacos.config.dataId}")
    private String dataId;
    @Value("${gateway.router.nacos.config.group}")
    private String group;

    @Override
    public void run(ApplicationArguments args) {
        log.info("============ 网关路由开始初始化 =============");
        try {
            configService = initConfigService();
            if (configService == null) {
                log.warn("网关路由初始化失败");
                return;
            }
            String configInfo = configService.getConfig(dataId, group, 30000);
            log.info("获取网关当前配置:\r\n{}", configInfo);
            List<RouteDefinition> definitionList = JSON.parseArray(configInfo, RouteDefinition.class);
            for (RouteDefinition definition : definitionList) {
                dynamicRouteServiceI.add(definition);
            }
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
        }
        dynamicRouteByNacosListener(dataId, group);
        log.info("============ 网关路由初始化完毕 =============");
    }

    /**
     * 监听Nacos下发的动态路由配置
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListener(String dataId, String group) {
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("进行网关更新:\n\r{}", configInfo);
                    // 先清空所有的配置
                    dynamicRouteServiceI.deleteAll();
                    List<RouteDefinition> definitionList = JSON.parseArray(configInfo, RouteDefinition.class);
                    for (RouteDefinition definition : definitionList) {
                        dynamicRouteServiceI.add(definition);
                    }
                }

            });
        } catch (NacosException e) {
            log.error("从nacos接收动态路由配置出错!!!", e);
        }
    }

    /**
     * 初始化网关路由 nacos config
     *
     * @return
     */
    private ConfigService initConfigService() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", addr);
            return configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
            return null;
        }
    }


}
