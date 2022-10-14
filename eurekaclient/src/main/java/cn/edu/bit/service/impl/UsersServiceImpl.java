package cn.edu.bit.service.impl;

import cn.edu.bit.entity.Users;
import cn.edu.bit.mapper.UsersMapper;
import cn.edu.bit.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

}
