package net.ysq.webchat.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;

/**
 * @author passerbyYSQ
 * @create 2021-02-22 17:20
 */
//@Component
public class MyWebBindingInitializer extends ConfigurableWebBindingInitializer {

    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder); // 不能少！！！
        // 注册自定义的类型转换器
        binder.registerCustomEditor(String.class, new EscapeStringEditor());
    }

}
