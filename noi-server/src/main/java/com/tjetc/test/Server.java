package com.tjetc.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(9528);
            Socket socket = serverSocket.accept();
            //获取客户端请求
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //获取键盘输入
            Scanner scanner = new Scanner(System.in);
            //发送消息到客户端
            PrintWriter pw = new PrintWriter(socket.getOutputStream());

            while (true){
                String input = br.readLine();
                System.out.println("收到客户端请求： " + input);
                String output = scanner.nextLine();

                pw.println(output);
                pw.flush();

                if("再见".equals(input)){
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("服务启动失败！");
        }
    }
}
