package com.example.webflux.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class RequestBodyValidator {

    @Autowired
    Validator validator;

    public <T> Mono<T> validate(Class<T> clazz, Mono<T> body) {

        return body.flatMap(d -> {
            Errors errors = new BeanPropertyBindingResult(d, clazz.getName());

            this.validator.validate(d, errors);

            if (errors == null || errors.getAllErrors().isEmpty()) {
                return Mono.just(d);
            } else {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        errors.getAllErrors().toString()));
            }
        });
    }
}
