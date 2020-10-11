package com.example.webflux.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("Device")
public class Device {
    @Id
    private Long id;
    private String name;
    private Type type;
    private String description;

    public static enum Type {
        WALLPAD, LOBBY
    }
}
