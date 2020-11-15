package com.example.webflux.data;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class User {
    @NotEmpty
    String id;
    @NotEmpty
    String name;
}
