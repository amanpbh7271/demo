package com.example.demo.model;

import lombok.*;

import javax.persistence.Id;
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
