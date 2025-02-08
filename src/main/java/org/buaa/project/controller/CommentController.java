package org.buaa.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.comment.CommentLikeReqDTO;
import org.buaa.project.dto.req.comment.CommentMinePageReqDTO;
import org.buaa.project.dto.req.comment.CommentPageReqDTP;
import org.buaa.project.dto.req.comment.CommentUpdateReqDTO;
import org.buaa.project.dto.req.comment.CommentUploadReqDTO;
import org.buaa.project.dto.req.comment.CommentUsefulReqDTO;
import org.buaa.project.dto.resp.CommentPageRespDTO;
import org.buaa.project.service.CommentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论管理控制层
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService CommentService;

    /**
     * 上传评论
     */
    @PostMapping("/api/answerly/v1/comment")
    public Result<Void> uploadComment(@RequestBody CommentUploadReqDTO requestParam) {
        CommentService.uploadComment(requestParam);
        return Results.success();
    }

    /**
     * 修改评论
     */
    @PutMapping("/api/answerly/v1/comment")
    public Result<Void> updateComment(@RequestBody CommentUpdateReqDTO requestParam) {
        CommentService.updateComment(requestParam);
        return Results.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/api/answerly/v1/comment")
    public Result<Void> deleteComment(@RequestParam("id") Long Id) {
        CommentService.deleteComment(Id);
        return Results.success();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/api/answerly/v1/comment/like")
    public Result<Void> likeQuestion(@RequestBody CommentLikeReqDTO requestParam) {
        CommentService.likeComment(requestParam);
        return Results.success();
    }

    /**
     * 标记评论有用
     */
    @PostMapping("/api/answerly/v1/comment/useful")
    public Result<Void> usefulQuestion(@RequestBody CommentUsefulReqDTO requestParam) {
        CommentService.markUsefulComment(requestParam);
        return Results.success();
    }

    /**
     * 分页查询某题的回答
     */
    @GetMapping("/api/answerly/v1/comment/page")
    public Result<IPage<CommentPageRespDTO>> pageComment(CommentPageReqDTP requestParam) {
        return Results.success(CommentService.pageComment(requestParam));
    }

    /**
     * 分页查询我的回答
     */
    @GetMapping("/api/answerly/v1/comment/my/page")
    public  Result<IPage<CommentPageRespDTO>> pageMyComment(CommentMinePageReqDTO requestParam) {
        return Results.success(CommentService.pageMyComment(requestParam));
    }

}
