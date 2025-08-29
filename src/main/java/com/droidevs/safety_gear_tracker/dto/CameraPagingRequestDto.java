package com.droidevs.safety_gear_tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CameraPagingRequestDto extends PagingRequestDto {
    private String zone;
    
    @JsonProperty("management_status")
    private ManagementStatus managementStatus;
    @JsonProperty("activation_status")
    private ActivationStatus activationStatus;
}
