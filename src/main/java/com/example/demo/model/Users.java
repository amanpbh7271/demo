package com.example.demo.model;

import com.couchbase.client.java.repository.annotation.Id;
import lombok.*;


@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
public class Users {

    @Id
    private String id;
    private String username;
    private String password;
    private String mobNumber;
}
