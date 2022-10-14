package cn.edu.bit.controller;


import cn.edu.bit.common.dto.ChangePwdDto;
import cn.edu.bit.common.dto.EditInfoDto;
import cn.edu.bit.common.dto.LoginDto;
import cn.edu.bit.common.dto.RegisterDto;
import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.Users;
import cn.edu.bit.service.UsersService;
import cn.edu.bit.utils.*;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;


@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    UsersService userService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        Md5Util md5 = new Md5Util();
        Users user = userService.getOne(new QueryWrapper<Users>().eq("username", loginDto.getUsername()));

        if (user == null){
            return Result.fail("用户不存在");
        }
        if (!user.getPassword().equals(md5.getMD5String(loginDto.getPassword()))) {
            return Result.fail("密码不正确");
        }
        if(user.getStatus().equals("0")){
            return Result.fail("用户被冻结");
        }
        String jwt = jwtUtils.generateToken(user.getId());
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");
        return Result.succ(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("name", user.getName())
                .put("type", user.getType())
                .map()
        );
    }

    @RequiresAuthentication
    @PostMapping("/register")
    public Result register(@Validated @RequestBody RegisterDto registerDto) throws ParseException {
        Users user = userService.getOne(new QueryWrapper<Users>().eq("username", registerDto.getUsername()));
        if(user!=null)
            return Result.fail("用户已存在");

        Users newUser = new Users();
        BeanUtil.copyProperties(registerDto,newUser);
        userService.save(newUser);

        return Result.succ(null);
    }

    @RequiresAuthentication
    @PostMapping("/import")
    public Result importUser(@RequestBody MultipartFile file) throws Exception {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if (!curUser.getType().equals("4"))
            return Result.fail("没有权限");

        if(file.getName().endsWith(".csv")) {
            CsvReader csvReader = CsvUtil.getReader();
            File f = FileUtil.multipartFile2File(file);

            List<RegisterDto> rows = csvReader.read(ResourceUtil.getUtf8Reader(f.getPath()), RegisterDto.class);
//        List<RegisterDto> rows = csvReader.read(ResourceUtil.getReader(f.getPath(), CharsetUtil.CHARSET_GBK),RegisterDto.class);
            int count = 0;
            for (RegisterDto row : rows) {
                if (row.getUsername() == null)
                    continue;
                Users user = userService.getOne(new QueryWrapper<Users>().eq("username", row.getUsername()));
                if (user != null)
                    continue;

                if (row.getTutor().equals(""))
                    row.setTutor(null);
                if (row.getTitle().equals(""))
                    row.setTitle(null);
                if (row.getQualification().equals(""))
                    row.setQualification(null);
                Users newUser = new Users();
                BeanUtil.copyProperties(row, newUser);
                userService.save(newUser);
                count++;
            }
            return Result.succ("成功导入"+ count +"个用户;"+ (rows.size() - count) +"个未导入");
        }
        else if (file.getOriginalFilename().endsWith(".xlsx")||file.getOriginalFilename().endsWith("xls")) {
            InputStream in = file.getInputStream();
            ExcelReader excelReader = ExcelUtil.getReader(in);
            List<RegisterDto> rows = excelReader.readAll(RegisterDto.class);
            int count=0;
            for (RegisterDto row : rows) {
                if (row.getUsername() == null)
                    continue;
                Users user = userService.getOne(new QueryWrapper<Users>().eq("username", row.getUsername()));
                if (user != null)
                    continue;

                Users newUser = new Users();
                BeanUtil.copyProperties(row, newUser);
                userService.save(newUser);
                count++;
            }
            return Result.succ("成功导入"+ count +"个用户;"+ (rows.size() - count) +"个未导入");
        }
        return null;
    }

    @RequiresAuthentication
    @GetMapping("/info")
    public Result userInfo() {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        return Result.succ(curUser);
    }

    @RequiresAuthentication
    @PostMapping("/edit")
    public Result userInfoEdit(@Validated @RequestBody EditInfoDto editInfoDto, HttpServletResponse response) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        curUser.setMobile(editInfoDto.getMobile());
        curUser.setEmail(editInfoDto.getEmail());
        curUser.setRemark(editInfoDto.getRemark());

        userService.saveOrUpdate(curUser);
        return Result.succ(null);
    }

    @RequiresAuthentication
    @PostMapping("/pwdEdit")
    public Result pwdEdit(@Validated @RequestBody ChangePwdDto changePwdDto, HttpServletResponse response) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        Md5Util md5 = new Md5Util();
        String oldPwd = changePwdDto.getCurPassword();
        if(!curUser.getPassword().equals(md5.getMD5String(oldPwd)))
            return Result.fail("旧密码不正确");

        String newPwd = changePwdDto.getNewPassword();
        String confirmNewPwd = changePwdDto.getConfirmNewPwd();
        if(!newPwd.equals(confirmNewPwd))
            return Result.fail("新密码不匹配");

        int pwdStr = CheckPwdStr.getPwdStrength(newPwd);
        if (pwdStr < 0)
            return Result.fail("密码长度不合适");
        else if (pwdStr < 1)
            return Result.fail("密码强度弱");

        curUser.setPassword(md5.getMD5String(newPwd));
        userService.saveOrUpdate(curUser);
        return Result.succ(null);
    }

    @GetMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        return Result.succ("logged out.");
    }

    //根据id返回状态
    @GetMapping("/getStatus")
    public Result getStatus(@RequestParam Long userId) {
        Users user = userService.getOne(new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(user.getStatus());
    }
    //根据id返回姓名
    @GetMapping("/getName")
    public Result getName(@RequestParam Long userId) {
        Users user = userService.getOne(new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(user.getName());
    }

    //根据id返回用户类型
    @GetMapping("/getType")
    public Result getType(@RequestParam Long userId) {
        Users user = userService.getOne(new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(user.getType());
    }

    //通过注册
    @RequiresAuthentication
    @GetMapping("/agreeReg")
    public Result agreeReg(@RequestParam Long userId) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(!curUser.getType().equals("4"))
            return Result.fail("没有权限");
        Users targetUser=userService.getOne(new QueryWrapper<Users>().eq("id",userId));
        targetUser.setStatus("1");
        userService.update(targetUser,new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(null);
    }

    //拒绝注册
    @RequiresAuthentication
    @GetMapping("/denyReg")
    public Result denyReg(@RequestParam Long userId) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(!curUser.getType().equals("3"))
            return Result.fail("没有权限");
        userService.remove(new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(null);
    }

    //封禁用户
    @RequiresAuthentication
    @GetMapping("/freeze")
    public Result freeze(@RequestParam Long userId) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(!curUser.getType().equals("3"))
            return Result.fail("没有权限");
        Users targetUser = userService.getOne(new QueryWrapper<Users>().eq("id",userId));
        targetUser.setStatus("0");
        userService.update(targetUser,new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(null);
    }

    //解封用户
    @RequiresAuthentication
    @GetMapping("/unfreeze")
    public Result unfreeze(@RequestParam Long userId) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(!curUser.getType().equals("3"))
            return Result.fail("没有权限");
        Users targetUser = userService.getOne(new QueryWrapper<Users>().eq("id",userId));
        targetUser.setStatus("1");
        userService.update(targetUser,new QueryWrapper<Users>().eq("id",userId));
        return Result.succ(null);
    }


    //获取用户列表
    @RequiresAuthentication
    @GetMapping("/list")
    public Result userList(@RequestParam(defaultValue = "1") Integer currentPage) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= userService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(!curUser.getType().equals("4"))
            return Result.fail("没有权限");
        Page page = new Page(currentPage,15);
        IPage pageData = userService.page(page,new QueryWrapper<Users>().ne("type","4").orderByDesc("username"));
        return Result.succ(pageData);
    }
}
