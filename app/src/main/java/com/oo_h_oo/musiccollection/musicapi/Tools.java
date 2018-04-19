package com.oo_h_oo.musiccollection.musicapi;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Tools {
    public static Date addDate(Date date, int type, int day){
        Calendar cl = Calendar.getInstance();
        cl.setTime(date);
        cl.add(type, day);
        return cl.getTime();
    }

    public static String getStrWithRegular(String pattern, String str){
        Matcher m = Pattern.compile(pattern).matcher(str);
        if (m.find()) {
            return m.group(1);
        }
        return "";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultData.toString();
    }

    public static String sendDataByPost(String path){
        HttpURLConnection conn;//声明连接对象
        InputStream is;
        StringBuilder resultData = new StringBuilder();
        try {
            URL url = new URL(path); //URL对象
            conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("POST"); //使用get请求
            int index = path.indexOf(".com");
            if (index > 4) conn.setRequestProperty("referer", path.substring(0, index+4));
            //conn.setRequestProperty("User-agent","Win32");
            if(conn.getResponseCode()==200){//返回200表示连接成功
                is = conn.getInputStream(); //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine;
                while((inputLine = bufferReader.readLine()) != null){
                    resultData.append(inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return resultData.toString();
    }

    // AES加密
    public static String aesEncrypt(String str, String key)  {
        String iv = "0102030405060708";
        if (str == null || key == null) return "";
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("utf-8"));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"), ivParameterSpec);
            String base64Str = Base64.encodeToString(cipher.doFinal(str.getBytes("utf-8")), Base64.DEFAULT);
            return base64Str.replaceAll("\n", "").replaceAll("\r", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
