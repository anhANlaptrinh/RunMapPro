package com.example.runmapproapp.data.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.runmapproapp.auth.AuthManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager implements WebSocketService {
    
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = "ws://10.0.2.2:8080/ws/chat/websocket";
    private static final int RECONNECT_DELAY_MS = 3000; // 3 seconds
    
    private static WebSocketManager instance;
    private WebSocket webSocket;
    private OkHttpClient okHttpClient;
    private Context context;
    private Gson gson;
    private Handler mainHandler;
    private List<WebSocketEventListener> listeners = new ArrayList<>();
    private boolean isConnected = false;
    private boolean shouldReconnect = false;
    private AuthManager authManager;
    
    private WebSocketManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.authManager = new AuthManager(context);
        initWebSocket();
    }
    
    public static synchronized WebSocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketManager(context);
        }
        return instance;
    }
    
    private void initWebSocket() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // Disable read timeout for WebSocket
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(0, TimeUnit.SECONDS) // Disable ping, let server handle keep-alive
                .build();
    }
    
    @Override
    public void connect() {
        if (isConnected || webSocket != null) {
            Log.d(TAG, "Already connected, webSocket=" + webSocket + ", isConnected=" + isConnected);
            return;
        }
        
        shouldReconnect = true;
        String token = authManager.getToken();
        Log.d(TAG, "Connecting to WebSocket: " + WS_URL);
        Log.d(TAG, "Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        
        Request request = new Request.Builder()
                .url(WS_URL)
                .addHeader("Authorization", "Bearer " + (token != null ? token : ""))
                .build();
        
        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket onOpen: SUCCESS! Response: " + response);
                isConnected = true;
                notifyConnected();
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "WebSocket onMessage: " + text);
                try {
                    WebSocketEvent event = gson.fromJson(text, WebSocketEvent.class);
                    Log.d(TAG, "Parsed event: type=" + event.getType() + ", conversationId=" + event.getConversationId());
                    notifyEvent(event);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message", e);
                    notifyError(e);
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket onFailure: " + t.getMessage(), t);
                Log.e(TAG, "Response: " + response);
                isConnected = false;
                notifyError(t);
                notifyDisconnected();
                
                // Auto reconnect
                if (shouldReconnect) {
                    Log.d(TAG, "Attempting to reconnect in " + RECONNECT_DELAY_MS + "ms");
                    mainHandler.postDelayed(() -> {
                        WebSocketManager.this.webSocket = null;
                        connect();
                    }, RECONNECT_DELAY_MS);
                }
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket onClosed: code=" + code + ", reason=" + reason);
                isConnected = false;
                notifyDisconnected();
                
                // Auto reconnect if closed unexpectedly
                if (shouldReconnect && code != 1000) { // 1000 = normal closure
                    Log.d(TAG, "Attempting to reconnect in " + RECONNECT_DELAY_MS + "ms");
                    mainHandler.postDelayed(() -> {
                        WebSocketManager.this.webSocket = null;
                        connect();
                    }, RECONNECT_DELAY_MS);
                }
            }
        });
        
        Log.d(TAG, "WebSocket connection initiated, webSocket=" + webSocket);
    }
    
    @Override
    public void disconnect() {
        shouldReconnect = false; // Stop auto reconnect
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
        isConnected = false;
        Log.d(TAG, "WebSocket disconnected");
    }
    
    @Override
    public void sendMessage(WebSocketEvent event) {
        if (webSocket != null && isConnected) {
            try {
                String json = gson.toJson(event);
                webSocket.send(json);
                Log.d(TAG, "Sent message: " + json);
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
                notifyError(e);
            }
        } else {
            Log.w(TAG, "WebSocket not connected");
        }
    }
    
    @Override
    public void addListener(WebSocketEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeListener(WebSocketEventListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyEvent(WebSocketEvent event) {
        mainHandler.post(() -> {
            for (WebSocketEventListener listener : new ArrayList<>(listeners)) {
                listener.onEvent(event);
            }
        });
    }
    
    private void notifyError(Throwable error) {
        mainHandler.post(() -> {
            for (WebSocketEventListener listener : new ArrayList<>(listeners)) {
                listener.onError(error);
            }
        });
    }
    
    private void notifyConnected() {
        mainHandler.post(() -> {
            for (WebSocketEventListener listener : new ArrayList<>(listeners)) {
                listener.onConnected();
            }
        });
    }
    
    private void notifyDisconnected() {
        mainHandler.post(() -> {
            for (WebSocketEventListener listener : new ArrayList<>(listeners)) {
                listener.onDisconnected();
            }
        });
    }
}
