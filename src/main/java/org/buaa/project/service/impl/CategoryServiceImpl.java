package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.dao.entity.CategoryDO;
import org.buaa.project.dao.mapper.CategoryMapper;
import org.buaa.project.dto.req.CategoryCreateReqDTO;
import org.buaa.project.dto.req.CategoryUpdateReqDTO;
import org.buaa.project.dto.resp.CategoryRespDTO;
import org.buaa.project.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.buaa.project.common.enums.QAErrorCodeEnum.CATEGORY_ACCESS_CONTROL_ERROR;

/**
 * 主题类别接口实现层
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryDO> implements CategoryService {

    @Override
    public void addCategory(CategoryCreateReqDTO requestParam) {
        checkIsAdmin();
        CategoryDO categoryDO = BeanUtil.toBean(requestParam, CategoryDO.class);
        baseMapper.insert(categoryDO);
    }

    @Override
    public void deleteCategory(Long id) {
        checkIsAdmin();
        LambdaUpdateWrapper<CategoryDO> queryWrapper = Wrappers.lambdaUpdate(CategoryDO.class)
                .eq(CategoryDO::getId, id);
        CategoryDO categoryDO = new CategoryDO();
        categoryDO.setDelFlag(1);
        baseMapper.update(categoryDO, queryWrapper);
    }

    @Override
    public void updateCategory(CategoryUpdateReqDTO requestParam) {
        checkIsAdmin();
        LambdaUpdateWrapper<CategoryDO> queryWrapper = Wrappers.lambdaUpdate(CategoryDO.class)
                .eq(CategoryDO::getId, requestParam.getId());
        CategoryDO categoryDO = BeanUtil.toBean(requestParam, CategoryDO.class);
        baseMapper.update(categoryDO, queryWrapper);
    }

    @Override
    public List<CategoryRespDTO> listCategory() {
        LambdaQueryWrapper<CategoryDO> queryWrapper = Wrappers.lambdaQuery(CategoryDO.class)
                .eq(CategoryDO::getDelFlag, 0)
                .orderByAsc(CategoryDO::getSort, CategoryDO::getUpdateTime);
        List<CategoryDO> groupDOList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groupDOList, CategoryRespDTO.class);
    }

    private void checkIsAdmin(){
        if(!UserContext.getUserType().equals("admin")){
            throw new ClientException(CATEGORY_ACCESS_CONTROL_ERROR);
        }
    }
}
