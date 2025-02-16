package org.buaa.project.toolkit.scheduled;

import com.alibaba.fastjson2.JSON;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class EsSynchronization implements CommandLineRunner {

    private final CanalConnector connector;

    private final RestHighLevelClient client;

    @Override
    public void run(String... args) {
        Thread canalThread = new Thread(() -> {
            log.info("连接canal并开始同步");
            try {
                connector.connect();
                connector.subscribe("answerly.question");
                connector.rollback();
                while (true) {
                    Message message = connector.getWithoutAck(100);
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId != -1 && size > 0) {
                        try {
                            for (CanalEntry.Entry entry : message.getEntries()) {
                                this.handle(entry);
                            }
                            connector.ack(batchId);
                        } catch (Exception e) {
                            connector.rollback(batchId);
                            log.error("异常,回滚...", e);
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log.error("连接canal出现异常", e);
            } finally {
                connector.disconnect();
            }
        });
        canalThread.setName("canal-thread-0");
        canalThread.start();
    }

    @SneakyThrows
    public void handle(CanalEntry.Entry entry) {
        if (entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)) {
            String tableName = entry.getHeader().getTableName();
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                CanalEntry.EventType eventType = rowChange.getEventType();
                switch (eventType) {
                    case INSERT -> {
                        Map<String, Object> params = rowData.getAfterColumnsList().stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue));
                        insertDocument(tableName, params);
                    }
                    case DELETE -> {
                        Map<String, Object> params = rowData.getBeforeColumnsList().stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue));
                        deleteDocument(tableName, params);
                    }
                    case UPDATE -> {
                        Map<String, Object> params = rowData.getAfterColumnsList().stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue));
                        updateDocument(tableName, params);
                    }
                }
            }
        }
    }

    private void insertDocument(String tableName, Map<String, Object> data) throws IOException {
        String id = data.get("id").toString();
        IndexRequest request = new IndexRequest(tableName)
                .id(id)
                .source(JSON.toJSONString(data), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
        log.info("插入 Elasticsearch 成功: {}", data);
    }


    private void updateDocument(String tableName, Map<String, Object> data) throws IOException {
        String id = data.get("id").toString();
        UpdateRequest request = new UpdateRequest(tableName, id)
                .doc(JSON.toJSONString(data), XContentType.JSON);
        client.update(request, RequestOptions.DEFAULT);
        log.info("更新 Elasticsearch 成功: {}", data);
    }

    private void deleteDocument(String tableName, Map<String, Object> data) throws IOException {
        String id = data.get("id").toString();
        DeleteRequest request = new DeleteRequest(tableName, id);
        client.delete(request, RequestOptions.DEFAULT);
        log.info("删除 Elasticsearch 文档成功: {}", data);
    }


}