package com.example.logtest.model;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    private String id;
    @Getter @Setter private String username;
    @Getter @Setter private String password;
    @Getter @Setter private String mobNumber;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @Getter @Setter private List<Account> account;

    // If needed, you can add custom methods here
}