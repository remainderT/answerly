package org.buaa.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dto.req.AnswerLikeReqDTO;
import org.buaa.project.dto.req.AnswerMinePageReqDTO;
import org.buaa.project.dto.req.AnswerPageReqDTP;
import org.buaa.project.dto.req.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.AnswerUploadReqDTO;
import org.buaa.project.dto.resp.AnswerPageRespDTO;

public interface AnswerService extends IService<AnswerDO> {

    /**
     * 上传回答
     */
    void uploadAnswer(AnswerUploadReqDTO requestParam);

    /**
     * 点赞回答
     */
    void likeAnswer(AnswerLikeReqDTO requestParam);

    /**
     * 删除回答
     */
    void deleteAnswer(Long id);

    /**
     * 标记回答有用
     */
    void markUsefulAnswer(Long id);

    /**
     * 更新回答
     */
    void updateAnswer(AnswerUpdateReqDTO requestParam);

    /**
     * 检查回答是否存在
     */
    void checkAnswerExist(Long id);

    /**
     * 检查回答所有者
     */
    void checkAnswerOwner(Long id);

    /**
     * 分页查询回答
     */
    IPage<AnswerPageRespDTO> pageAnswer(AnswerPageReqDTP requestParam);

    /**
     * 分页查询我的回答
     */
    IPage<AnswerPageRespDTO> pageMyAnswer(AnswerMinePageReqDTO requestParam);


}
