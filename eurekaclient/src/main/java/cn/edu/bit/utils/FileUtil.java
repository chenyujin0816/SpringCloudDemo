package cn.edu.bit.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class FileUtil {

    // 读取本地文件获取输入流
    public static FileInputStream readFile(String path) throws IOException {
        return new FileInputStream(new File(path));
    }
    public static File multipartFile2File(MultipartFile multipartFile){
        File file = null;
        if (multipartFile != null){
            try {
                file=File.createTempFile("tmp", null);
                multipartFile.transferTo(file);
                System.gc();
                file.deleteOnExit();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("multipartFile转File发生异常："+e);
            }
        }
        return file;
    }


    // 读取表中图片获取输出流
    public static void readBin2File(InputStream in, String targetPath) {
        File file = new File(targetPath);
        String path = targetPath.substring(0, targetPath.lastIndexOf("/"));
        if (!file.exists()) {
            new File(path).mkdir();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}