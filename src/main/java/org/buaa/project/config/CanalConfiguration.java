package org.buaa.project.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CanalConfiguration {

    @Value("${canal.host}")
    private String host;

    @Value("${canal.port}")
    private int port;

    @Value("${canal.destination}")
    private String destination;

    @Bean
    public CanalConnector canalConnector() {
        return CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port), destination, "", "");
    }


}
