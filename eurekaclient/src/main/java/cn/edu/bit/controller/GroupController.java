package cn.edu.bit.controller;

import cn.edu.bit.common.dto.CreateGroupDto;
import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.AttendGroup;
import cn.edu.bit.entity.Group;
import cn.edu.bit.entity.User;
import cn.edu.bit.service.AttendGroupService;
import cn.edu.bit.service.GroupService;
import cn.edu.bit.service.UserService;
import cn.edu.bit.utils.ShiroUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RelationSupport;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;

@RestController
@RequestMapping("/group")
public class GroupController {
    @Autowired
    GroupService groupService;
    @Autowired
    UserService userService;
    @Autowired
    AttendGroupService attendGroupService;

    @RequiresAuthentication
    @PostMapping("/createGroup")
    public Result createGroup(@Validated @RequestBody CreateGroupDto createGroupDto, HttpServletResponse response){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        Group group=new Group();
        group.setGroupName(createGroupDto.getGroupName());
        group.setTeacherId((long)curUserId);
        group.setCourseId(createGroupDto.getCourseId());
        group.setCreateTime(new Timestamp(System.currentTimeMillis()));

        groupService.save(group);

        return Result.succ(null);
    }

    @RequiresAuthentication
    @GetMapping("/groups")
    public Result groupList(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam Long courseId){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        Page page = new Page(currentPage, 10);
        if(curUser.getType()=='1')
            return Result.fail("没有权限");
        else if(curUser.getType()=='2') {
            IPage pageData = groupService.page(page, new QueryWrapper<Group>().eq("course_id", courseId).orderByDesc("create_time"));
            return Result.succ(pageData);
        }else{
            IPage pageData = groupService.page(page, new QueryWrapper<Group>().orderByDesc("course_id"));
            return Result.succ(pageData);
        }
    }

    @RequiresAuthentication
    @GetMapping("/deleteGroup")
    public Result groupList(@RequestParam Long groupId){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()=='1')
            return Result.fail("没有权限");

        groupService.remove(new QueryWrapper<Group>().eq("id",groupId));
        attendGroupService.remove(new QueryWrapper<AttendGroup>().eq("group_id",groupId));
        return Result.succ(null);
    }

}
