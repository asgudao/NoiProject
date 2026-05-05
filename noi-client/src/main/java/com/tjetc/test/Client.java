package com.tjetc.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try{
            Socket socket = new Socket("127.0.0.1", 9528);
            //获取服务端响应
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //获取客户端用户输入
            Scanner scanner = new Scanner(System.in);
            //向服务端发送请求
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            System.out.println("准备接收请求……");
            while (true){
                String output = scanner.next();
                pw.println(output);
                pw.flush();

                String input = br.readLine();
                System.out.println("来自服务端的响应： " + input);
                if("再见".equals(output)){
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}