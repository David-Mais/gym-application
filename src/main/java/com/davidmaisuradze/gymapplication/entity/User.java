package com.davidmaisuradze.gymapplication.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder
public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive;
}
