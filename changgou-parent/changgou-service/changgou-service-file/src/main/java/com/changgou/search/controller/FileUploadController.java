package com.changgou.search.controller;

import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSUtil;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;


/**
 * @author caijia
 * @Date 2020年11月20日 16:20:00
 */
@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController{

    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) throws IOException{

        FastDFSFile dfsFile = new FastDFSFile(
                file.getOriginalFilename(),
                file.getBytes(),
                StringUtils.getFilenameExtension(file.getOriginalFilename())
        );
        String[] strings = FastDFSUtil.upload(dfsFile);
        String returnUrl = "http://192.168.119.128:8080/"+strings[0]+"/"+strings[1];

        return new Result(true, StatusCode.OK, "上传成功",returnUrl);
    }
}
