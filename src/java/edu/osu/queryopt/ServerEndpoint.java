/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.osu.queryopt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 *
 * @author ritika
 */
@javax.websocket.server.ServerEndpoint("/queryoptimizer")
public class ServerEndpoint {
    
    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    SessionHandler handler = new SessionHandler();
    
    @OnOpen
    public void onOpen (Session peer) {
        peers.add(peer);
    }

    @OnClose
    public void onClose (Session peer) {
        peers.remove(peer);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("session id: " + session.getId());
        System.out.println("input query: " + message);
        List<String> trees = handler.getExpressionTree(message);
        try {
            for (String tree:trees) {
                session.getBasicRemote().sendText(tree);
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
