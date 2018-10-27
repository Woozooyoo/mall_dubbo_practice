package com.atguigu.gmall1129.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 *把图片的存储 和应用程序 和浏览器 前台业务完全分开
 */
@Controller
public class FileUploadController {

    @PostMapping("/fileUpload")
    @ResponseBody
    public String upload(MultipartFile file) throws IOException, MyException {
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(configFile);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, null);

        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
	    //截取最后一个点后面的 文件类型名
        String extFilename = StringUtils.substringAfter(originalFilename, ".");


        String[] upload_file = storageClient.upload_file(bytes, extFilename, null);
        String imgUrl="http://file.gmall.com";
        for (int i = 0; i < upload_file.length; i++) {
            String uploadFilePath = upload_file[i];
            imgUrl+="/"+uploadFilePath;
        }
        System.out.println("imgUrl = " + imgUrl);
        return imgUrl;
    }

    @Test
    public void uploadTest () throws IOException, MyException {
    	//读到配置文件 从classpath
        String file = this.getClass().getResource("/tracker.conf").getFile();
        //对文件初始化
        ClientGlobal.init(file);
        //new可以读到这个文件
        TrackerClient trackerClient = new TrackerClient();
        //通过tracker客户端 找tracker服务器
        TrackerServer trackerServer = trackerClient.getConnection();
        //tracker服务器 找storage客户端
        StorageClient storageClient = new StorageClient(trackerServer, null);

		//storage客户端可以上传 上传结果就是 访问路径名的String
        String[] upload_file = storageClient.upload_file("C:\\Users\\Adrian\\Pictures\\小米黑\\黑正背.jpg", "jpg", null);
        String url="http://file.gmall.com";
        for (int i = 0; i < upload_file.length; i++) {
            url+="/"+upload_file[i];
        }
        System.out.println(url);

    }

}
