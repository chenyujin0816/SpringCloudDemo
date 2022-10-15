package cn.edu.bit.controller;

import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.AttendCourse;
import cn.edu.bit.entity.Course;
import cn.edu.bit.entity.StudentCourse;
import cn.edu.bit.entity.User;
import cn.edu.bit.service.AttendCourseService;
import cn.edu.bit.service.CourseService;
import cn.edu.bit.service.StudentCourseService;
import cn.edu.bit.service.UserService;
import cn.edu.bit.utils.ShiroUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/studentcourse")
public class StudentCourseController {
    @Autowired
    StudentCourseService studentCourseService;
    @Autowired
    CourseService courseService;
    @Autowired
    UserService userService;
    @Autowired
    AttendCourseService attendCourseService;

    @RequiresAuthentication
    @GetMapping("/getworklist") //查询该周作业情况
    public Result getworklist(@RequestParam(value = "courseinfo") String courseinfo,
                              @RequestParam(value = "teachername") String teachername,
                              @RequestParam(value = "week") Integer week){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() == '1')
            return Result.fail("该学生用户权限不够");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo));
        if(curCourse.getTeacherId() != curUser.getId() && curUser.getType() == '2')
            return Result.fail("该课程不属于您任教");

        class StudentRes{
            String name; //姓名
            String number; //学号
            String filename; //文件名
            Integer grade; //评分
            char posted; //是否提交作业

            public StudentRes(String name, String number) {
                this.name = name;
                this.number = number;
            }

            public String getNumber() {
                return number;
            }

            public StudentRes setNumber(String number) {
                this.number = number;
                return this;
            }

            public String getName() {
                return name;
            }

            public StudentRes setName(String name) {
                this.name = name;
                return this;
            }

            public String getFilename() {
                return filename;
            }

            public StudentRes setFilename(String filename) {
                this.filename = filename;
                return this;
            }

            public Integer getGrade() {
                return grade;
            }

            public StudentRes setGrade(Integer grade) {
                this.grade = grade;
                return this;
            }

            public char getPosted() {return posted;}

            public StudentRes setPosted(char posted){
                this.posted = posted;
                return this;
            }
        };

        ArrayList<StudentRes> res = new ArrayList<StudentRes>();
        List<AttendCourse> attendCourseList = attendCourseService.list(new QueryWrapper<AttendCourse>().eq("course_id",curCourse.getId()));

        for(int i = 0 ; i < attendCourseList.size(); i++){
            User curStudent = userService.getOne(new QueryWrapper<User>().eq("id",attendCourseList.get(i).getStudentId()));

            StudentRes studentRes = new StudentRes(
                    curStudent.getName(),
                    curStudent.getUsername()
            );

            StudentCourse curStudentcourse = studentCourseService.getOne(new QueryWrapper<StudentCourse>().
                    eq("course_id",curCourse.getId()).eq("student_id",curStudent.getId()).
                    eq("course_week",week));

            if(curStudentcourse == null)
            {
                studentRes.setPosted('0');
            }
            else
            {
                studentRes.setPosted('1'); //有提交物
                studentRes.setFilename(curStudentcourse.getUrl());
                studentRes.setGrade(curStudentcourse.getScore());
            }

            res.add(studentRes);
        }
        Map map = new LinkedHashMap();
        map.put("record", res);

        return Result.succ(map);
    }

    @RequiresAuthentication
    @PostMapping("/judgework") //作业评分
    public Result judgework(@RequestParam(value = "courseinfo") String courseinfo,
                            @RequestParam(value = "teachername") String teachername,
                            @RequestParam(value = "grade") String grade,
                            @RequestParam(value = "number") String number,
                            @RequestParam(value = "week") String week){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() != '2')
            return Result.fail("该用户不是老师");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo));
        if(curCourse.getTeacherId() != curUser.getId())
            return Result.fail("该课程不属于您任教");

        User student = userService.getOne(new QueryWrapper<User>().eq("username",number));

        int a = Integer.parseInt(grade);
        if (a < 1 )
            return Result.fail("评分不能低于1分");
        if (a > 100)
            return Result.fail("评分不能高于100分");

        int curWeek = Integer.parseInt(week);

        StudentCourse studentCourse = studentCourseService.getOne(new QueryWrapper<StudentCourse>()
                .eq("course_id",curCourse.getId()).eq("student_id",student.getId())
                .eq("course_week",curWeek));

        studentCourse.setScore(a);

        studentCourseService.update(studentCourse,new QueryWrapper<StudentCourse>()
                .eq("course_id",curCourse.getId()).eq("student_id",student.getId())
                .eq("course_week",curWeek));


        List<StudentCourse> studentCourseList = studentCourseService.list(new QueryWrapper<StudentCourse>()
                .eq("course_id",curCourse.getId()).eq("student_id",student.getId()));

        Integer sum = 0;
        for(int i = 0 ; i < studentCourseList.size(); i++ )
        {
            sum += studentCourseList.get(i).getScore();
        }
        sum /= studentCourseList.size();

        AttendCourse curAttendcourse = attendCourseService.getOne(new QueryWrapper<AttendCourse>()
                .eq("course_id",curCourse.getId()).eq("student_id",student.getId()));
        curAttendcourse.setGrade(sum);
        attendCourseService.update(curAttendcourse,new QueryWrapper<AttendCourse>()
                .eq("course_id",curCourse.getId()).eq("student_id",student.getId()));

        return Result.succ("成功评分");
    }

    @RequiresAuthentication
    @GetMapping("/lookgrade") //查询该周作业情况
    public Result lookgrade(@RequestParam(value = "courseinfo") String courseinfo,
                            @RequestParam(value = "teachername") String teachername,
                            @RequestParam(value = "week") String  week){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() != '1')
            return Result.fail("该用户不是学生");

        Integer curWeek = Integer.parseInt(week);

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo));
        StudentCourse studentCourse = studentCourseService.getOne(new QueryWrapper<StudentCourse>()
                .eq("student_id",curUser.getId()).eq("course_id",curCourse.getId())
                .eq("course_week",curWeek));

        return Result.succ(studentCourse.getScore());
    }



}
