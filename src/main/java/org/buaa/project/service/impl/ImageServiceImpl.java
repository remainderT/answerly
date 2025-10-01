package org.buaa.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Producer;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.buaa.project.common.consts.RedisCacheConstants.USER_LOGIN_CAPTCHA_KEY;
import static org.buaa.project.common.enums.ServiceErrorCodeEnum.IMAGE_UPLOAD_ERROR;

/**
 * 图片接口实现层
 */
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final Producer kapchaProducer;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    private COSClient cosClient;

    @Value("${tencent.cos.bucketName}")
    private String bucketName;

    public String cosUploadImage(MultipartFile file){
        try (InputStream inputStream = file.getInputStream()) {
            String key = createNewFileName(file.getOriginalFilename());
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            cosClient.putObject(putObjectRequest);
            return key;
        } catch (Exception e) {
            throw new ServiceException("COS上传异常: " + e.getMessage());
        }
    }

    private String createNewFileName(String originalFilename) {
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        String name = UUID.randomUUID().toString();
        String datePath = java.time.LocalDate.now().toString().replace("-", "/");
        return StrUtil.format("{}/{}.{}", datePath, name, suffix);
    }

    @Override
    public void getCaptcha(HttpServletResponse response) {
        String text = kapchaProducer.createText();
        BufferedImage image = kapchaProducer.createImage(text);

        String CaptchaOwner = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("CaptchaOwner", CaptchaOwner);
        cookie.setMaxAge(60);
        response.addCookie(cookie);

        String redisKey = USER_LOGIN_CAPTCHA_KEY + CaptchaOwner;
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            throw new ServiceException(IMAGE_UPLOAD_ERROR);
        }
    }

}
