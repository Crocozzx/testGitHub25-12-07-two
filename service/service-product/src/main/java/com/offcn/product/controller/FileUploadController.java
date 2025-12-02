package com.offcn.product.controller;

import com.offcn.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/product")
public class FileUploadController {
    @Value("${fileServer.url}")
    private String URL;
    @RequestMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception{
        //通过file读取配置文件
        String file1 = this.getClass().getResource("/tracker.conf").getFile();
        String path = "";
        if (file1 != null){
            //初始化
            ClientGlobal.init(file1);
            //获取trackerServer地址,通过TrackerClient进行连接,创建对象  连接server使用client来连接
            //获取TrackerClient
            TrackerClient t = new TrackerClient();
            //连接到trackerServer
            TrackerServer connection = t.getConnection();
            //存储的客户端存入存储的服务器端  storageClient->storageServer
            StorageClient1 storageClient1 = new StorageClient1(connection,null);
            //参数说明，connection获得就是tracker给client返回的storageserver的ip+port找到分布式，然后通过负载均衡访问
            //null：他是让我们传入一个storageserver的客户端，因为文件是通过tracker来进行选择storageserver的所以不传入
            //通过storage客户端存入文件到nginx中
            path =  storageClient1.upload_appender_file1(file.getBytes(),//将文件转换成Byte数组
                    FilenameUtils.getExtension(file.getOriginalFilename()),null);
            // FilenameUtils.getExtension(file.getOriginalFilename())获取改文件的扩展名
            //file.getOriginalFilename()是获取文件的全名称
            //null这里是让我们传入文件其他信息，所以不传入
            //返回值path是nginx中的相对路径,所以要拼接上nginx的ip+port
        }
        return Result.ok(this.URL+path);
    }
}
