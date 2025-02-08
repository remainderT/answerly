package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.CategoryDO;
import org.buaa.project.dto.req.category.CategoryCreateReqDTO;
import org.buaa.project.dto.req.category.CategoryUpdateReqDTO;
import org.buaa.project.dto.resp.CategoryRespDTO;

import java.util.List;

/**
 * 主题类别接口层
 */
public interface CategoryService extends IService<CategoryDO> {

    /**
     * 增加主题
     */
    void addCategory(CategoryCreateReqDTO requestParam);

    /**
     * 删除主题
     */
    void deleteCategory(Long id);

    /**
     * 修改主题
     */
    void updateCategory(CategoryUpdateReqDTO requestParam);

    /**
     * 查询所有主题
     */
    List<CategoryRespDTO> listCategory();

}
