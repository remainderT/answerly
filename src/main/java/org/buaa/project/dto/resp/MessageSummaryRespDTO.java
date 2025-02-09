package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * 所有类别的消息的大致信息返回参数
 */
@Data
public class MessageSummaryRespDTO {

    /**
     * 消息大致信息
     */
    private List<HashMap<String, Object>> messageSummary;
}
