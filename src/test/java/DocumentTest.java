import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import org.buaa.project.ProjectApplication;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.QuestionDOC;
import org.buaa.project.service.QuestionService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = ProjectApplication.class)
public class DocumentTest {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private QuestionService questionService;

    @Test
    void testInsertAndVerify() {
        List<QuestionDO> questionDOS = questionService.list();
        for (QuestionDO questionDO : questionDOS) {
            QuestionDOC questionDoc = BeanUtil.copyProperties(questionDO, QuestionDOC.class);
            List<String> suggestion = new ArrayList<>();
            suggestion.add(questionDO.getTitle());
            suggestion.add(questionDO.getContent());
            questionDoc.setSuggestion(suggestion);
            IndexRequest request = new IndexRequest("question")
                    .id(questionDO.getId().toString())
                    .source(JSON.toJSONString(questionDoc), XContentType.JSON);
            try {
                IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                System.out.println("插入结果状态: " + response.status());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        client.close();
        System.in.read();
    }
}
