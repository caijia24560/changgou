package com.changgou;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author caijia
 * @Date 2020年12月10日 10:26:00
 */
public class TestCJ{
    @Test
    public void test(){
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://ht-dp.boleme.net/dmp/open/getDpSecret?appId=02607aa20589091e4fdb4cbdf8b55b21&secret=7060ebd18c94c4bc";
        ResponseEntity entity = restTemplate.getForObject(url, ResponseEntity.class);
        HttpStatus statusCode = entity.getStatusCode();
        int codeValue = entity.getStatusCodeValue();
        Object body = entity.getBody();

    }
}
