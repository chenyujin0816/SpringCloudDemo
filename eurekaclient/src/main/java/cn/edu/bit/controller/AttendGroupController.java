package cn.edu.bit.controller;

import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.AttendGroup;
import cn.edu.bit.entity.User;
import cn.edu.bit.service.AttendGroupService;
import cn.edu.bit.service.UserService;
import cn.edu.bit.utils.ShiroUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping("/attendGroup")
public class AttendGroupController {
    @Autowired
    AttendGroupService attendGroupService;
    @Autowired
    UserService userService;

    @RequiresAuthentication
    @GetMapping("/attend")
    public Result attend(@RequestParam Long groupId){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()!='1')
            return Result.fail("没有权限");
        AttendGroup tmp=attendGroupService.getOne(new QueryWrapper<AttendGroup>().eq("group_id",groupId).eq("student_id",curUserId));
        if(tmp!=null)
            return Result.fail("已经加入该组");
        AttendGroup attendGroup=new AttendGroup();
        attendGroup.setCreateTime(new Timestamp(System.currentTimeMillis()));
        attendGroup.setStudentId((long)curUserId);
        attendGroup.setGroupId(groupId);
        attendGroupService.save(attendGroup);
        return Result.succ(null);
    }

    @RequiresAuthentication
    @GetMapping("/exit")
    public Result exit(@RequestParam Long groupId){
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()!='1')
            return Result.fail("没有权限");
        AttendGroup tmp=attendGroupService.getOne(new QueryWrapper<AttendGroup>().eq("group_id",groupId).eq("student_id",curUserId));
        if(tmp==null)
            return Result.fail("尚未加入该组");

        attendGroupService.remove(new QueryWrapper<AttendGroup>().eq("group_id",groupId).eq("student_id",curUserId));
        return Result.succ(null);
    }

}
