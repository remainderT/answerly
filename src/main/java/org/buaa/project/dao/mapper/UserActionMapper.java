package org.buaa.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.buaa.project.dao.entity.UserActionDO;
import org.buaa.project.dto.req.question.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.question.QuestionRecentPageReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;

/**
 * 用户行为持久层
 */
public interface UserActionMapper extends BaseMapper<UserActionDO> {

    /**
     * 分页查询用户收藏的问题
     */
    @Select("SELECT q.id, q.category_id, q.title, q.content, q.user_id, q.username, q.images, " +
            "q.view_count, q.like_count, q.comment_count, q.solved_flag, q.create_time, q.update_time " +
            "FROM user_action ua " +
            "JOIN question q ON ua.entity_id = q.id " +
            "WHERE ua.user_id = #{userId} " +
            "AND ua.entity_type = 0 " +
            "AND ua.del_flag = 0 " +
            "AND ua.collect_stat = 1 " +
            "AND q.del_flag = 0 " +
            "ORDER BY q.create_time DESC")
    IPage<QuestionPageRespDTO> pageCollectQuestion(Page<?> page,
                                                   @Param("userId") Long userId,
                                                   @Param("requestParam") QuestionCollectPageReqDTO requestParam);


    /**
     * 分页查询最近浏览的问题
     */
    @Select("SELECT q.id, q.category_id, q.title, q.content, q.user_id, q.username, q.images, " +
            "q.view_count, q.like_count, q.comment_count, q.solved_flag, q.create_time, q.update_time " +
            "FROM user_action ua " +
            "JOIN question q ON ua.entity_id = q.id " +
            "WHERE ua.user_id = #{userId} " +
            "AND ua.entity_type = 'question' " +
            "AND ua.del_flag = 0 " +
            "AND q.del_flag = 0 " +
            "ORDER BY ua.last_view_time DESC")
    IPage<QuestionPageRespDTO> pageRecentViewQuestion(Page<?> page,
                                                      @Param("userId") Long userId,
                                                      @Param("requestParam") QuestionRecentPageReqDTO requestParam);
}
