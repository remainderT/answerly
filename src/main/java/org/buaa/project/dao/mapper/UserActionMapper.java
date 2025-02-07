package org.buaa.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.buaa.project.dao.entity.UserActionDO;
import org.buaa.project.dto.req.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.QuestionRecentPageReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;

/**
 * 用户行为持久层
 */
public interface UserActionMapper extends BaseMapper<UserActionDO> {

    /**
     * 收藏/取消收藏 问题
     */
    @Update("UPDATE user_action SET collection_stat = #{isCollect}, update_time = NOW()" +
            "WHERE question_id = #{questionId} AND user_id = #{userId}")
    void collectQuestion(@Param("questionId") Long questionId,
                         @Param("userId") Long userId,
                         @Param("isCollect") int isCollect);

    /**
     * 更新用户最后查看问题时间（不存在则插入）
     */
    @Insert("INSERT INTO user_action (user_id, question_id, last_view_time, create_time, update_time, del_flag) " +
            "VALUES (#{userId}, #{questionId}, NOW(), NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE last_view_time = NOW(), update_time = NOW()")
    void updateLastViewTime(@Param("userId") Long userId,
                            @Param("questionId") Long questionId);


    /**
     * 分页查询用户收藏的问题
     */
    @Select("SELECT q.id, q.category_id, q.title, q.content, q.user_id, q.username, q.images, " +
            "q.view_count, q.like_count, q.comment_count, q.solved_flag, q.create_time, q.update_time " +
            "FROM user_action ua " +
            "JOIN question q ON ua.question_id = q.id " +
            "WHERE ua.user_id = #{userId} " +
            "AND ua.del_flag = 0 " +
            "AND ua.collection_stat = 1 " +
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
            "JOIN question q ON ua.question_id = q.id " +
            "WHERE ua.user_id = #{userId} " +
            "AND ua.del_flag = 0 " +
            "AND q.del_flag = 0 " +
            "ORDER BY ua.last_view_time DESC")
    IPage<QuestionPageRespDTO> pageRecentViewQuestion(Page<?> page,
                                                      @Param("userId") Long userId,
                                                      @Param("requestParam") QuestionRecentPageReqDTO requestParam);
}
