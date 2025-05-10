package cn.dataplatform.open.web.vo.data.source.console;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Message {

    private Map<String, String> headers;
    private String body;

    /**
     * @param headers 消息头
     * @param string  消息体
     */
    public Message(Map<String, ?> headers, String string) {
        this.body = string;
        Map<String, String> map = new LinkedHashMap<>();
        headers.forEach((k, v) -> map.put(k, (v == null ? null : v.toString())));
        this.headers = map;
    }

}