package com.example.robot20;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import com.google.gson.Gson;//Google Gson包

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
public class HttpRequest {
    private static String fromjson(String message) {
        String msg = "";
        msg = "{" +
                "\"perception\":{" +
                "\"inputText\": {" +
                "\"text\": \"" +
                message+
                "\"}}," +
                "\"userInfo\":{" +
                "\"apiKey\":\"" + MyRobot.API_KEY + "\"," +
                "\"userId\":\"" + MyRobot.ID + "\"}}";
        return msg;
    }

    public static String sendJsonPost(String Json) {
        // HttpClient 6.0被抛弃了
        String result = "";
        BufferedReader reader = null;
        try {
            String urlPath = MyRobot.URL_KEY;
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            conn.setRequestProperty("accept", "application/json");
            // 往服务器里面发送数据
            if (Json != null && !TextUtils.isEmpty(Json)) {
                byte[] writebytes = Json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes());
                outwritestream.flush();
                outwritestream.close();
                Log.d("msg", "doJsonPost: " + conn.getResponseCode());
            }
            if (conn.getResponseCode() == 200) {//上传成功
                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                result = reader.readLine();//得到返回的json字符串
                Log.d("msg","成功"+result);
            }
        }
        catch (Exception e) {
            Log.d("msg", "没成功");//Log出现这个一般是fromjson函数没写好，格式不对
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static ChatMessage sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage();
        Log.d("msg",fromjson(message));
        String gsonResult = sendJsonPost(fromjson(message));//连接请求的内容
        if (gsonResult != null) {
            try {
                chatMessage.setMessage(GetText(gsonResult));//http连接获取的内容解析之后的结果给聊天信息赋值
            } catch (Exception e) {
                chatMessage.setMessage("请求错误...");
                Log.d("msg",gsonResult);
                Log.e("msg",Log.getStackTraceString(e));
            }
        }
        chatMessage.setDate(new Date());
        chatMessage.setType(ChatMessage.Type.INCOUNT);
        return chatMessage;
    }

    private static String GetText(String json) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(json));
        String ans="";
        reader.beginObject();
        try {
            while(reader.hasNext()){
                String name=reader.nextName();

                if(name.equals("results")){ reader.beginArray();
                    reader.beginObject();
                    for(int i=0;i<2;i++){
                        reader.nextName();
                        reader.nextString();
                    }
                    name=reader.nextName();
                    if(name.equals("values")){
                        reader.beginObject();
                        reader.nextName();
                        ans=reader.nextString();
                        reader.close();
                        return  ans;
                    }
                }
                else{
                    reader.skipValue();//跳过不必要字段
                }

            }
        } finally {
            return ans;
        }

    }


}
