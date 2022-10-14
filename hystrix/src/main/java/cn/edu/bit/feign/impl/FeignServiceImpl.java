package cn.edu.bit.feign.impl;

import cn.edu.bit.entity.Student;
import cn.edu.bit.feign.IFeignService;
import org.springframework.stereotype.Component;

import java.util.Collection;


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