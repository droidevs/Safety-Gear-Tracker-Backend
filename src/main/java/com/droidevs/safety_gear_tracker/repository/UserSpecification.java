package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> isActive(boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active) {
                return criteriaBuilder.and(
                        criteriaBuilder.isTrue(root.get("isActiveByMaster")), // Changed from 'locked' to 'isActiveByMaster'
                        criteriaBuilder.isTrue(root.get("weeklyCodeVerified")),
                        criteriaBuilder.isTrue(root.get("otpVerified"))
                );
            } else {
                return criteriaBuilder.or(
                        criteriaBuilder.isFalse(root.get("isActiveByMaster")), // Changed from 'locked' to 'isActiveByMaster'
                        criteriaBuilder.isFalse(root.get("weeklyCodeVerified")),
                        criteriaBuilder.isFalse(root.get("otpVerified"))
                );
            }
        };
    }

    public static Specification<User> hasZone(Long zoneId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("zones").get("id"), zoneId);
    }
}
