package cn.edu.bit.controller;

import cn.edu.bit.common.lang.Result;
import cn.edu.bit.entity.Course;
import cn.edu.bit.entity.StudentCourse;
import cn.edu.bit.entity.User;
import cn.edu.bit.service.CourseService;
import cn.edu.bit.service.StudentCourseService;
import cn.edu.bit.service.UserService;
import cn.edu.bit.utils.ShiroUtil;
import cn.edu.bit.utils.ZipUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Scope("prototype")
@RequestMapping("/file")
public class FileController {


    @Autowired
    private UserService userService;
    @Autowired
    private StudentCourseService studentCourseService;
    @Autowired
    private CourseService courseService;


    //下载接口
    @GetMapping(value = "/download")
    public void fileDownload(@RequestParam String type,
                             @RequestParam String courseName,
                             @RequestParam String week,
                             @RequestParam String name,
                             HttpServletResponse response) throws Exception {
        this.doDownload(type,courseName,week,name,response);
    }

    @GetMapping(value = "/downloadAssign")
    public void assignDownload(@RequestParam String url,
                               @RequestParam String name,
                               HttpServletResponse response) throws Exception {
        File file = new File(url);

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

    //删除接口
    @GetMapping(value = "/delete")
    public Result fileDelete(@RequestParam String type,
                             @RequestParam String courseName,
                             @RequestParam String week,
                             @RequestParam String name,
                             HttpServletResponse response) throws Exception {
        String path="files"+File.separator+type+File.separator+courseName+File.separator+week+File.separator+name;
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

    @RequiresAuthentication
    @GetMapping(value = "/deleteAssign")
    public Result assignDelete(@RequestParam String type,
                               @RequestParam String courseName,
                               @RequestParam String week,
                               @RequestParam String name) throws Exception {
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        if(curUser.getType()!='1')
            return Result.fail("没有权限");
        String path="files"+File.separator+type+File.separator+courseName+File.separator+
                week+File.separator+curUser.getUsername()+File.separator+name;
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

    //查询接口
    @GetMapping(value = "/getFiles")
    public Result getFiles(@RequestParam String type,
                           @RequestParam String courseName,
                           @RequestParam String week,
                           HttpServletResponse response) throws Exception {
        String path="files"+File.separator+type+File.separator+courseName+File.separator+week;
        ArrayList<String> filesName=new ArrayList<String>();
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
        if (fs==null)
            return Result.succ(filesName);
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory()) {       //若非目录(即文件)，则打印
                filesName.add(f.getName());
            }
        }
        return Result.succ(filesName);
    }

    @RequiresAuthentication
    @GetMapping(value = "/getAssign")
    public Result getAssign(@RequestParam String type,
                            @RequestParam String courseName,
                            @RequestParam String week,
                            HttpServletResponse response) throws Exception {
        int curUserId = ShiroUtil.getProfile().getId();
        User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
        String path="files"+File.separator+type+File.separator+courseName+File.separator+week+File.separator+curUser.getUsername();
        ArrayList<Map> files=new ArrayList<Map>();
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
        if (fs==null)
            return Result.succ(files);
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory()) {       //若非目录(即文件)，则打印
                Map map=new LinkedHashMap();
                String fPath=f.getPath().replaceAll("\\\\","/");
                map.put("name",f.getName());
                map.put("url",fPath);
                files.add(map);
            }
        }

