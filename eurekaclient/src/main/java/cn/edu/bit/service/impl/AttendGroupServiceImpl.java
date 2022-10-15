package cn.edu.bit.service.impl;

import cn.edu.bit.entity.AttendGroup;
import cn.edu.bit.mapper.AttendGroupMapper;
import cn.edu.bit.service.AttendGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AttendGroupServiceImpl extends ServiceImpl<AttendGroupMapper, AttendGroup> implements AttendGroupService {
}
