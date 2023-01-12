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

    public PlannerController(final CriticalPathEngine criticalPathEngine) {
        this.criticalPathEngine = criticalPathEngine;
    }

    @GetMapping
    public PlanResponseDto getPlan() {
        return PlanResponseDto.builder()
            .totalDuration(criticalPathEngine.getMaxCost())
            .maxCrewMembers(criticalPathEngine.getMaxCrewMembers())
            .build();
    }

}
