package maco.terminal_chat.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

public class ChatHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = session.getId();
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("Welcome! Your user ID is " + userId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String[] parts = payload.split(":", 2); // expecting "userId:message" format

        if (parts.length == 2) {
            String targetUserId = parts[0];
            String chatMessage = parts[1];

            WebSocketSession targetSession = sessions.get(targetUserId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage("Message from " + session.getId() + ": " + chatMessage));
                session.sendMessage(new TextMessage("Message sent to " + targetUserId));
            } else {
                session.sendMessage(new TextMessage("User " + targetUserId + " not found or not connected."));
            }
        } else {
            session.sendMessage(new TextMessage("Invalid message format. Use userId:message"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }
}
