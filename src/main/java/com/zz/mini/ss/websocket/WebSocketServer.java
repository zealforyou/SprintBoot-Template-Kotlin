package com.zz.mini.ss.websocket;


import com.zz.mini.ss.db.CacheDb;
import com.zz.mini.ss.bean.Data;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}") // 客户端URI访问的路径
@Component
public class WebSocketServer {


    private static ConcurrentHashMap<String, Session> sockets = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> waitNotifyUser = new ConcurrentHashMap<>();

    private String userId;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;

        System.out.println("建立连接:" + userId + ":" + session.hashCode());
        // 添加socket
        sockets.put(userId, session);
        if (waitNotifyUser.get(userId) != null) {
            String fromID = waitNotifyUser.get(userId);
            notifyCheckConnect(sockets.get(fromID), new HashMap());
            waitNotifyUser.remove(userId);
        }
    }

    @OnClose
    public void onClose(Session session) {
//        Session session = sockets.get(userId);
//        if (session == null) return;
//        //删除session
//        sockets.remove(userId);
        String[] removeKey = new String[1];
        sockets.entrySet().stream().forEach(stringSessionEntry -> {
            if (stringSessionEntry.getValue() == session) {
                removeKey[0] = stringSessionEntry.getKey();
            }
        });
        if (removeKey[0] != null) {
            CacheDb.db.candidates.remove(removeKey[0]);
            CacheDb.db.answerCache.remove(removeKey[0]);
            CacheDb.db.offerCache.remove(removeKey[0]);
            sockets.remove(removeKey[0]);
            System.out.println(session.hashCode() + "用户离开--" + removeKey[0]);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message.contains("event")) {
            handleMessage(message, session);
        }
    }

    public void sendMessage(String message) {
        Session session = sockets.get(userId);
        if (session != null) {
            session.getAsyncRemote().sendText(message);
        }
    }


    private void handleMessage(String message, Session session) {
        Gson gson = new Gson();
        Data data = gson.fromJson(message, Data.class);
//        System.out.println("接收到来自" + data.getData().get("fromID") + "的新消息：" + message);
        switch (data.getEvent()) {
            case "__ice_candidate":
                iceCandidate(data.getData(), session);
                break;
            case "__offer":
                offer(data.getData(), session);
                break;
            case "__answer":
                answer(data.getData(), session);
                break;
            case "__user_connect":
                checkConnect(data.getData(), session);
                break;
            default:
                break;
        }
    }

    private void iceCandidate(Map<String, Object> data, Session socket) {
        //soc=this
        Session session = sockets.get(data.get("userID").toString());
        if (session == null) {
            return;
        }

        Data send = new Data();
        send.setEvent("_ice_candidate");
        Map<String, Object> map = data;
        map.put("id", data.get("id"));
        map.put("label", data.get("label"));
        map.put("candidate", data.get("candidate"));
        send.setData(map);
        String content = new Gson().toJson(send);
        CacheDb.db.addCandidate(data.get("fromID").toString(), map);
        session.getAsyncRemote().sendText(content);

        System.out.println("接收到来自" + data.get("fromID").toString() + "的ICE Candidate");
    }

    private void offer(Map<String, Object> data, Session socket) {
        Session session = sockets.get(data.get("userID").toString());
        if (session == null) {
            return;
        }
        Data send = new Data();
        send.setEvent("_offer");

        Map<String, Object> map = data;
        map.put("sdp", data.get("sdp"));
        send.setData(map);
        String content = new Gson().toJson(send);
        CacheDb.db.offerCache.put(data.get("fromID").toString(), data.get("sdp").toString());
        session.getAsyncRemote().sendText(content);
        System.out.println("接收到来自" + data.get("fromID").toString() + "的Offer");

    }

    private void answer(Map<String, Object> data, Session socket) {
        Session session = sockets.get(data.get("userID").toString());
        if (session == null) {
            return;
        }

        Data send = new Data();
        send.setEvent("_answer");

        Map<String, Object> map = data;
        map.put("sdp", data.get("sdp"));
        send.setData(map);
        String content = new Gson().toJson(send);
        CacheDb.db.answerCache.put(data.get("fromID").toString(), data.get("sdp").toString());
        session.getAsyncRemote().sendText(content);
        System.out.println("接收到来自" + data.get("fromID").toString() + "的Answer");
    }

    private void notifyCheckConnect(Session session, Map ext) {
        if (session != null) {
            Data send = new Data();
            send.setEvent("_user_connect");
            send.setData(ext);
            session.getAsyncRemote().sendText(new Gson().toJson(send));
        }
    }

    private void checkConnect(Map<String, Object> data, Session socket) {
        String userID = data.get("userID").toString();
        String fromID = data.get("fromID").toString();
        if (sockets.get(userID) != null) {
            //对方在线
            Map map = new HashMap<>();
            if (fromID.equals("answer")) {
                map.put("offer", CacheDb.db.offerCache.get(userID));
            } else {
                map.put("answer", CacheDb.db.answerCache.get(userID));
            }

            List<Map<String, Object>> candidates = CacheDb.db.candidates.get(userID);
            map.put("candidates", candidates);
            notifyCheckConnect(socket, map);
        } else {
            waitNotifyUser.put(userID, fromID);
        }
        System.out.println("接收到来自" + data.get("fromID").toString() + "的checkConnect");
    }

}