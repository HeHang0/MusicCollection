package com.oo_h_oo.musiccollection.musicapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class Tools {
    public static Date addDate(Date date, int type, int day){
        Calendar cl = Calendar.getInstance();
        cl.setTime(date);
        cl.add(type, day);
        return cl.getTime();
    }

    public static String sendDataByGet(String path){
        HttpURLConnection conn;//声明连接对象
        InputStream is;
        StringBuilder resultData = new StringBuilder();
        try {
            URL url = new URL(path); //URL对象
            conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("GET"); //使用get请求
            conn.setRequestProperty("User-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36");
            if (path.contains("i.y.qq.com"))
            {
                conn.setRequestProperty("referer","http://y.qq.com");
            }
            else if (path.contains(".com"))
            {
                conn.setRequestProperty("referer",path.substring(0, path.indexOf(".com") + 4));
            }
            if(conn.getResponseCode()==200){//返回200表示连接成功
                is = conn.getInputStream(); //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine;
                while((inputLine = bufferReader.readLine()) != null){
                    resultData.append(inputLine);
                }
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultData.toString();
    }
}
