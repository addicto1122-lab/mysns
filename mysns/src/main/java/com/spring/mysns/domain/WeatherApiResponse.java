package com.spring.mysns.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            Header header,
            Body body
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Header(
                String resultCode,
                String resultMsg
        ) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Body(
                String dataType,
                Items items
        ) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Items(
                List<Item> item
        ) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Item(
                String category,
                String obsrValue
        ) {}
    }
}