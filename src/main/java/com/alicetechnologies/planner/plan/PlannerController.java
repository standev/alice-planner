package com.alicetechnologies.planner.plan;

import com.alicetechnologies.planner.PlannerApplication;
import com.alicetechnologies.planner.CriticalPathEngine;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = PlannerApplication.API_PLAN, produces = MediaType.APPLICATION_JSON_VALUE)
public class PlannerController {

    final CriticalPathEngine criticalPathEngine;
    final CrewMemberService crewMemberService;

    public PlannerController(
        final CriticalPathEngine criticalPathEngine,
        final CrewMemberService crewMemberService
    ) {
        this.criticalPathEngine = criticalPathEngine;
        this.crewMemberService = crewMemberService;
    }

    @GetMapping
    public PlanResponseDto getPlan() {
        return PlanResponseDto.builder()
            .totalDuration(criticalPathEngine.getTotalDuration())
            .maxCrewMembers(crewMemberService.getMaxCrewMembers(criticalPathEngine.getTasks()))
            .build();
    }

}
