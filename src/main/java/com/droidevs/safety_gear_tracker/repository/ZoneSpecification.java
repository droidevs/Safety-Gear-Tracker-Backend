package com.droidevs.safety_gear_tracker.repository;

import com.droidevs.safety_gear_tracker.model.Zone;
import org.springframework.data.jpa.domain.Specification;

public class ZoneSpecification {

    public static Specification<Zone> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
            );
        };
    }
}
