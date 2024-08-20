package com.example.logtest.model;

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