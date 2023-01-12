package com.alicetechnologies.planner.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PlanResponseDto {

    int totalDuration;

    int maxCrewMembers;

}
