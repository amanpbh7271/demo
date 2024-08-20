package com.example.logtest.model;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class INC_USERS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int user_id;
    String username;
    String mobile;
    String ntnet;

    // These fields are not stored in the INC_USERS table but will be used to link records
    @Transient
    List<Integer> regionIds;
    @Transient
    List<Integer> accountIds;
}
