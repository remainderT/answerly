package org.buaa.project.toolkit.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.dao.entity.CategoryDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.CategoryMapper;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.buaa.project.common.consts.RedisCacheConstants.HOT_QUESTION_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;
import static org.buaa.project.common.consts.SystemConstants.COLLECT_WEIGHT;
import static org.buaa.project.common.consts.SystemConstants.COMMENT_WEIGHT;
import static org.buaa.project.common.consts.SystemConstants.LIKE_WEIGHT;
import static org.buaa.project.common.consts.SystemConstants.VIEW_WEIGHT;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotQuestion {

    private final StringRedisTemplate stringRedisTemplate;

    private final QuestionMapper questionMapper;

    private final CategoryMapper categoryMapper;


    @XxlJob("hotQuestion")
    public void hotQuestion() {
        log.info("执行定时任务,执行时间:{}", new Date());

        List<CategoryDO> categorys = categoryMapper.selectList(null);

        for (CategoryDO category : categorys) {
            List<QuestionDO> questions = questionMapper.selectList(new LambdaQueryWrapper<>(QuestionDO.class)
                    .eq(QuestionDO::getCategoryId, category.getId()));

            for (QuestionDO question : questions) {
                Map<Object, Object> countData = stringRedisTemplate.opsForHash().entries(QUESTION_COUNT_KEY + question.getId());

                int viewCount = Integer.parseInt(countData.getOrDefault("view", "0").toString());
                int likeCount = Integer.parseInt(countData.getOrDefault("like", "0").toString());
                int collectCount = Integer.parseInt(countData.getOrDefault("collect", "0").toString());
                int commentCount = Integer.parseInt(countData.getOrDefault("comment", "0").toString());

                // 获取文章的发布时间和更新时间
                Date createDate = question.getCreateTime();
                Date updateDate = question.getUpdateTime();

                // 计算文章的年龄和更新时间差
                long age = (new Date().getTime() - createDate.getTime()) / (1000 * 3600 * 24 * 7);
                long updateAge = (new Date().getTime() - updateDate.getTime()) / (1000 * 3600 * 24 * 7);

                // 计算文章的热度分值
                double score = Math.log(viewCount + 1) * VIEW_WEIGHT
                        + likeCount * LIKE_WEIGHT
                        + collectCount * COLLECT_WEIGHT
                        + Math.log(commentCount + 1) * COMMENT_WEIGHT
                        / (1 + age / 2.0 + updateAge / 2.0);

                stringRedisTemplate.opsForZSet().add(HOT_QUESTION_KEY + category.getId(), question.getId().toString(), score);
            }

            // 获取前 10 名文章
            Set<String> topQuestions = stringRedisTemplate.opsForZSet().reverseRange(HOT_QUESTION_KEY + category.getId(), 0, 9);

            log.info("当前分类：{}，热门文章：{}", category.getName(), topQuestions);
        }

    }
}
