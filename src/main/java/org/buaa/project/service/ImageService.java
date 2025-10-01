package org.buaa.project.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片接口层
 */
public interface ImageService {

    /**
     * cos上传图片
     */
    String cosUploadImage(MultipartFile file);

    /**
     * 生成验证码用于显示，并把结果存入session中.
     */
    void getCaptcha(HttpServletResponse response);

}
