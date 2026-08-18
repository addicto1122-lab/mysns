package com.spring.mysns.service;

import com.spring.mysns.domain.WeatherApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {

    public Map<String, String> getCurrentWeather(){

        //API 서비스 요청을 위한 변수 선언
        String EndPoint =
                "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

        String ServiceKey = "nvgb1YByVp6rO+vKXTtMqnK+RYm/VPzxwYehqxrfcAXDVo5mh1PZUMoPFDDJFsREQ1Y/8GLGf9YBq+E/N0U8QA==";


        // 혹시 ServiceKey에러 발생시 직접 encoding 코드 추가
        String encodeKey =
                URLEncoder.encode(ServiceKey, StandardCharsets.UTF_8);


        // 요청 URL 생성
        URI uri = UriComponentsBuilder
                .fromUriString(EndPoint)
                .queryParam("ServiceKey", encodeKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", "20260818")
                .queryParam("base_time", "0600")
                .queryParam("nx", 55)
                .queryParam("ny", 127)
                .build(true)
                .toUri();


        // 클라이언트가 Uri로 Request(요청) 후 Response(응답) 결과 반환
        RestClient restClient = RestClient.create();

        WeatherApiResponse weatherApiResponse =
                restClient.get().uri(uri) // 요청
                        .retrieve() // 응답 결과 반환
                        .body(WeatherApiResponse.class); // 응답을 WeatherApiResponse 통으로 반환


        Map<String, String> result = new HashMap<>();

        for (WeatherApiResponse.Response.Item item :
                weatherApiResponse.response().body().items().item()) {

            result.put(item.category(), item.obsrValue());
        }

        return result;
    }
}