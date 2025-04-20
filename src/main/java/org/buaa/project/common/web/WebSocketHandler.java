package org.buaa.project.common.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        sessions.put(userId, session); // 存储用户会话
        log.info("用户 {} 连接成功", userId);
    }
    private String getUserIdFromSession(WebSocketSession session) {
        //从 URL 中获取参数,url规定如ws://localhost:8080/ws?userId=123）
        return session.getUri().getQuery().split("=")[1];
    }
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        sessions.remove(userId); // 移除用户会话
        log.info("用户 {} 已断开连接", userId);
    }
    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("发送消息给用户 {} 失败", userId, e);
            }
        } else {
            log.info("用户 {} 未连接或连接已断开", userId);
        }
    }
}
