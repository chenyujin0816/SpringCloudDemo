package cn.edu.bit.service.impl;

import cn.edu.bit.entity.ContentCourse;
import cn.edu.bit.mapper.ContentCourseMapper;
import cn.edu.bit.service.ContentCourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ContentCourseServiceImpl extends ServiceImpl<ContentCourseMapper, ContentCourse> implements ContentCourseService {
}
