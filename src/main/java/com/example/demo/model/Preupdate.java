package com.example.demo.model;


import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
public class Preupdate {
    String timestamp;
    String message;
}
