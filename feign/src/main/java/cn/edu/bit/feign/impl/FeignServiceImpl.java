package cn.edu.bit.feign.impl;

import java.util.Collection;

import cn.edu.bit.entity.Student;
import cn.edu.bit.feign.IFeignService;
import org.springframework.stereotype.Component;


//错误熔断
@Component
public class FeignServiceImpl implements IFeignService {

    @Override
    public Collection<Student> findAll() {
        return null;
    }

    @Override
    public String index() {
        return "服务器维护中。。。";
    }

}