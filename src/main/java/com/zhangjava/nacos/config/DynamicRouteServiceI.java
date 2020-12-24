package com.zhangjava.nacos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 动态更新路由网关配置
 *
 * @author zhangxu
 * @date 2020/12/23 13:48
 */
@Slf4j
@Service
public class DynamicRouteServiceI implements ApplicationEventPublisherAware {
    @Autowired
    private RouteDefinitionRepository routeDefinitionRepository;
    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    /**
     * 增加路由
     *
     * @param definition
     * @return
     */
    public String add(RouteDefinition definition) {
        routeDefinitionRepository.save(Mono.just(definition)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    /**
     * 删除所有路由
     */
    public void deleteAll() {
        Flux<RouteDefinition> routeDefinitions = routeDefinitionRepository.getRouteDefinitions();
        Iterable<RouteDefinition> iterable = routeDefinitions.toIterable();
        for (RouteDefinition routeDefinition : iterable) {
            routeDefinitionRepository.delete(Mono.just(routeDefinition.getId())).subscribe();
        }
    }

}
