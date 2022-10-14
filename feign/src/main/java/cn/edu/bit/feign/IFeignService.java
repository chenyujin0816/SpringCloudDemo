package cn.edu.bit.feign;

import java.util.Collection;

import cn.edu.bit.entity.Student;
import cn.edu.bit.feign.impl.FeignServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(value = "provider", fallback = FeignServiceImpl.class)
public interface IFeignService {

    @GetMapping("/student/findAll")
    public Collection<Student> findAll();

    @GetMapping("/student/index")
    public String index();
}