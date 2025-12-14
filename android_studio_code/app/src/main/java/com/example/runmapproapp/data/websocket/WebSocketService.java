package com.example.runmapproapp.data.websocket;

public interface WebSocketService {
    void connect();
    void disconnect();
    void sendMessage(WebSocketEvent event);
    void addListener(WebSocketEventListener listener);
    void removeListener(WebSocketEventListener listener);
    
    interface WebSocketEventListener {
        void onEvent(WebSocketEvent event);
        void onError(Throwable error);
        void onConnected();
        void onDisconnected();
    }
}
