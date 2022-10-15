package cn.edu.bit.service.impl;

import cn.edu.bit.entity.Group;
import cn.edu.bit.mapper.GroupMapper;
import cn.edu.bit.service.GroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
}
