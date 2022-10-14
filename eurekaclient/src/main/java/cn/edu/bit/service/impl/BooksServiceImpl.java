package cn.edu.bit.service.impl;

import cn.edu.bit.entity.Books;
import cn.edu.bit.mapper.BooksMapper;
import cn.edu.bit.service.BooksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class BooksServiceImpl extends ServiceImpl<BooksMapper, Books> implements BooksService {

}
