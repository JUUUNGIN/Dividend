package org.example.dividend.exception.impl;

import org.example.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NocompanyException extends AbstractException {

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMassage() {
        return "존재하지 않는 회사명 입니다.";
    }


}
