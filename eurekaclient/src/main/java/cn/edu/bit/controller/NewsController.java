package cn.edu.bit.controller;

import java.time.LocalDateTime;
import java.util.Collection;

import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.News;
import cn.edu.bit.entity.Student;
import cn.edu.bit.entity.Users;
import cn.edu.bit.repository.StudentRepository;
import cn.edu.bit.service.NewsService;
import cn.edu.bit.service.UsersService;
import cn.edu.bit.utils.JwtUtils;
import cn.edu.bit.utils.ShiroUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController //在Spring中@RestController的作用等同于@Controller + @ResponseBody。
@RequestMapping("/news")
public class NewsController {
    @Autowired
    UsersService userService;

    @Autowired
    NewsService newsService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/findAll")
    public Collection<Student> findAll(){
        return studentRepository.findAll();
    }

    @GetMapping("/allNotice")
    public Result notice(@RequestParam(defaultValue = "1") Integer currentPage) {
        Page page = new Page(currentPage,15);
        IPage pageData = newsService.page(page,new QueryWrapper<News>().orderByDesc("release_date"));
        return Result.succ(pageData);
    }

    @GetMapping("/news")
    public Result news(@RequestParam(defaultValue = "1") Integer currentPage) {
        Page page = new Page(currentPage,15);
        IPage pageData = newsService.page(page,new QueryWrapper<News>().eq("type","2").orderByDesc("release_date"));
        return Result.succ(pageData);
    }

    @GetMapping("/academic")
    public Result academic(@RequestParam(defaultValue = "1") Integer currentPage) {
        Page page = new Page(currentPage,15);
        IPage pageData = newsService.page(page,new QueryWrapper<News>().eq("type","3").orderByDesc("release_date"));
        return Result.succ(pageData);
    }

    @GetMapping("/announcement")
    public Result announcement(@RequestParam(defaultValue = "1") Integer currentPage) {
        Page page = new Page(currentPage,15);
        IPage pageData = newsService.page(page,new QueryWrapper<News>().eq("type","1").orderByDesc("release_date"));
        return Result.succ(pageData);
    }

    @GetMapping("/allNotice/{id}")
    public Result detail(@PathVariable(name = "id") Long id) {
        News news = newsService.getById(id);
        Assert.notNull(news, "该通告不存在或已被删除");
        return Result.succ(news);
    }

    //编辑通告
    @RequiresAuthentication
    @PostMapping("/edit")
    public Result newsEdit(@Validated @RequestBody News news) {

        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(!curUser.getType().equals("4"))
            return Result.fail("没有权限");
        News temp=null;
        if (news.getId()!=null){
            System.out.println("edit");
            temp=newsService.getOne(new QueryWrapper<News>().eq("id",news.getId()));
            if (temp==null)
                return Result.fail("通告不存在");
            BeanUtil.copyProperties(news,temp);
            newsService.update(temp,new QueryWrapper<News>().eq("id",temp.getId()));

        }else{
            System.out.println("new");
            temp=new News();
            BeanUtil.copyProperties(news,temp);
            temp.setReleaseDate(LocalDateTime.now());
            newsService.save(temp);
        }
        return Result.succ(null);
    }

    //删除通告
    @RequiresAuthentication
    @GetMapping("/delete")
    public Result newsDelete(@RequestParam Integer newsId) {
        if(!isAdmin())
            return Result.fail("没有权限");
        newsService.remove(new QueryWrapper<News>().eq("id",newsId));
        return Result.succ(null);
    }

    private boolean isAdmin(){
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        return curUser.getType().equals("4");
    }

}