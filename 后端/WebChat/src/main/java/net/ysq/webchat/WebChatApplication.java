package net.ysq.webchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("net.ysq.webchat.dao") // 注意不要导入导错包！是tk下的
@ComponentScan(basePackages = {"net.ysq.webchat", "org.n3r.idworker"}) // 注意有两个并列的顶级包，必须都要扫描到
public class WebChatApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(WebChatApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(WebChatApplication.class, args);
	}

}
