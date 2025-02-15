package org.buaa.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dto.req.question.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.question.QuestionCollectReqDTO;
import org.buaa.project.dto.req.question.QuestionLikeReqDTO;
import org.buaa.project.dto.req.question.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.question.QuestionRecentPageReqDTO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;

import java.util.List;

/**
 * 题目接口层
 */
public interface QuestionService extends IService<QuestionDO> {

    /**
     * 上传题目
     */
    void uploadQuestion(QuestionUploadReqDTO requestParam);


    /**
     * 修改题目
     */
    void updateQuestion(QuestionUpdateReqDTO requestParam);

    /**
     * 删除题目
     */
    void deleteQuestion(Long id);

    /**
     * 点赞题目
     */
    void likeQuestion(QuestionLikeReqDTO requestParam);

    /**
     * 标记题目已经解决
     */
    void resolvedQuestion(QuestionSolveReqDTO requestParam);

    /**
     * 查询题目详细信息
     */
    QuestionRespDTO findQuestionById(Long id);

    /**
     * 查询热门题目
     */
    List<QuestionPageRespDTO> findHotQuestion(Long categoryId);

    /**
     * 检查题目是否存在
     */
    void checkQuestionExist(Long id);

    /**
     * 检查题目是否为当前用户所有
     */
    void checkQuestionOwner(Long id);

    /**
     * 分页查询我的题目
     */
    IPage<QuestionPageRespDTO> pageMyQuestion(QuestionMinePageReqDTO requestParam);

    /**
     * 收藏题目
     */
    void collectQuestion(QuestionCollectReqDTO requestParam);

    /**
     * 分页查询我收藏的题目
     */
    IPage<QuestionPageRespDTO> pageCollectQuestion(QuestionCollectPageReqDTO requestParam);

    /**
     * 分页查询最近浏览的题目
     */
    IPage<QuestionPageRespDTO> pageRecentViewQuestion(QuestionRecentPageReqDTO requestParam);

    /**
     * 根据题目id得到用户id
     */
    Long getUserIdByQuestionId(Long questionId);

}
