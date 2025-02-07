package org.buaa.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dto.req.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.QuestionCollectReqDTO;
import org.buaa.project.dto.req.QuestionLikeReqDTO;
import org.buaa.project.dto.req.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.QuestionPageReqDTO;
import org.buaa.project.dto.req.QuestionRecentPageReqDTO;
import org.buaa.project.dto.req.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;

import java.util.List;

/**
 * 问题接口层
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
     *
     * @param id
     */
    void deleteQuestion(Long id);

    /**
     * 点赞题目
     */
    void likeQuestion(QuestionLikeReqDTO requestParam);

    /**
     * 标记问题已经解决
     */
    void resolvedQuestion(Long id);

    /**
     * 查询题目详细信息
     */
    QuestionRespDTO findQuestionById(Long id);

    /**
     * 分页查询题目
     */
    IPage<QuestionPageRespDTO> pageQuestion(QuestionPageReqDTO requestParam);

    /**
     * 查询热门题目
     */
    List<QuestionPageRespDTO> findHotQuestion(Integer category);

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
     * 收藏问题
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

}
