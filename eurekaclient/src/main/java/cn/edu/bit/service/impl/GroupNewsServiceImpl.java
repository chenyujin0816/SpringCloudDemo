package cn.edu.bit.service.impl;

import cn.edu.bit.entity.GroupNews;
import cn.edu.bit.mapper.GroupNewsMapper;
import cn.edu.bit.service.GroupNewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GroupNewsServiceImpl extends ServiceImpl<GroupNewsMapper, GroupNews> implements GroupNewsService {
}
