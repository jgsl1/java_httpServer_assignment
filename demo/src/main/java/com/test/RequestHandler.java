package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

//服务器一旦接受到连接，启用的线程将在这里处理


public class RequestHandler implements Runnable {
    @SuppressWarnings("FieldMayBeFinal")
    private Socket socket;
    public Map<String, byte[]> fileCache = new HashMap<>();

    //另外三个类的初始化
    private DatabaseManager databaseManager;
    private HandlePages handlePages;
    private Error error;
    public RequestHandler(Socket socket) {
        this.socket = socket;
        this.databaseManager = new DatabaseManager();
        this.handlePages = new HandlePages();
        this.error = new Error();
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")

    //根据用户发出的不同请求进行处理
    //主要实现了POST和GET请求的处理
    public void run() {
        try (InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String requestLine = reader.readLine();
            System.out.println("Request: " + requestLine);

            //提取方法和url
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            //GET方法
            if (method.equals("GET")) {
                if (path.equals("/")) {
                    handlePages.sendResponse(output, getFilePath(path));
                } else if (path.startsWith("/redirect")) {
                    handlePages.handleRedirect(output, path);
                } else if (path.startsWith("/download")) {
                    handlePages.handleDownload(output, path);
                } else {
                    handlePages.sendResponse(output, getFilePath(path));
                }
            }
            
            //POST方法
            else if (method.equals("POST")) {
                if (path.equals("/search")) {
                    String url = handlePages.readPostBody(reader);
                    System.out.println("获取的URL: " + url);
                    String responseBody = "URL获取成功    " + "可尝试访问";
                    handlePages.sendPostResponse(output, responseBody);
                } else if (path.equals("/login")) {
                    handlePages.handleLogin(output, reader);
                } else if (path.equals("/register")) {
                    handlePages.handleRegister(output, reader);
                } else {
                    error.send404Response(output);
                }
            } else {
                error.send404Response(output);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //根据请求返回显示页面
    private String getFilePath(String path) {
        if (path.equals("/")) {
            return "webapp/Enter.html";
        } else if (path.equals("/register.html")) {
            return "webapp/register.html";
        } else {
            return "webapp" + path; // 动态映射路径到文件系统
        }
    }


}