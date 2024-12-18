package com.test;

import java.io.IOException;
import java.io.OutputStream;

    
public class Error{
    
    
    public void send404Response(OutputStream output) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        output.write(response.getBytes());
        output.flush();
    }

    public void send500Response(OutputStream output) throws IOException {
        String response = "HTTP/1.1 500 Internal Server Error\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        output.write(response.getBytes());
        output.flush();
    }
}