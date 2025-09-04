package com.russia.dnevnik;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GatewayController {

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/gateway/routes")
    public Mono<List<Route>> getRoutes() {
        return routeLocator.getRoutes().collectList();
    }
}