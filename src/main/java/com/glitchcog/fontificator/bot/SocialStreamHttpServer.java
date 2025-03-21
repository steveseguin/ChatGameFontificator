package com.glitchcog.fontificator.bot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTP server that listens for messages from Social Stream Ninja and forwards them to the chat bot
 */
public class SocialStreamHttpServer {
    
    private static final Logger logger = Logger.getLogger(SocialStreamHttpServer.class);
    private HttpServer server;
    private ChatViewerBot chatBot;
    
    public SocialStreamHttpServer(int port, ChatViewerBot chatBot) throws IOException {
        // Store the chat bot reference
        if (chatBot == null) {
            logger.error("ChatViewerBot is null in SocialStreamHttpServer constructor");
            throw new IllegalArgumentException("ChatViewerBot cannot be null");
        }
        this.chatBot = chatBot;
        
        // Create the HTTP server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/message", new MessageHandler());
        server.createContext("/", new MessageHandler());
        server.setExecutor(null);
    }
    
    public void start() {
        if (chatBot == null) {
            logger.error("Cannot start SocialStreamHttpServer because ChatViewerBot is null");
            return;
        }
        
        server.start();
        logger.info("Social Stream HTTP server started on port " + server.getAddress().getPort());
    }
    
    private class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers to allow all origins
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

            // Handle OPTIONS requests for preflight
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String body = "";
                try {
                    // Check if ChatViewerBot is available
                    if (chatBot == null) {
                        throw new IllegalStateException("ChatViewerBot is not available");
                    }
                    
                    // Read the request body
                    InputStream is = exchange.getRequestBody();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    body = s.hasNext() ? s.next() : "";
                    s.close();
                    
                    // Log the received JSON for debugging
                    logger.info("Received JSON: " + body);
                    
                    // Process the JSON message
                    JSONObject json = new JSONObject(body);
                    
                    // Extract required fields with defaults if missing
                    String username = json.optString("chatname", "Guest");
                    String content = json.optString("chatmessage", "");
                    
                    // Default to NORMAL if type is missing or invalid
                    String typeStr = json.optString("type", "NORMAL").toUpperCase();
                    MessageType messageType = MessageType.NORMAL;
                    try {
                        if ("ACTION".equals(typeStr)) {
                            messageType = MessageType.ACTION;
                        } else if ("JOIN".equals(typeStr)) {
                            messageType = MessageType.JOIN;
                        }
                    } catch (Exception e) {
                        logger.warn("Invalid message type: " + typeStr + ", defaulting to NORMAL");
                    }
                    
                    // Create a TwitchPrivmsg object to hold message metadata
                    TwitchPrivmsg privmsg = new TwitchPrivmsg(username);
                    
                    // Handle color property - check both nameColor and color fields
                    if (json.has("nameColor") || json.has("color")) {
                        try {
                            String colorStr = json.has("nameColor") ? 
                                    json.getString("nameColor") : 
                                    json.optString("color", "");
                                    
                            if (colorStr != null && !colorStr.isEmpty()) {
                                // Handle color names or hex values
                                if (colorStr.startsWith("#")) {
                                    colorStr = colorStr.substring(1);
                                    privmsg.setColor(new java.awt.Color(Integer.parseInt(colorStr, 16)));
                                } else {
                                    // Try to parse as a color name using reflection
                                    try {
                                        java.lang.reflect.Field field = java.awt.Color.class.getField(colorStr.toLowerCase());
                                        privmsg.setColor((java.awt.Color)field.get(null));
                                    } catch (Exception e) {
                                        // Default to a light blue color if parsing fails
                                        privmsg.setColor(new java.awt.Color(173, 216, 230));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing color: " + e.getMessage());
                        }
                    }
                    
                    // You could add additional processing here for any other fields you want to support
                    
                    // Send the message to chat
                    chatBot.sendMessageToChat(messageType, content, privmsg);
                    
                    // Send a success response
                    String response = "Message received";
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    
                } catch (Exception e) {
                    logger.error("Error processing message: " + e.getMessage(), e);
                    logger.error("JSON body was: " + body);
                    
                    String response = "Error processing message: " + e.getMessage();
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(500, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                // Add a simple GET handler for status checks
                String response = "SocialStreamHttpServer is running";
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Method not allowed";
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}