package org.buaa.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.question.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.question.QuestionCollectReqDTO;
import org.buaa.project.dto.req.question.QuestionRecentPageReqDTO;
import org.buaa.project.dto.req.question.QuestionLikeReqDTO;
import org.buaa.project.dto.req.question.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.question.QuestionPageReqDTO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionPageAllRespDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.service.EsService;
import org.buaa.project.service.QuestionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 题目管理控制层
 */
@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    private final EsService esService;

    /**
     * 上传题目
     */
    @PostMapping("/api/answerly/v1/question")
    public Result<Void> uploadQuestion(@RequestBody QuestionUploadReqDTO requestParam) {
        questionService.uploadQuestion(requestParam);
        return Results.success();
    }

    /**
     * 修改题目
     */
    @PutMapping("/api/answerly/v1/question")
    public Result<Void> updateQuestion(@RequestBody QuestionUpdateReqDTO requestParam) {
        questionService.updateQuestion(requestParam);
        return Results.success();
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/api/answerly/v1/question")
    public Result<Void> deleteQuestion(@RequestParam("id") Long Id) {
        questionService.deleteQuestion(Id);
        return Results.success();
    }

    /**
     * 点赞题目
     */
    @PostMapping("/api/answerly/v1/question/like")
    public Result<Void> likeQuestion(@RequestBody QuestionLikeReqDTO requestParam) {
        questionService.likeQuestion(requestParam);
        return Results.success();
    }

    /**
     * 标记题目已经解决
     */
    @PostMapping("/api/answerly/v1/question/resolved")
    public Result<Void> resolvedQuestion(@RequestBody QuestionSolveReqDTO requestParam) {
        questionService.resolvedQuestion(requestParam);
        return Results.success();
    }

    /**
     * 分页查询题目
     */
    @GetMapping("/api/answerly/v1/question/page")
    public Result<QuestionPageAllRespDTO> pageQuestion(QuestionPageReqDTO requestParam) {
        return Results.success(esService.search(requestParam));
    }

    /**
     * keyword自动补全
     */
    @GetMapping("/api/answerly/v1/question/suggest")
    public Result<List<String>> autoComplete(@RequestParam String keyword) {
        return Results.success(esService.autoComplete(keyword));
    }

    /**
     * 查询热门题目
     */
    @GetMapping("/api/answerly/v1/question/hot/{categoryId}")
    public Result<List<QuestionPageRespDTO>> findHotQuestion(@PathVariable("categoryId") Long category) {
        return Results.success(questionService.findHotQuestion(category));
    }

    /**
     * 查询题目详情
     */
    @GetMapping("/api/answerly/v1/question/{id}")
    public Result<QuestionRespDTO> findQuestionById(@PathVariable("id") Long id) {
        return Results.success(questionService.findQuestionById(id));
    }

    /**
     * 分页查询我的题目
     */
    @GetMapping("/api/answerly/v1/question/my/page")
    public Result<IPage<QuestionPageRespDTO>> findMyQuestion(QuestionMinePageReqDTO requestParam) {
        return Results.success(questionService.pageMyQuestion(requestParam));
    }

    /**
     * 收藏题目
     */
    @PostMapping("/api/answerly/v1/question/collect")
    public Result<Void> collectQuestion(@RequestBody QuestionCollectReqDTO requestParam) {
        questionService.collectQuestion(requestParam);
        return Results.success();
    }

    /**
     * 分页查询我收藏的题目
     */
    @GetMapping("/api/answerly/v1/question/collect/my/page")
    public Result<IPage<QuestionPageRespDTO>> findCollectQuestion(QuestionCollectPageReqDTO requestParam) {
        return Results.success(questionService.pageCollectQuestion(requestParam));
    }

    /**
     * 分页查询最近浏览的题目
     */
    @GetMapping("/api/answerly/v1/question/recent/page")
    public Result<IPage<QuestionPageRespDTO>> findHistoryQuestion(QuestionRecentPageReqDTO requestParam) {
        return Results.success(questionService.pageRecentViewQuestion(requestParam));
    }
}

