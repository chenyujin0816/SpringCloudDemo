package cn.edu.bit.controller;

import cn.edu.bit.common.dto.CreateAssignDto;
import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.Assignment;
import cn.edu.bit.entity.User;
import cn.edu.bit.service.AssignmentService;
import cn.edu.bit.service.UserService;
import cn.edu.bit.utils.ShiroUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {
    @Autowired
    AssignmentService assignmentService;
    @Autowired
    UserService userService;


    //查看指定课程的所有作业
    @GetMapping("/assignList")
    public Result assignList(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam Long courseId){
        Page page = new Page(currentPage,10);
        IPage pageData = assignmentService.page(page,new QueryWrapper<Assignment>().eq("course_id",courseId).orderByDesc("create_time"));
        return Result.succ(pageData);
    }

    //查看作业详情
    @GetMapping("/assign")
    public Result assignDetail(@RequestParam Long courseId,@RequestParam Long week){
        Assignment assignment = assignmentService.getOne(new QueryWrapper<Assignment>().eq("course_id",courseId).eq("week",week));
        if(assignment==null)
            return Result.fail("该作业已被删除");
        return Result.succ(assignment);
    }

    //删除作业
    @RequiresAuthentication
    @GetMapping("/deleteAssignment")
    public Result deleteAssignment(@RequestParam Integer assignId){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        Assignment assignment=assignmentService.getOne(new QueryWrapper<Assignment>().eq("assign_id",assignId));
        if(assignment==null)
            return Result.fail("该作业不存在");
        assignmentService.remove(new QueryWrapper<Assignment>().eq("assign_id",assignId));
        return Result.succ(null);
    }

    //发布作业
    @RequiresAuthentication
    @PostMapping("/createAssignment")
    public Result createAssignment(@Validated @RequestBody CreateAssignDto createAssignDto, HttpServletResponse response){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        Assignment temp=assignmentService.getOne(new QueryWrapper<Assignment>().eq("course_id",createAssignDto.getCourseId())
                .eq("week",createAssignDto.getWeek()));
        if(temp!=null)
        {
            temp.setTeacherId((long) curUserId);
            temp.setTitle(createAssignDto.getTitle());
            temp.setDeadline(Timestamp.valueOf(createAssignDto.getDeadline()));
            temp.setDescription(createAssignDto.getDescription());
            temp.setCreateTime(new Timestamp(System.currentTimeMillis()));
            temp.setCourseId(createAssignDto.getCourseId());
            temp.setWeek(createAssignDto.getWeek());
            assignmentService.update(temp, new QueryWrapper<Assignment>().eq("course_id",createAssignDto.getCourseId())
                    .eq("week",createAssignDto.getWeek()));

            return Result.succ("更新了发布作业的内容");
        }
        Assignment assignment=new Assignment();
        assignment.setTeacherId((long) curUserId);
        assignment.setTitle(createAssignDto.getTitle());
        assignment.setDeadline(Timestamp.valueOf(createAssignDto.getDeadline()));
        assignment.setDescription(createAssignDto.getDescription());
        assignment.setCreateTime(new Timestamp(System.currentTimeMillis()));
        assignment.setCourseId(createAssignDto.getCourseId());
        assignment.setWeek(createAssignDto.getWeek());

        assignmentService.save(assignment);

        return Result.succ("该周作业发布成功");
    }

}
