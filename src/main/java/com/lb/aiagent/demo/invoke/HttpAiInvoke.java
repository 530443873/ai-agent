package com.lb.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class HttpAiInvoke {
    public static void main(String[] args) {
        // 构造请求 JSON
        JSONObject input = new JSONObject();
        JSONArray messages = new JSONArray();

        // 添加 system 消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // 添加 user 消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "帮用Java写一个王者荣耀");
        messages.add(userMessage);

        input.set("messages", messages);

        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus");
        requestBody.set("input", input);
        requestBody.set("parameters", parameters);

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                .header("Authorization", "Bearer " + TestApiKey.API_KEY)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute();

        // 输出响应
        System.out.println("Status: " + response.getStatus());
        System.out.println("Response: " + response.body());
    }
}