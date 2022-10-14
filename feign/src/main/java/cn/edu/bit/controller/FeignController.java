package cn.edu.bit.controller;

import java.util.Collection;

import cn.edu.bit.entity.Student;
import cn.edu.bit.feign.IFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign")
public class FeignController {

    @Autowired
    private IFeignService feignService;

    @GetMapping("/findAll")
    public Collection<Student> findAll(){
        return feignService.findAll();
    }

    @GetMapping("/index")
    public String index() {
        return feignService.index();
    }

}