        return Result.succ(files);
    }

    @RequiresAuthentication
    @GetMapping(value = "/getAllAssign")
    public Result getAllAssign(@RequestParam String type,
                               @RequestParam String courseName,
                               @RequestParam String week,
                               HttpServletResponse response) throws Exception {
        String path="files"+File.separator+type+File.separator+courseName+File.separator+week;
        System.out.println(path);
        ArrayList<Map> files=new ArrayList<Map>();
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中

        if (fs==null)
            return Result.succ(files);
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory()) {       //若非目录(即文件)，则打印
                Map map=new LinkedHashMap();
                String fPath=f.getPath().replaceAll("\\\\","/");
                map.put("name",f.getName());
                map.put("url",fPath);
                files.add(map);
            }else{
                File dir = new File(f.getPath());
                File[] fs1 = dir.listFiles();
                for(File f1:fs1){
                    Map map=new LinkedHashMap();
                    String fPath=f1.getPath().replaceAll("\\\\","/");
                    map.put("name",f1.getName());
                    map.put("url",fPath);
                    files.add(map);
                }
            }
        }
        return Result.succ(files);
    }

    //上传接口
    @RequiresAuthentication
    @PostMapping(value = "/upload")
    public Result fileUpload(@RequestParam("file") MultipartFile file,
                             @RequestParam String type,
                             @RequestParam String courseName,
                             @RequestParam String week) throws Exception {
        String curUsername = ShiroUtil.getProfile().getUsername();
        User curUser =  userService.getOne(new QueryWrapper<User>().eq("username", curUsername));
        if(curUser.getType() == '1') //学生，只上传作业文件
        {
            Course curCourse = courseService.getOne(new QueryWrapper<Course>().eq("course_info",courseName));
            int curWeek = Integer.parseInt(week);
            StudentCourse curStudentCourse = studentCourseService.getOne(new QueryWrapper<StudentCourse>()
                    .eq("course_id",curCourse.getId())
                    .eq("student_id",curUser.getId())
                    .eq("course_week",curWeek));
            if(curStudentCourse == null){
                StudentCourse newstudentcourse = new StudentCourse();
                newstudentcourse.setCourseId(curCourse.getId());
                newstudentcourse.setStudentId(curUser.getId());
                newstudentcourse.setCourseWeek(curWeek);
                String filePath = file.getOriginalFilename();
                newstudentcourse.setUrl(filePath);

                studentCourseService.save(newstudentcourse);
                return this.doUpload(file,type,courseName,week);
            }
            else{ //已有文件，要先删除再添加新的
                String oldfilePath = curStudentCourse.getUrl();

                assignDelete(type,courseName,week,oldfilePath);

                curStudentCourse.setUrl(file.getOriginalFilename());

                studentCourseService.update(curStudentCourse,new QueryWrapper<StudentCourse>()
                        .eq("course_id",curCourse.getId())
                        .eq("student_id",curUser.getId())
                        .eq("course_week",curWeek));

                return this.doUpload(file,type,courseName,week);
            }

        }
        else if(curUser.getType() == '2')
        {
            return this.doUpload(file,type,courseName,week);
        }
        else return Result.fail("用户权限不对");

    }

    @GetMapping(value = "/downloadAllAssign")
    public Result downloadAllAssign(@RequestParam String type,
                                    @RequestParam String courseName,
                                    @RequestParam String week,
                                    HttpServletResponse response) throws Exception {
        String path="files"+File.separator+type+File.separator+courseName+File.separator+week;
        String zipPath="files"+File.separator+type+File.separator+courseName;
        System.out.println(path+"\n"+zipPath);
        ZipUtil.compressToZip(path,zipPath,"download.zip");

        File file = new File(zipPath+File.separator+"download.zip");

        if (!file.exists()) {
            throw new Exception("文件不存在");
        }
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=download.zip");

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

        file.delete();
        return Result.succ(null);
    }


    //下载文件
    public void doDownload(String type, String courseName, String week, String name, HttpServletResponse response) throws Exception {
        File file = new File("files" + File.separator +
                type + File.separator +
                courseName + File.separator +
                week + File.separator + name);

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
    public Result doUpload(MultipartFile file, String type, String courseName, String week) throws Exception {
        if (type.equals("handout")) {
            if (file == null || file.isEmpty()) {
                return Result.fail("文件为空");
            }

            String filePath = new File("files" + File.separator +
                    type + File.separator +
                    courseName + File.separator +
                    week + File.separator).getAbsolutePath();
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
        }else{
            int curUserId = ShiroUtil.getProfile().getId();
            User curUser= userService.getOne(new QueryWrapper<User>().eq("id",curUserId));
            if (file == null || file.isEmpty())
                return Result.fail("文件为空");
            String filePath = new File("files" + File.separator +
                    type + File.separator +
                    courseName + File.separator +
                    week + File.separator+curUser.getUsername()+File.separator).getAbsolutePath();

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
}

