package com.example.demo.model;

import com.couchbase.client.java.repository.annotation.Id;
import lombok.*;

import java.util.List;


@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
public class AccountDetails {

    @Id
    String name;
    String type;
    List<Managers> managers;
}
