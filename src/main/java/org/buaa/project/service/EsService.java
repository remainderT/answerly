package org.buaa.project.service;

import org.buaa.project.dto.req.question.QuestionPageReqDTO;
import org.buaa.project.dto.resp.QuestionPageAllRespDTO;

import java.util.List;

/**
 * es服务接口层
 */
public interface EsService {

    /**
     * 关键字分页搜索
     */
    QuestionPageAllRespDTO search(QuestionPageReqDTO requestParam);

    /**
     * 自动补全
     */
    List<String> autoComplete(String keyword);

    /**
     * 分词
     */
    List<String> analyze(String text);
}
