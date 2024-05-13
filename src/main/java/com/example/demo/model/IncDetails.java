package com.example.demo.model;

import com.couchbase.client.java.repository.annotation.Id;
import lombok.*;

import java.util.List;


@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
public class IncDetails {
    @Id
    String incNumber;
    String account;
    String nextUpdate;
    String status;
    String businessImpact;
    String workAround;
    String manager;
    String issueOwnedBy;
    String bridgeDetails;
    String priority;
    String date;
    String time;
    List<Preupdate> preUpdates;
}


