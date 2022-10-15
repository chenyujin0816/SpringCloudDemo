package cn.edu.bit.controller;

import cn.edu.bit.common.dto.EditNewsDto;
import cn.edu.bit.common.dto.ReleaseNewsDto;
import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.AttendCourse;
import cn.edu.bit.entity.Course;
import cn.edu.bit.entity.News;
import cn.edu.bit.entity.User;
import cn.edu.bit.service.*;
import cn.edu.bit.utils.ShiroUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Autowired
    NewsService newsService;
    @Autowired
    AttendGroupService attendGroupService;
    @Autowired
    GroupNewsService groupNewsService;
    @Autowired
    GroupService groupService;
    @Autowired
    CourseService courseService;
    @Autowired
    UserService userService;
    @Autowired
    AttendCourseService attendCourseService;

    //获取新闻列表
    @RequiresAuthentication
    @GetMapping("/getAllNews")
    public Result getNews(){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        class NewsRes{
            Long id;//ID
            String title;//标题
            String content;//内容
            Long creatorId;//发布人ID
            Timestamp createTime;//发布时间
            String creatorName;

            public Long getId() {
                return id;
            }

            public NewsRes setId(Long id) {
                this.id = id;
                return this;
            }

            public NewsRes(Long id, String title, String content, Long creatorId, Timestamp createTime, String creatorName) {
                this.id = id;
                this.title = title;
                this.content = content;
                this.creatorId = creatorId;
                this.createTime = createTime;
                this.creatorName = creatorName;
            }

            public String getTitle() {
                return title;
            }

            public NewsRes setTitle(String title) {
                this.title = title;
                return this;
            }

            public String getContent() {
                return content;
            }

            public NewsRes setContent(String content) {
                this.content = content;
                return this;
            }

            public Long getCreatorId() {
                return creatorId;
            }

            public NewsRes setCreatorId(Long creatorId) {
                this.creatorId = creatorId;
                return this;
            }

            public Timestamp getCreateTime() {
                return createTime;
            }

            public NewsRes setCreateTime(Timestamp createTime) {
                this.createTime = createTime;
                return this;
            }

            public String getCreatorName() {
                return creatorName;
            }

            public NewsRes setCreatorName(String creatorName) {
                this.creatorName = creatorName;
                return this;
            }
        };
        //学生
        if(curUser.getType()=='1') {
            ArrayList<NewsRes> res = new ArrayList<NewsRes>();
            List<AttendCourse> curUserCourse = attendCourseService.list(new QueryWrapper<AttendCourse>().eq("student_id", curUserId));

            for (int i = 0; i < curUserCourse.size(); i++) {
                List<News> news = newsService.list(new QueryWrapper<News>().eq("course_id", curUserCourse.get(i).getCourseId()));
                for (int k = 0; k < news.size(); k++) {
                    News temp=news.get(k);
                    User creator=userService.getOne(new QueryWrapper<User>().eq("id",temp.getCreatorId()));
                    NewsRes newsRes=new NewsRes(
                            temp.getId(),
                            temp.getTitle(),
                            temp.getContent(),
                            temp.getCreatorId(),
                            temp.getCreateTime(),
                            creator.getName()
                            );
                    res.add(newsRes);
                }
            }
            List<News> publicNews=newsService.list(new QueryWrapper<News>().isNull("course_id"));
            for (int i=0;i<publicNews.size();i++){
                News temp=publicNews.get(i);
                NewsRes newsRes=new NewsRes(
                        temp.getId(),
                        temp.getTitle(),
                        temp.getContent(),
                        temp.getCreatorId(),
                        temp.getCreateTime(),
                        "管理员"
                );
                res.add(newsRes);
            }
            //按时间降序排列
            res.sort(new Comparator<NewsRes>() {
                @Override
                public int compare(NewsRes arg0, NewsRes arg1) {
                    if (arg0.getCreateTime() == null || arg1.getCreateTime() == null)
                        return 0;
                    return arg1.getCreateTime().compareTo(arg0.getCreateTime());
                }
            });
            Map map = new LinkedHashMap();
            map.put("record", res);
            return Result.succ(map);
        }
        else if(curUser.getType()=='2'){//教师
            ArrayList<NewsRes> res = new ArrayList<NewsRes>();
            List<News> publicNews=newsService.list(new QueryWrapper<News>().isNull("course_id"));
            for (int i=0;i<publicNews.size();i++){
                News temp=publicNews.get(i);
                NewsRes newsRes=new NewsRes(
                        temp.getId(),
                        temp.getTitle(),
                        temp.getContent(),
                        temp.getCreatorId(),
                        temp.getCreateTime(),
                        "管理员"
                );
                res.add(newsRes);
            }
            List<News> news = newsService.list(new QueryWrapper<News>().eq("creator_id", curUserId));
            for (int i=0;i<news.size();i++){
                News temp=news.get(i);
                NewsRes newsRes=new NewsRes(
                        temp.getId(),
                        temp.getTitle(),
                        temp.getContent(),
                        temp.getCreatorId(),
                        temp.getCreateTime(),
                        curUser.getName()
                );
                res.add(newsRes);
            }
            res.sort(new Comparator<NewsRes>() {
                @Override
                public int compare(NewsRes arg0, NewsRes arg1) {
                    if (arg0.getCreateTime() == null || arg1.getCreateTime() == null)
                        return 0;
                    return arg1.getCreateTime().compareTo(arg0.getCreateTime());
                }
            });
            Map map = new LinkedHashMap();
            map.put("record", res);
            return Result.succ(map);
        }
        else{//管理员
            ArrayList<NewsRes> res = new ArrayList<NewsRes>();
            List<News> news = newsService.list(new QueryWrapper<News>().orderByDesc("create_time"));
            for(int i=0;i<news.size();i++){
                News temp=news.get(i);
                User creator=userService.getOne(new QueryWrapper<User>().eq("id",temp.getCreatorId()));
                NewsRes newsRes=new NewsRes(
                        temp.getId(),
                        temp.getTitle(),
                        temp.getContent(),
                        temp.getCreatorId(),
                        temp.getCreateTime(),
                        creator.getName()
                );
                res.add(newsRes);
            }
            Map map = new LinkedHashMap();
            map.put("record", res);
            return Result.succ(map);
        }
    }

    //发布新闻
    @RequiresAuthentication
    @PostMapping("/releaseNews")
    public Result releaseNews(@Validated @RequestBody ReleaseNewsDto releaseNewsDto, HttpServletResponse response){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        Course temp = courseService.getOne(new QueryWrapper<Course>().eq("teacher_id",curUserId).eq("course_number",releaseNewsDto.getCourseNumber()));
        if (temp==null)
            return Result.fail("教师没有开设该课");
        News news=new News();
        news.setContent(releaseNewsDto.getContent());
        news.setTitle(releaseNewsDto.getTitle());
        news.setCreateTime(new Timestamp(System.currentTimeMillis()));
        news.setCreatorId(curUser.getId());
        news.setCourseId(temp.getId());
        newsService.save(news);

        return Result.succ(null);
    }

    //发布全体新闻
    @RequiresAuthentication
    @PostMapping("/releasePublicNews")
    public Result releasePublicNews(@Validated @RequestBody ReleaseNewsDto releaseNewsDto, HttpServletResponse response){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()!='3')
            return Result.fail("没有权限");
        News news=new News();

        news.setContent(releaseNewsDto.getContent());
        news.setTitle(releaseNewsDto.getTitle());
        Timestamp curTimestamp=new Timestamp(System.currentTimeMillis());
        news.setCreateTime(curTimestamp);
        news.setCreatorId(curUser.getId());
        newsService.save(news);

        return Result.succ(null);
    }

    //删除新闻
    @RequiresAuthentication
    @GetMapping("/deleteNews")
    public Result deleteNews(@RequestParam Integer newsId){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        newsService.remove(new QueryWrapper<News>().eq("id",newsId));
        return Result.succ(null);
    }

    //修改新闻
    @RequiresAuthentication
    @PostMapping("/editNews")
    public Result editNews(@RequestParam Integer newsId, @Validated @RequestBody EditNewsDto editNewsDto, HttpServletResponse response){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        News curNews = newsService.getOne(new QueryWrapper<News>().eq("id",newsId));
        curNews.setContent(editNewsDto.getContent());
        curNews.setTitle((editNewsDto.getTitle()));
        curNews.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newsService.update(curNews,new QueryWrapper<News>().eq("id",curNews.getId()));
        return Result.succ(null);
    }

}
