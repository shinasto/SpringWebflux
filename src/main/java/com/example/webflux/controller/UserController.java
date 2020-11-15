package com.example.webflux.controller;

import com.example.webflux.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController("/webflux/users")
public class UserController {

    @Autowired
    RequestBodyValidator validator;


    @PostMapping("/test")
    public Mono<User> getDeviceById(@RequestBody Mono<User> user) {
        return this.validator.validate(User.class, user)
        .flatMap(u -> {
            System.out.println(u.getId());
            return Mono.just(u);
        }).map(u -> {
            System.out.println(u.getName());
            return u;
        }).map(u -> {
            u.setId("bbb");
            u.setName("chulwoo");
            return u;
        });
    }

    protected Mono<Errors> onValidationErrors(
            Errors errors) {
        return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                errors.getAllErrors().toString()));
    }
}
