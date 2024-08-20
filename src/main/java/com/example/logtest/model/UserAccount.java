package com.example.logtest.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "useraccounts")
public class UserAccount {
    @Id
    int user_id;
    int account_id;
}
