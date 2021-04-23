package net.ysq.webchat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author passerbyYSQ
 * @create 2021-02-09 17:55
 */
@Controller
@RequestMapping("index")
@ResponseBody
public class IndexController {

    @PostMapping("post")
    public String testPost(@RequestBody String wd) {
        System.out.println(wd);
        return wd;
    }

    @GetMapping("test1")
    public String testXss(String wd) {
//        System.out.println(wd);
        return wd;
    }

}
