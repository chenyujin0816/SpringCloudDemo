package cn.edu.bit.controller;

import cn.edu.bit.common.dto.AddFileDto;
import cn.edu.bit.common.dto.JudgeWorkDto;
import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.*;
import cn.edu.bit.service.*;
import cn.edu.bit.utils.ShiroUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/contentcourse")
    public class ContentCourseController {
        @Autowired
        ContentCourseService contentCourseService;
        @Autowired
        CourseService courseService;
        @Autowired
        UserService userService;
        @Autowired
        AttendCourseService attendCourseService;
        @Autowired
        StudentCourseService studentCourseService;

        @RequiresAuthentication
        @GetMapping("/courseinfo") //课程 某一周的信息
        public Result courseinfo(@RequestParam(value = "courseid") Integer courseid, @RequestParam(value = "week") Integer week ){
            String curUsername = ShiroUtil.getProfile().getUsername();
            User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
            Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("id",courseid));
            ContentCourse contentCourse = contentCourseService.getOne(new QueryWrapper<ContentCourse>().eq("course_id",courseid).eq("course_week",week));
            if(contentCourse == null)
                return Result.succ(null);
            else return Result.succ(contentCourse);
        }

        @RequiresAuthentication
        @PostMapping("/addfile") //教师上传周文件，若有则替换
        public Result addfile(@RequestParam(value = "courseid") Integer courseid,
                              @RequestParam(value = "week") Integer week,
                              @Validated @RequestBody AddFileDto addFileDto){
            String curUsername = ShiroUtil.getProfile().getUsername();
            User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
            Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("id",courseid));
            ContentCourse contentCourse = contentCourseService.getOne(new QueryWrapper<ContentCourse>().eq("course_id",courseid).eq("course_week",week));
            if(curCourse.getTeacherId() != curUser.getId())
                return Result.fail("不是该课的任课老师");

            if(contentCourse == null) {
                ContentCourse newcontent = new ContentCourse();
                newcontent.setCourseId(curCourse.getId());
                newcontent.setTeacherId(curUser.getId());
                newcontent.setCourseWeek(week);
                newcontent.setUrl(addFileDto.getUrl());
                contentCourseService.save(newcontent);
                return Result.succ("文件成功上传");
            }
            else {
                contentCourse.setUrl(addFileDto.getUrl());
                contentCourseService.update(contentCourse,new QueryWrapper<ContentCourse>().eq("course_id",courseid).eq("course_week",week));
                return Result.succ("文件成功替换");
            }
        }

        @RequiresAuthentication
        @PostMapping("/addwork") //学生上传个人周作业
        public Result addwork(@RequestParam(value = "courseid") Integer courseid,
                              @RequestParam(value = "week") Integer week,
                              @Validated @RequestBody AddFileDto addFileDto){
            String curUsername = ShiroUtil.getProfile().getUsername();
            User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
            Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("id",courseid));
            StudentCourse studentCourse = studentCourseService.getOne(new QueryWrapper<StudentCourse>().eq("course_id",courseid).eq("course_week",week).eq("student_id",curUser.getId()));
            AttendCourse attendCourse = attendCourseService.getOne(new QueryWrapper<AttendCourse>().eq("course_id",courseid).eq("student_id",curUser.getId()));
            if(attendCourse == null) {
                return Result.fail("学生没有此课");
            }
            if(curCourse.getCourseStatus() != '0'){
                return Result.fail("该课程目前无法操作");
            }

            if(studentCourse == null) {
                StudentCourse newstudentcourse= new StudentCourse();
                newstudentcourse.setCourseId(curCourse.getId());
                newstudentcourse.setStudentId(curUser.getId());
                newstudentcourse.setCourseWeek(week);
                newstudentcourse.setUrl(addFileDto.getUrl());
                studentCourseService.save(newstudentcourse);
                return Result.succ("文件成功上传");
            }
            else {
                studentCourse.setUrl(addFileDto.getUrl());
                studentCourseService.update(studentCourse,new QueryWrapper<StudentCourse>().eq("course_id",courseid).eq("course_week",week).eq("student_id",curUser.getId()));
                return Result.succ("文件成功替换");
            }
        }

        @RequiresAuthentication
        @PostMapping("/judge") //老师评价学生作业
        public Result addwork(@RequestParam(value = "courseid") Integer courseid,
                              @RequestParam(value = "week") Integer week,
                              @RequestParam(value = "studentid") Integer studentid,
                              @Validated @RequestBody JudgeWorkDto judgeWorkDto){
            String curUsername = ShiroUtil.getProfile().getUsername();
            User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
            Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("id",courseid));
            User student = userService.getOne(new QueryWrapper<User>().eq("id",studentid));
            StudentCourse studentCourse = studentCourseService.getOne(new QueryWrapper<StudentCourse>().eq("course_id",courseid).eq("course_week",week).eq("student_id",student.getId()));
            AttendCourse attendCourse = attendCourseService.getOne(new QueryWrapper<AttendCourse>().eq("course_id",courseid).eq("student_id",student.getId()));
            if(curCourse.getTeacherId() != curUser.getId())
                return Result.fail("不是该课的任课老师");
            if(attendCourse == null)
                return Result.fail("学生没有此课");
            if(studentCourse == null)
                return Result.fail("学生没有提交作业");
            if(judgeWorkDto.getScore() < 0 || judgeWorkDto.getScore() > 100)
                return Result.fail("给的分数不合理");

            studentCourse.setScore(judgeWorkDto.getScore());
            studentCourseService.update(studentCourse,new QueryWrapper<StudentCourse>().eq("course_id",courseid).eq("course_week",week).eq("student_id",student.getId()));

            //简单的分值记录
            int courseScore = attendCourse.getGrade();
            if(courseScore == 0)
                courseScore = judgeWorkDto.getScore();
            else courseScore = (int)((judgeWorkDto.getScore()+courseScore) / 2);
            attendCourse.setGrade(courseScore);
            attendCourseService.update(attendCourse,new QueryWrapper<AttendCourse>().eq("course_id",courseid).eq("student_id",student.getId()));

            return Result.succ("成功给作业评分");
        }



    }
