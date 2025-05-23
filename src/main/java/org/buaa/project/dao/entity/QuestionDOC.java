package org.buaa.project.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDOC {

    /**
     * 题目id
     */
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 是否解决
     */
    private Integer solvedFlag;

    /**
     * 题目分类
     */
    private Long categoryId;

    /**
     * 创建日期
     */
    private Long createTime;

    /**
     * 自动补全
     */
    private List<String> suggestion;
}
