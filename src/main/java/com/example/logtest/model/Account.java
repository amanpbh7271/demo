package com.example.logtest.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    private String id;
    @Getter @Setter private String name;



    // If needed, you can add custom methods here
}