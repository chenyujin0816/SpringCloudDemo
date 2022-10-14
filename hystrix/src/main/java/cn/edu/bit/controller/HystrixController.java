package cn.edu.bit.controller;

import cn.edu.bit.entity.Student;
import cn.edu.bit.feign.IFeignService;
import cn.edu.bit.feign.impl.FeignServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/hystrix")
public class HystrixController {
    @Autowired
    private IFeignService feignService;

    @GetMapping("findAll")
    public Collection<Student> findAll(){
        return feignService.findAll();
    }

    @GetMapping("/index")
    public String index(){
        return feignService.index();
    }
}
