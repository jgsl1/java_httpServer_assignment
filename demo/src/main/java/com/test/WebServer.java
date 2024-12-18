package com.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//利用socket实现一个http服务器
/*工作过程：用户进入服务器，main函数执行，服务器在端口8081不断监听（while死循环）
 *一旦监听到用户访问就立即建立新线程
 *线程独立于程序单独运行，随后main继续监听新的用户，实现多线程处理
 */

public class WebServer {
    private int port;

    public WebServer(int port) {
        this.port = port;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            System.out.println("服务器已在端口启动！ " + port);

            while (true) {
                Socket socket = serverSocket.accept();                  //确认服务器接口连接
                System.out.println("一个用户连接成功！");

                new Thread(new RequestHandler(socket)).start();        //建立新线程  线程独立运行
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServer server = new WebServer(8081);       //在8081端口开放
        server.start();
    }
}