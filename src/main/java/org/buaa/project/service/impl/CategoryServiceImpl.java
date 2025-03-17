package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.dao.entity.CategoryDO;
import org.buaa.project.dao.mapper.CategoryMapper;
import org.buaa.project.dto.req.category.CategoryCreateReqDTO;
import org.buaa.project.dto.req.category.CategoryUpdateReqDTO;
import org.buaa.project.dto.resp.CategoryRespDTO;
import org.buaa.project.service.CategoryService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.buaa.project.common.consts.RedisCacheConstants.CATEGORY_CONTENT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.CATEGORY_LOCK_KEY;
import static org.buaa.project.common.enums.QAErrorCodeEnum.CATEGORY_ACCESS_CONTROL_ERROR;

/**
 * 主题类别接口实现层
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryDO> implements CategoryService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    @Override
    public void addCategory(CategoryCreateReqDTO requestParam) {
        checkIsAdmin();
        CategoryDO categoryDO = BeanUtil.toBean(requestParam, CategoryDO.class);
        baseMapper.insert(categoryDO);
        stringRedisTemplate.opsForSet().add(CATEGORY_CONTENT_KEY, JSONUtil.toJsonStr(categoryDO));
    }

    @Override
    public void deleteCategory(Long id) {
        checkIsAdmin();
        LambdaUpdateWrapper<CategoryDO> queryWrapper = Wrappers.lambdaUpdate(CategoryDO.class)
                .eq(CategoryDO::getId, id);
        CategoryDO categoryDO = new CategoryDO();
        categoryDO.setDelFlag(1);
        baseMapper.update(categoryDO, queryWrapper);
        stringRedisTemplate.opsForSet().remove(CATEGORY_CONTENT_KEY, JSONUtil.toJsonStr(categoryDO));
    }

    @Override
    public void updateCategory(CategoryUpdateReqDTO requestParam) {
        checkIsAdmin();
        LambdaUpdateWrapper<CategoryDO> queryWrapper = Wrappers.lambdaUpdate(CategoryDO.class)
                .eq(CategoryDO::getId, requestParam.getId());
        CategoryDO categoryDO = BeanUtil.toBean(requestParam, CategoryDO.class);
        stringRedisTemplate.opsForSet().remove(CATEGORY_CONTENT_KEY, JSONUtil.toJsonStr(categoryDO));
        baseMapper.update(categoryDO, queryWrapper);
        stringRedisTemplate.opsForSet().add(CATEGORY_CONTENT_KEY, JSONUtil.toJsonStr(categoryDO));
    }

    @Override
    public List<CategoryRespDTO> listCategory() {
        Set<String> categorys = stringRedisTemplate.opsForSet().members(CATEGORY_CONTENT_KEY);
        if (Objects.isNull(categorys) || categorys.isEmpty()) {
            RLock lock = redissonClient.getLock(CATEGORY_LOCK_KEY);
            lock.lock();
            try {
                categorys = stringRedisTemplate.opsForSet().members(CATEGORY_CONTENT_KEY);
                if (Objects.isNull(categorys) || categorys.isEmpty()) {
                    baseMapper.selectList(null).forEach(
                            category -> stringRedisTemplate.opsForSet().add(CATEGORY_CONTENT_KEY, JSONUtil.toJsonStr(category)));
                }
            } finally {
                lock.unlock();
            }
        }
        return categorys.stream()
                .map(json -> JSONUtil.toBean(json, CategoryRespDTO.class))
                .collect(Collectors.toList());
    }

    private void checkIsAdmin(){
        if(!UserContext.getUserType().equals("admin")){
            throw new ClientException(CATEGORY_ACCESS_CONTROL_ERROR);
        }
    }
}
