package com.droidevs.safety_gear_tracker.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CameraPagingRequestDto extends PagingRequestDto {
    private String zone;
    private ManagementStatus managementStatus;
    private ActivationStatus activationStatus;
}
