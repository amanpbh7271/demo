package com.example.logtest.model;


import lombok.*;

import javax.persistence.Id;
import java.sql.Date;
import java.util.List;


@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
public class IncDetails {
    @Id
    String incNumber;
    String region;
    String account;
    String nextUpdate;
    String status;
    String businessImpact;
    String workAround;
    String manager;
    String issueOwnedBy;
    String bridgeDetails;
    String priority;
    String problemStatement;
    Date date;
    String time;
    Date impactStartDate;
    String impactStartTime;
    Date impactEndDate;
    String impactEndTime;
    String minutesOfOutage;
    List<Preupdate> preUpdates;
    String rootCause;

    // New fields
    String affectedServices;
    String problemIdentified; // yes or no
    String escalatedLevel;
    String expertsContacted;
    String updateFrequency; // 10, 15 min time period
    String checkedWithOtherAccounts;
    String coreExpertsInvolved;
    String etaForResolution;
    Boolean isEditing;
    String newIncNumber;
}

