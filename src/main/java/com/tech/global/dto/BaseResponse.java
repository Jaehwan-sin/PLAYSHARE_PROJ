package com.tech.global.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BaseResponse {
    private String code;
    private String status;
    private Object data;
}
