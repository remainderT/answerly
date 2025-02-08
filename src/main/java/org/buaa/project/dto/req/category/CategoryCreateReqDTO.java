package org.buaa.project.dto.req.category;

import lombok.Data;

/**
 * 创建分类请求参数
 */
@Data
public class CategoryCreateReqDTO {

    /**
    * 分类名
    */
    private String name;

    /**
    * 图像
    */
    private String image;

    /**
     * 排序
     */
    private Integer sort;

}
