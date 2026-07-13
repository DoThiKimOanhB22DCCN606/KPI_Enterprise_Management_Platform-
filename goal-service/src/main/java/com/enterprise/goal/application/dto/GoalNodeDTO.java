package com.enterprise.goal.application.dto;

import com.enterprise.goal.domain.model.Goal;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GoalNodeDTO {
    private Goal goal;
    private List<GoalNodeDTO> children;
}
