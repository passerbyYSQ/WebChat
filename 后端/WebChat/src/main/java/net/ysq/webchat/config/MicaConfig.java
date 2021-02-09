package net.ysq.webchat.config;

import net.dreamlu.mica.xss.core.XssCleaner;
import net.ysq.webchat.component.MyXssCleaner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author passerbyYSQ
 * @create 2021-02-09 22:49
 */
@Configuration
public class MicaConfig {

    @Bean
    public XssCleaner xssCleaner() {
        return new MyXssCleaner();
    }

}
