
package com.droidevs.safety_gear_tracker.auth.service;

import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationRequest;
import com.droidevs.safety_gear_tracker.auth.dtos.AuthenticationResponse;

public interface MasterUserAuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
