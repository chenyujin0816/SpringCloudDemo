package cn.edu.bit.service.impl;

import cn.edu.bit.entity.Resource;
import cn.edu.bit.mapper.ResourceMapper;
import cn.edu.bit.service.ResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

}
