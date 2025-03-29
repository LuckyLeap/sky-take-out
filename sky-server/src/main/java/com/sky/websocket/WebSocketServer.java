package com.sky.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 使用静态ConcurrentHashMap确保所有实例共享会话
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("连接建立成功，sid: {}", sid);
        sessionMap.put(sid, session);
        log.debug("当前会话数量：{}", sessionMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) {
        System.out.println("服务端收到客户端发来的消息:" + message);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("连接关闭，sid: {}", sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发消息
     */
    public void sendToAllClient(String message) {
        log.info("开始群发消息，当前会话数：{}", sessionMap.size());
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                // 服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (IllegalArgumentException e) {
                log.error("非法参数异常: {}", e.getMessage());
            } catch (IllegalStateException e) {
                log.error("会话状态异常: {}", e.getMessage());
                sessionMap.values().remove(session); // 移除无效会话
            } catch (Exception e) {
                log.error("消息发送失败: {}", e.getMessage(), e);
            }
        }
    }
}