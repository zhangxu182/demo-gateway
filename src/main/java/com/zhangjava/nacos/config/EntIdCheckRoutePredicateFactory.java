package com.zhangjava.nacos.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 自定义谓词工厂类，过滤请求参数entId
 * <p>
 * 1、继承AbstractRoutePredicateFactory类
 * 2、重写apply方法
 * 3、apply方法的参数是自定义的配置类，可以在apply方法中直接获取使用配置参数。
 * 4、类的命名需要以RoutePredicateFactory结尾
 * 配置路由时，name名称为： EntIdCheck
 *
 * 路由配置使用：
 *      {
*         "filters": [],
 *         "id": "path1",
 *         "predicates": [{
 *             "args": {
 *                "entId": "1"
 *             },
 *             "name": "EntIdCheck"
 *         }],
 *         "uri": "lb://service-a1"
 *     }
 *
 * @author zhangxu
 * @date 2020/12/23 16:01
 */
@Component
public class EntIdCheckRoutePredicateFactory extends AbstractRoutePredicateFactory<EntIdCheckRoutePredicateFactory.Config> {

    public EntIdCheckRoutePredicateFactory() {
        super(Config.class);
    }


    @Override
    public Predicate<ServerWebExchange> apply(EntIdCheckRoutePredicateFactory.Config config) {
        return serverWebExchange -> {
            String entId = serverWebExchange.getRequest().getQueryParams().getFirst("entId");
            if (StringUtils.isBlank(entId)) {
                return false;
            }

            List<String> list = Arrays.asList(config.getEntId().split(","));

            //检查请求参数中的entId是否与配置的数据相同，如果相同则允许访问，否则不允许访问
            return list.contains(entId);
        };
    }

    @Validated
    public static class Config {
        private String entId;

        public String getEntId() {
            return entId;
        }

        public void setEntId(String entId) {
            this.entId = entId;
        }
    }

}
