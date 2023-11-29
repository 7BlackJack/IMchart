package com.imchat.server.controller;

import cn.hutool.crypto.SecureUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/28
 */
@Controller
public class FileController {
    @PostMapping(value = "/upload/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String,Object> uploadAudio(HttpServletRequest request, @RequestBody MultipartFile audio) throws IOException {
        String fileName = SecureUtil.md5(UUID.randomUUID().toString()); // 保存的文件名
        String uploadPath = System.getProperty("user.dir") + "/file/upload/"; // 文件保存路径

        File file = new File(uploadPath + fileName + ".webm");
        FileOutputStream outputStream = new FileOutputStream(file);

        FileCopyUtils.copy(audio.getInputStream(), outputStream);
        outputStream.close();

        Map<String,Object> map = new HashMap<>(3);
        map.put("code",200);
        map.put("message","success");
        Map<String,Object> dataMap = new HashMap<>(2);
        dataMap.put("url",request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/audio/" + fileName);
        dataMap.put("file",fileName);
        map.put("data",dataMap);
        return map;
    }

    @GetMapping(value = "/audio/{name}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> getAudio(@PathVariable String name) {
        String filePath = System.getProperty("user.dir") + "/file/upload/" + name + ".webm"; // 音频文件路径

        FileSystemResource file = new FileSystemResource(filePath);
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=" + file.getFilename())
                .body(file);
    }
}
