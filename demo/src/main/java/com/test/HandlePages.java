package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;



public class HandlePages{

    //对三个引用的类进行初始化
    @SuppressWarnings("FieldMayBeFinal")
    private Map<String, byte[]> fileCache = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    private DatabaseManager databaseManager;
    @SuppressWarnings("FieldMayBeFinal")
    private Error error;
    public HandlePages() {
        this.databaseManager = new DatabaseManager();
        this.error = new Error();
    }


    //一个完整的路径path包括https://example.com/path?url=..........
    //先将path按？分成两部分，将url的部分赋给params，利用param遍历params来查询是否包含”url=“
    //随后解析url地址并进行重定向
    public void handleRedirect(OutputStream output, String path) throws IOException {
        String[] parts = path.split("\\?");
        if (parts.length > 1) {
            String query = parts[1];                    //part通过split命令将？左右的值赋给了part[0]和part[1]。 part[0]为域名 part[1]为url
            String[] params = query.split("&");         //url中包含url=https%3A%2F%2Fgoogle.com&name=value   因此还需要继续分割得到url
            for (String param : params) {
                if (param.startsWith("url=")) {
                    String url = URLDecoder.decode(param.substring("url=".length()), StandardCharsets.UTF_8.name());     //利用解码器解析url
                    String redirectResponse = "HTTP/1.1 302 Found\r\n" +
                            "Location: " + url + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";
                    output.write(redirectResponse.getBytes(StandardCharsets.UTF_8));      //重定向到目标网页（将重定向的网页写入outputstream返回给用户）
                    output.flush();           //执行完操作之后刷新output迎接下一个处理
                    return;
                }
            }
        }
        error.send404Response(output);
    }



    public void handleDownload(OutputStream output, String path) throws IOException {
        String[] parts = path.split("\\?");
        if (parts.length > 1) {
            String query = parts[1];
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("url=")) {
                    String url = URLDecoder.decode(param.substring("url=".length()), StandardCharsets.UTF_8.name());
                    String htmlContent = fetchHTMLContent(url);
                    if (htmlContent != null) {
                        String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/octet-stream\r\n" +
                                "Content-Disposition: attachment; filename=\"downloaded.html\"\r\n" +
                                "Content-Length: " + htmlContent.getBytes().length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n" +
                                htmlContent;
                        output.write(response.getBytes(StandardCharsets.UTF_8));
                        output.flush();
                        return;
                    }
                }
            }
        }
        error.send404Response(output);
    }


    public void handleLogin(OutputStream output, BufferedReader reader) throws IOException {

        //根据前端发送的格式username=testuser&password=testpass进行分解
        String body = readPostBody(reader);
        String[] params = body.split("&");
        String username = null;
        String password = null;

        //解析用户发来的id和密码
        for (String param : params) {
            if (param.startsWith("username=")) {
                username = sanitizeInput(URLDecoder.decode(param.substring("username=".length()), StandardCharsets.UTF_8.name()));
            } else if (param.startsWith("password=")) {
                password = sanitizeInput(URLDecoder.decode(param.substring("password=".length()), StandardCharsets.UTF_8.name()));
            }
        }
    
        if (username != null && password != null) {
            // 从数据库中验证用户信息
            if (databaseManager.validateUserFromDatabase(username, password)) {
                sendPostResponse(output, "登录成功！");
                // 登录成功后重定向到 index 页面
                sendRedirect(output, "/index.html");
            } else {
                sendPostResponse(output, "登录失败！");
            }
        } else {
            sendPostResponse(output, "登录失败！");
        }
    }

    public void handleRegister(OutputStream output, BufferedReader reader) throws IOException {
        String body = readPostBody(reader);
        String[] params = body.split("&");
        String username = null;
        String password = null;
    
        for (String param : params) {
            if (param.startsWith("username=")) {
                username = sanitizeInput(URLDecoder.decode(param.substring("username=".length()), StandardCharsets.UTF_8.name()));
            } else if (param.startsWith("password=")) {
                password = sanitizeInput(URLDecoder.decode(param.substring("password=".length()), StandardCharsets.UTF_8.name()));
            }
        }
    
        if (username != null && password != null) {
            // 将用户信息存入数据库
            if (databaseManager.saveUserToDatabase(username, password)) {
                sendPostResponse(output, "Registration successful");
                // 注册成功后重定向回登录页面
                sendRedirect(output, "/log.html");
            } else {
                sendPostResponse(output, "Registration failed");
            }
        } else {
            sendPostResponse(output, "Registration failed");
        }
    }

    private String fetchHTMLContent(String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder content;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } else {
            System.out.println("请求失败，响应码: " + responseCode);
            return null;
        }
    }
    
    private void sendRedirect(OutputStream output, String location) throws IOException {
        String response = "HTTP/1.1 302 Found\r\n" +
                        "Location: " + location + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
    
        output.write(response.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }
    

    private String sanitizeInput(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "");
    }

    public void sendResponse(OutputStream output, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            error.send404Response(output);
            return;
        }

        byte[] fileContent = readFileContent(file);
        if (fileContent == null) {
            error.send500Response(output);
            return;
        }

        String contentType = getContentType(filePath);

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + fileContent.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        output.write(response.getBytes());
        output.write(fileContent);
        output.flush();
    }

    public void sendPostResponse(OutputStream output, String responseBody) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Length: " + responseBody.getBytes().length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseBody;

        output.write(response.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }


    private byte[] readFileContent(File file) throws IOException {
        if (fileCache.containsKey(file.getPath())) {
            return fileCache.get(file.getPath());
        }

        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
        }

        fileCache.put(file.getPath(), buffer);
        return buffer;
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html";
        } else if (filePath.endsWith(".css")) {
            return "text/css";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript";
        } else {
            return "application/octet-stream";
        }
    }

    public String readPostBody(BufferedReader reader) throws IOException {
        StringBuilder body = new StringBuilder();
        int contentLength = 0;

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            reader.read(buffer, 0, contentLength);
            body.append(buffer);
        }

        return body.toString();
    }
}