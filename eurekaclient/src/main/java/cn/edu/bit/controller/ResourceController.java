package cn.edu.bit.controller;


import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.Users;
import cn.edu.bit.service.ResourceService;
import cn.edu.bit.service.UsersService;
import cn.edu.bit.utils.ShiroUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;


@RestController
@RequestMapping("/resource")
public class ResourceController {

    @Autowired
    ResourceService resourceService;
    @Autowired
    UsersService usersService;

    //查询接口
    @RequiresAuthentication
    @GetMapping("/resource")
    public Result resourceList() {
        String path="files"+File.separator;
        ArrayList<String> filesName=new ArrayList<>();
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
        if (fs==null)
            return Result.succ(filesName);
        for (File f : fs) {
            if (!f.isDirectory())
                filesName.add(f.getName());
        }
        return Result.succ(filesName);
    }

    //下载接口
    @RequiresAuthentication
    @GetMapping(value = "/download")
    public void resourceDownload(@RequestParam String name, HttpServletResponse response) throws Exception {
        this.doDownload(name,response);
    }

    //删除接口
    @RequiresAuthentication
    @GetMapping(value = "/delete")
    public Result resourceDelete(@RequestParam String name) {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= usersService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if (!curUser.getType().equals("4"))
            return Result.fail("没有权限");
        String path="files"+File.separator+name;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return Result.succ("删除成功");
            } else {
                return Result.fail("删除失败");
            }
        } else {
            return Result.fail("文件不存在");
        }
    }

    //上传接口
    @RequiresAuthentication
    @GetMapping(value = "/release")
    public Result resourceRelease(@RequestParam("file") MultipartFile file) throws Exception {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= usersService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if(curUser.getType().equals("4")) {

            return this.doUpload(file);
        }
        else return Result.fail("用户权限不符");
    }

    //下载文件
    @RequiresAuthentication
    public void doDownload(String name, HttpServletResponse response) throws Exception {
        File file = new File("files" + File.separator + name);

        if (!file.exists()) {
            throw new Exception("文件不存在");
        }
        String fileName = new String(name.getBytes("UTF-8"), "iso-8859-1");
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);

        byte[] buffer = new byte[1024];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        }
    }

    //上传文件
    @RequiresAuthentication
    public Result doUpload(MultipartFile file) throws Exception {
        int curUserId = ShiroUtil.getProfile().getId();
        Users curUser= usersService.getOne(new QueryWrapper<Users>().eq("id",curUserId));
        if (!curUser.getType().equals("4"))
            return Result.fail("用户没有权限");

        if (file == null || file.isEmpty())
            return Result.fail("文件为空");

        String filePath = new File("files" + File.separator).getAbsolutePath();
        File fileUpload = new File(filePath);
        if (!fileUpload.exists()) {
            fileUpload.mkdirs();
        }
        fileUpload = new File(filePath, file.getOriginalFilename());
        if (fileUpload.exists()) {
            return Result.fail("已存在同名文件");
        }
        try {
            file.transferTo(fileUpload);
            return Result.succ(null);
        } catch (IOException e) {
            return Result.fail("上传失败，请重试");
        }
    }

}
