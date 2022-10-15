package cn.edu.bit.controller;

import cn.edu.bit.common.dto.AddCourseDto;
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
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    CourseService courseService;
    @Autowired
    UserService userService;
    @Autowired
    AttendCourseService attendCourseService;
    @Autowired
    StudentCourseService studentCourseService;

    @RequiresAuthentication
    @GetMapping("/courses") //查询所有课程
    public Result getInfos(@RequestParam(defaultValue = "1") Integer currentPage){
        class CourseRes{
            String info; //课程信息
            String name; //老师姓名
            char status; //状态
            char attend; //若是学生，是否有报名

            public CourseRes(String info, String name, char status) {
                this.info = info;
                this.name = name;
                this.status = status;
            }

            public String getInfo() {
                return info;
            }

            public CourseRes setInfo(String info) {
                this.info = info;
                return this;
            }

            public String getName() {
                return name;
            }

            public CourseRes setName(String name) {
                this.name = name;
                return this;
            }

            public char getStatus() {return status;}

            public CourseRes setStatus(char status){
                this.status = status;
                return this;
            }

            public char getAttend() {return attend;}

            public CourseRes setAttend(char attend){
                this.attend = attend;
                return this;
            }

        };
        ArrayList<CourseRes> res = new ArrayList<CourseRes>();
        List<Course> courselist = courseService.list(new QueryWrapper<Course>());

        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        for(int i = 0 ; i < courselist.size(); i++){
            User curteacher = userService.getOne(new QueryWrapper<User>().eq("id",courselist.get(i).getTeacherId()));

            CourseRes courseRes = new CourseRes(
                    courselist.get(i).getCourseInfo(),
                    curteacher.getName(),
                    courselist.get(i).getCourseStatus()
            );

            if(curUser.getType() == '1'){
                AttendCourse curAttendcourse = attendCourseService.getOne(new QueryWrapper<AttendCourse>().
                        eq("student_id",curUser.getId()).eq("course_id",courselist.get(i).getId()));
                if(curAttendcourse != null)
                    courseRes.setAttend('1');
                else courseRes.setAttend('0');
            }

            res.add(courseRes);
        }
        Map map = new LinkedHashMap();
        map.put("record", res);

        return Result.succ(map);
/*
        Page page = new Page(currentPage,10);

        IPage pageData = courseService.page(page, new QueryWrapper<Course>());

        return Result.succ(pageData);
*/
    }

    @GetMapping("/mycourse") //查询单个课程
    public Result getInfo(@RequestParam(value = "courseinfo") String courseinfo,
                          @RequestParam(value = "teachername") String teachername) {

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info", courseinfo));
        User curTeacher = userService.getOne(new QueryWrapper<User>().eq("name",teachername));
        if(curCourse == null)
            return Result.fail("该课程不存在");

        Map map=new LinkedHashMap();
        map.put("courseinfo", curCourse.getCourseInfo());
        map.put("teachername", curTeacher.getName());
        map.put("weeks", curCourse.getCourseWeek());
        map.put("courseid",curCourse.getId());
        return Result.succ(map);
    }

    @RequiresAuthentication
    @PostMapping("/add") //老师申请添加课程
    public Result add(@Validated @RequestBody AddCourseDto addCourseDto){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        if(curUser.getType() != '2')
            return Result.fail("该用户不是老师");

        Course course1 = courseService.getOne(new QueryWrapper<Course>().eq("course_number",addCourseDto.getCourseNumber()));
        if(course1!=null)
            return Result.fail("该课程编号已被占用");
        Course course2 = courseService.getOne(new QueryWrapper<Course>().eq("course_name",addCourseDto.getCourseName()));
        if(course2!=null)
            return Result.fail("该课程已有申报");
        Course course3 = courseService.getOne(new QueryWrapper<Course>().eq("course_info",addCourseDto.getCourseInfo()));
        if(course3!=null)
            return Result.fail("该课程名已被占用");

        Course newcourse = new Course();
        newcourse.setCourseNumber(addCourseDto.getCourseNumber());
        newcourse.setCourseName(addCourseDto.getCourseName());
        newcourse.setCourseInfo(addCourseDto.getCourseInfo());
        newcourse.setCourseScore(addCourseDto.getCourseScore());
        newcourse.setCoursePwd(addCourseDto.getCoursePwd());
        newcourse.setTeacherId(curUser.getId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = simpleDateFormat.parse(addCourseDto.getCourseStart());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(addCourseDto.getCourseWeek() > 18)
            return Result.fail("开课周数超过18周");

        if(addCourseDto.getCourseWeek() < 3)
            return Result.fail("开课周低于3周");

        Timestamp timestamp = new Timestamp(date.getTime());
        newcourse.setCourseStart(timestamp);
        newcourse.setCourseWeek(addCourseDto.getCourseWeek());
        newcourse.setCourseStatus('2');
        newcourse.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        courseService.save(newcourse);

        return Result.succ("申请增加课程成功，等待管理员通过");
    }

    @RequiresAuthentication
    @PostMapping("/delete") //老师申请删除课程
    public Result delete(@RequestParam(value = "courseinfo") String courseinfo){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        if(curUser.getType() != '2')
            return Result.fail("该用户不是老师");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo));
        Assert.notNull(curCourse, "该课程不存在");

        if(curCourse.getTeacherId() != curUser.getId())
            return Result.fail("该课程不属于此任课老师");

        curCourse.setCourseStatus('3');
        courseService.update(curCourse,new QueryWrapper<Course>().eq("course_info",courseinfo));

        return Result.succ("申请删除课程成功，等待管理员通过");
    }

    @RequiresAuthentication
    @PostMapping("/fixedadd") //管理员通过申请添加的课程
    public Result fixedadd(@RequestParam(value = "courseinfo") String courseinfo,
                           @RequestParam(value = "teachername") String teachername){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        if(curUser.getType() != '3')
            return Result.fail("该用户不是管理员");

        User curTeacher = userService.getOne(new QueryWrapper<User>().eq("name",teachername));
        if (curTeacher == null)  return Result.fail("该教师不存在");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));
        if (curCourse == null)  return  Result.fail("该课程不存在");

        if(curCourse.getCourseStatus() != '2')
            return Result.fail("该课程并不在申请添加队列中");

        curCourse.setCourseStatus('0');
        courseService.update(curCourse,new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));
        return Result.succ("课程成功添加");
    }

    @RequiresAuthentication
    @PostMapping("/fixeddelete") //管理员通过申请删除的课程
    public Result fixeddelete(@RequestParam(value = "courseinfo") String courseinfo,
                              @RequestParam(value = "teachername") String teachername){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        User curTeacher = userService.getOne(new QueryWrapper<User>().eq("name",teachername));
        if (curTeacher == null)  return Result.fail("该教师不存在");

        if(curUser.getType() != '3')
            return Result.fail("该用户不是管理员");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));
        if (curCourse == null)  return  Result.fail("该课程不存在");

        if(curCourse.getCourseStatus() != '3')
            return Result.fail("该课程并不在申请删除队列中");

        courseService.remove(new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));;

        List<AttendCourse> attendCourseList = attendCourseService.list(new QueryWrapper<AttendCourse>().eq("course_id",curCourse.getId()));
        for(int i = 0 ; i < attendCourseList.size(); i++){
            attendCourseService.remove(new QueryWrapper<AttendCourse>().eq("id",attendCourseList.get(i).getId()));
        }

        List<StudentCourse> studentCourseList = studentCourseService.list(new QueryWrapper<StudentCourse>().eq("course_id",curCourse.getId()));
        for(int i = 0 ; i < studentCourseList.size(); i++){
            studentCourseService.remove(new QueryWrapper<StudentCourse>().eq("id",studentCourseList.get(i).getId()));
        }

        String assignmentPath = new File("files" + File.separator +
                "assignment" + File.separator +
                curCourse.getCourseInfo()).getAbsolutePath();

        String handoutPath = new File("files" + File.separator +
                "handout" + File.separator +
                curCourse.getCourseInfo()).getAbsolutePath();

        FileSystemUtils.deleteRecursively(new File(assignmentPath));
        FileSystemUtils.deleteRecursively(new File(handoutPath));

        return Result.succ("课程成功删除");
    }

    @RequiresAuthentication
    @PostMapping("/fixed") //管理员拒绝申请删除的课程，让其恢复
    public Result fixed(@RequestParam(value = "courseinfo") String courseinfo,
                        @RequestParam(value = "teachername") String teachername){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        if(curUser.getType() != '3')
            return Result.fail("该用户不是管理员");

        User curTeacher = userService.getOne(new QueryWrapper<User>().eq("name",teachername));
        if (curTeacher == null)  return Result.fail("该教师不存在");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));
        if (curCourse == null)  return  Result.fail("该课程不存在");

        if(curCourse.getCourseStatus() != '3')
            return Result.fail("该课程并不在申请删除队列中");

        curCourse.setCourseStatus('0');
        courseService.update(curCourse,new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));
        return Result.succ("课程状态恢复");
    }

    @RequiresAuthentication
    @GetMapping("/studentlist") //查询所有报名本课的学生
    public Result studentlist(@RequestParam(value = "courseid") Integer courseid){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() != '2')
            return Result.fail("该用户不是老师");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("id",courseid));
        if(curCourse.getTeacherId() != curUser.getId())
            return Result.fail("该课程不属于您任教");

        class StudentRes{
            Long id;//ID
            String number; //学号
            String name; //姓名

            public Long getId() {
                return id;
            }

            public StudentRes setId(Long id) {
                this.id = id;
                return this;
            }

            public StudentRes(Long id, String number, String name) {
                this.id = id;
                this.number = number;
                this.name = name;
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
        };
        ArrayList<StudentRes> res = new ArrayList<StudentRes>();
        List<AttendCourse> attendcourselist = attendCourseService.list(new QueryWrapper<AttendCourse>().eq("course_id",courseid));

        for(int i = 0 ; i < attendcourselist.size(); i++){
            List<User> studentlist = userService.list(new QueryWrapper<User>().eq("id",attendcourselist.get(i).getStudentId()));
            for(int j = 0 ; j < studentlist.size() ; j++){
                StudentRes studentRes = new StudentRes(
                        studentlist.get(j).getId(),
                        studentlist.get(j).getUsername(),
                        studentlist.get(j).getName()
                );
                res.add(studentRes);
            }
        }
        Map map = new LinkedHashMap();
        map.put("record", res);

        return Result.succ(map);
    }

    @RequiresAuthentication
    @PostMapping("/Qdelete") //管理员主动删除课程
    public Result Qdelete(@RequestParam(value = "courseinfo") String courseinfo,
                          @RequestParam(value = "teachername") String teachername){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() != '3')
            return Result.fail("该用户不是管理员");

        User curTeacher = userService.getOne(new QueryWrapper<User>().eq("name",teachername));
        if (curTeacher == null)  return Result.fail("该教师不存在");

        Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));
        if (curCourse == null)  return  Result.fail("该课程不存在");


        courseService.remove(new QueryWrapper<Course>().eq("course_info",courseinfo).eq("teacher_id",curTeacher.getId()));;

        List<AttendCourse> attendCourseList = attendCourseService.list(new QueryWrapper<AttendCourse>().eq("course_id",curCourse.getId()));
        for(int i = 0 ; i < attendCourseList.size(); i++){
            attendCourseService.remove(new QueryWrapper<AttendCourse>().eq("id",attendCourseList.get(i).getId()));
        }

        List<StudentCourse> studentCourseList = studentCourseService.list(new QueryWrapper<StudentCourse>().eq("course_id",curCourse.getId()));
        for(int i = 0 ; i < studentCourseList.size(); i++){
            studentCourseService.remove(new QueryWrapper<StudentCourse>().eq("id",studentCourseList.get(i).getId()));
        }

        String assignmentPath = new File("files" + File.separator +
                "assignment" + File.separator +
                curCourse.getCourseInfo()).getAbsolutePath();

        String handoutPath = new File("files" + File.separator +
                "handout" + File.separator +
                curCourse.getCourseInfo()).getAbsolutePath();

        FileSystemUtils.deleteRecursively(new File(assignmentPath));
        FileSystemUtils.deleteRecursively(new File(handoutPath));

        return Result.succ("课程成功删除");
    }

    @RequiresAuthentication
    @GetMapping("/addlist") //管理员查询正在等待添加的课程
    public Result addlist(){
        class CourseRes{
            Date date;
            String courseinfo; //课程信息
            String teachername; //老师姓名


            public CourseRes(Date date, String courseinfo, String teachername) {
                this.date = date;
                this.courseinfo = courseinfo;
                this.teachername = teachername;
            }

            public Date getDate() {
                return date;
            }

            public CourseRes SetDate(Date date){
                this.date = date;
                return this;
            }

            public String getCourseinfo() {
                return courseinfo;
            }

            public CourseRes setCourseinfo(String courseinfo) {
                this.courseinfo = courseinfo;
                return this;
            }

            public String getTeachername() {
                return teachername;
            }

            public CourseRes setTeachername(String teachername) {
                this.teachername = teachername;
                return this;
            }
        };

        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() != '3')
            return Result.fail("该用户不是管理员");

        ArrayList<CourseRes> res = new ArrayList<CourseRes>();
        List<Course> courselist = courseService.list(new QueryWrapper<Course>().eq("course_status",'2'));

        for(int i = 0 ; i < courselist.size(); i++){
            User curteacher = userService.getOne(new QueryWrapper<User>().eq("id",courselist.get(i).getTeacherId()));
            Date d = new Date(courselist.get(i).getCreateTime().getTime());
            CourseRes courseRes = new CourseRes(
                    d,
                    courselist.get(i).getCourseInfo(),
                    curteacher.getName()
            );
            res.add(courseRes);
        }
        Map map = new LinkedHashMap();
        map.put("record", res);

        return Result.succ(map);
    }

    @RequiresAuthentication
    @GetMapping("/deletelist") //管理员查询正在等待删除的课程
    public Result deletelist(){
        class CourseRes{
            Date date;
            String courseinfo; //课程信息
            String teachername; //老师姓名

            public CourseRes(Date date, String courseinfo, String teachername) {
                this.date = date;
                this.courseinfo = courseinfo;
                this.teachername = teachername;
            }

            public Date getDate() {
                return date;
            }

            public CourseRes SetDate(Date date){
                this.date = date;
                return this;
            }

            public String getCourseinfo() {
                return courseinfo;
            }

            public CourseRes setCourseinfo(String courseinfo) {
                this.courseinfo = courseinfo;
                return this;
            }

            public String getTeachername() {
                return teachername;
            }

            public CourseRes setTeachername(String teachername) {
                this.teachername = teachername;
                return this;
            }
        };

        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() != '3')
            return Result.fail("该用户不是管理员");

        ArrayList<CourseRes> res = new ArrayList<CourseRes>();
        List<Course> courselist = courseService.list(new QueryWrapper<Course>().eq("course_status",'3'));

        for(int i = 0 ; i < courselist.size(); i++){
            User curteacher = userService.getOne(new QueryWrapper<User>().eq("id",courselist.get(i).getTeacherId()));
            Date d = new Date(courselist.get(i).getCreateTime().getTime());
            CourseRes courseRes = new CourseRes(
                    d,
                    courselist.get(i).getCourseInfo(),
                    curteacher.getName()
            );
            res.add(courseRes);
        }
        Map map = new LinkedHashMap();
        map.put("record", res);

        return Result.succ(map);
    }

    @RequiresAuthentication
    @GetMapping("/myindex") //查询主页的课程表
    public Result myindex(){
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));

        class CourseRes{
            Date date;
            String courseinfo; //课程信息
            String teachername; //老师姓名
            String weeks;
            Long courseId;

            public Long getCourseId() {
                return courseId;
            }

            public CourseRes(Date date, String courseinfo, String teachername, String weeks, Long courseId) {
                this.date = date;
                this.courseinfo = courseinfo;
                this.teachername = teachername;
                this.weeks = weeks;
                this.courseId = courseId;
            }

            public CourseRes setCourseId(Long courseId) {
                this.courseId = courseId;
                return this;
            }

            public String getWeeks() {
                return weeks;
            }

            public CourseRes setWeeks(String weeks) {
                this.weeks = weeks;
                return this;
            }

            public Date getDate() {
                return date;
            }

            public CourseRes SetDate(Date date){
                this.date = date;
                return this;
            }

            public String getCourseinfo() {
                return courseinfo;
            }

            public CourseRes setCourseinfo(String courseinfo) {
                this.courseinfo = courseinfo;
                return this;
            }

            public String getTeachername() {
                return teachername;
            }

            public CourseRes setTeachername(String teachername) {
                this.teachername = teachername;
                return this;
            }
        };

        if(curUser.getType() == '1') //学生的返回
        {
            ArrayList<CourseRes> res = new ArrayList<CourseRes>();
            List<AttendCourse> attendCourseList = attendCourseService.list(new QueryWrapper<AttendCourse>().eq("student_id",curUser.getId()));

            for(int i = 0 ; i < attendCourseList.size(); i++){
                Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("id",attendCourseList.get(i).getCourseId()));
                User curteacher = userService.getOne(new QueryWrapper<User>().eq("id",curCourse.getTeacherId()));
                Date d = new Date(curCourse.getCreateTime().getTime());
                CourseRes courseRes = new CourseRes(
                        d,
                        curCourse.getCourseInfo(),
                        curteacher.getName(),
                        String.valueOf(curCourse.getCourseWeek()),
                        curCourse.getId()
                );
                res.add(courseRes);
            }
            Map map = new LinkedHashMap();
            map.put("record", res);

            return Result.succ(map);
        }
        else if(curUser.getType() == '2') //教师的返回
        {
            ArrayList<CourseRes> res = new ArrayList<CourseRes>();
            List<Course> courselist = courseService.list(new QueryWrapper<Course>().eq("teacher_id",curUser.getId()));

            for(int i = 0 ; i < courselist.size(); i++){
                Date d = new Date(courselist.get(i).getCreateTime().getTime());
                CourseRes courseRes = new CourseRes(
                        d,
                        courselist.get(i).getCourseInfo(),
                        curUser.getName(),
                        String.valueOf(courselist.get(i).getCourseWeek()),
                        courselist.get(i).getId()
                );
                res.add(courseRes);
            }
            Map map = new LinkedHashMap();
            map.put("record", res);

            return Result.succ(map);
        }
        else if(curUser.getType() == '3')
        {
            ArrayList<CourseRes> res = new ArrayList<CourseRes>();
            List<Course> courselist = courseService.list(new QueryWrapper<Course>());

            for(int i = 0 ; i < courselist.size(); i++){
                User curteacher = userService.getOne(new QueryWrapper<User>().eq("id",courselist.get(i).getTeacherId()));
                Date d = new Date(courselist.get(i).getCreateTime().getTime());
                CourseRes courseRes = new CourseRes(
                        d,
                        courselist.get(i).getCourseInfo(),
                        curteacher.getName(),
                        String.valueOf(courselist.get(i).getCourseWeek()),
                        courselist.get(i).getId()
                );

                res.add(courseRes);
            }
            Map map = new LinkedHashMap();
            map.put("record", res);

            return Result.succ(map);
        }
        return Result.fail("err");
    }



}
