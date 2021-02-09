package net.ysq.webchat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author passerbyYSQ
 * @create 2021-02-09 17:55
 */
@Controller
@RequestMapping("index")
@ResponseBody
public class IndexController {

    @GetMapping("test1")
    public String testXss(String wd) {
//        System.out.println(wd);
        return wd;
    }

}
