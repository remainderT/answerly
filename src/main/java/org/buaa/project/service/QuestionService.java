package org.buaa.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dto.req.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.QuestionPageReqDTO;
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
     *
     * @param requestParam
     */
    void uploadQuestion(QuestionUploadReqDTO requestParam);


    /**
     * 修改题目
     *
     * @param requestParam
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
     *
     * @param id
     */
    void likeQuestion(Long id, Long entityUserId);

    /**
     * 标记问题已经解决
     *
     * @param id
     */
    void resolvedQuestion(Long id);

    /**
     * 查询题目详细信息
     *
     * @param id
     * @return
     */
    QuestionRespDTO findQuestionById(Long id);

    /**
     * 分页查询题目
     *
     * @param requestParam
     * @return
     */
    IPage<QuestionPageRespDTO> pageQuestion(QuestionPageReqDTO requestParam);

    /**
     * 查询热门题目
     *
     * @param category
     * @return
     */
    List<QuestionPageRespDTO> findHotQuestion(int category);

    /**
     * 检查题目是否存在
     *
     * @param id
     */
    void checkQuestionExist(Long id);

    /**
     * 检查题目是否为当前用户所有
     *
     * @param id
     */
    void checkQuestionOwner(Long id);

    /**
     * 分页查询我的题目
     * @param requestParam
     * @return
     */
    IPage<QuestionPageRespDTO> pageMyQuestion(QuestionMinePageReqDTO requestParam);

    /**
     * 收藏问题
     */
    void collectQuestion(Long id, int isCollect);

    /**
     * 分页查询我收藏的题目
     */
    IPage<QuestionPageRespDTO> pageCollectQuestion(QuestionCollectPageReqDTO requestParam);

}
