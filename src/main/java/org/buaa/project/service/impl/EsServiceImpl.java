package org.buaa.project.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.dto.req.question.QuestionPageReqDTO;
import org.buaa.project.dto.resp.QuestionPageAllRespDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.service.EsService;
import org.buaa.project.toolkit.RedisCount;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;

/**
 * es服务接口层实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsServiceImpl implements EsService {

    private final RestHighLevelClient client;

    private final RedisCount redisCount;

    @Value("${elasticsearch.index-name}")
    private String INDEX_NAME;

    @SneakyThrows
    public QuestionPageAllRespDTO search(QuestionPageReqDTO requestParam)  {
        SearchRequest request = new SearchRequest(INDEX_NAME);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title").requireFieldMatch(false)
                .field("content").requireFieldMatch(false);

        String keyword = requestParam.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(
                    QueryBuilders.boolQuery()
                            .should(QueryBuilders.matchQuery("title", keyword))
                            .should(QueryBuilders.matchQuery("content", keyword))
                            .minimumShouldMatch(1)
            );
        }

        if (requestParam.getCategoryId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", requestParam.getCategoryId()));
        }
        if (requestParam.getSolvedFlag() != 2) {
            boolQuery.filter(QueryBuilders.termQuery("solvedFlag", requestParam.getSolvedFlag()));
        }

        request.source()
                .query(boolQuery)
                .highlighter(highlightBuilder);

        long page = requestParam.getCurrent();
        long size = requestParam.getSize();
        request.source().from((int) ((page - 1) * size)).size((int) size);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        SearchHits searchHits = response.getHits();
        QuestionPageAllRespDTO questionPageAllRespDTO = QuestionPageAllRespDTO.builder()
                .total(searchHits.getTotalHits() == null ?  0 :  searchHits.getTotalHits().value)
                .size(requestParam.getSize())
                .current(requestParam.getCurrent())
                .build();

        List<QuestionPageRespDTO> questionPageRespDTOS = new ArrayList<>();
        searchHits.forEach(hit -> {
            String json = hit.getSourceAsString();
            QuestionPageRespDTO question = JSON.parseObject(json, QuestionPageRespDTO.class);

            // 高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("title");
                if (highlightField != null) {
                    String title = highlightField.fragments()[0].string();
                    question.setTitle(title);
                }
                highlightField = highlightFields.get("content");
                if (highlightField != null) {
                    String content = highlightField.fragments()[0].string();
                    question.setContent(content);
                }
            }
            question.setLikeCount(redisCount.hGet(QUESTION_COUNT_KEY + question.getId(), "like"));
            question.setViewCount(redisCount.hGet(QUESTION_COUNT_KEY + question.getId(), "view"));
            question.setCommentCount(redisCount.hGet(QUESTION_COUNT_KEY + question.getId(), "comment"));

            questionPageRespDTOS.add(question);
        });

        questionPageAllRespDTO.setRecords(questionPageRespDTOS);
        return questionPageAllRespDTO;
    }

    @SneakyThrows
    @Override
    public List<String> autoComplete(String keyword){
        SearchRequest request = new SearchRequest(INDEX_NAME);
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "mySuggestion",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix(keyword)
                        .skipDuplicates(true)
                        .size(10)
        ));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        Suggest suggest = response.getSuggest();
        CompletionSuggestion suggestions = suggest.getSuggestion("mySuggestion");

        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        List<String> list = new ArrayList<>(options.size());
        for (CompletionSuggestion.Entry.Option option : options) {
            String text = option.getText().toString();
            list.add(text);
        }
        return list;
    }


}
