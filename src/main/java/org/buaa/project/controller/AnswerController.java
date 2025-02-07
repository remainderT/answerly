package org.buaa.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.AnswerLikeReqDTO;
import org.buaa.project.dto.req.AnswerMinePageReqDTO;
import org.buaa.project.dto.req.AnswerPageReqDTP;
import org.buaa.project.dto.req.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.AnswerUploadReqDTO;
import org.buaa.project.dto.resp.AnswerPageRespDTO;
import org.buaa.project.service.AnswerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 答案管理控制层
 */
@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 上传答案
     */
    @PostMapping("/api/answerly/v1/answer")
    public Result<Void> uploadAnswer(@RequestBody AnswerUploadReqDTO requestParam) {
        answerService.uploadAnswer(requestParam);
        return Results.success();
    }

    /**
     * 修改答案
     */
    @PutMapping("/api/answerly/v1/answer")
    public Result<Void> updateAnswer(@RequestBody AnswerUpdateReqDTO requestParam) {
        answerService.updateAnswer(requestParam);
        return Results.success();
    }

    /**
     * 删除答案
     */
    @DeleteMapping("/api/answerly/v1/answer")
    public Result<Void> deleteAnswer(@RequestParam("id") Long Id) {
        answerService.deleteAnswer(Id);
        return Results.success();
    }

    /**
     * 点赞答案
     */
    @PostMapping("/api/answerly/v1/answer/like")
    public Result<Void> likeQuestion(AnswerLikeReqDTO requestParam) {
        answerService.likeAnswer(requestParam);
        return Results.success();
    }

    /**
     * 标记答案有用
     */
    @PostMapping("/api/answerly/v1/answer/useful")
    public Result<Void> usefulQuestion(@RequestParam("id") Long Id) {
        answerService.markUsefulAnswer(Id);
        return Results.success();
    }

    /**
     * 分页查询某题的回答
     */
    @GetMapping("/api/answerly/v1/answer/page")
    public Result<IPage<AnswerPageRespDTO>> pageAnswer(AnswerPageReqDTP requestParam) {
        return Results.success(answerService.pageAnswer(requestParam));
    }

    /**
     * 分页查询我的回答
     */
    @GetMapping("/api/answerly/v1/answer/my")
    public  Result<IPage<AnswerPageRespDTO>> pageMyAnswer(AnswerMinePageReqDTO requestParam) {
        return Results.success(answerService.pageMyAnswer(requestParam));
    }

}
