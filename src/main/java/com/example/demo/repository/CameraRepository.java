package com.example.demo.repository;

import com.example.demo.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraRepository extends JpaRepository<Camera, String> {
    // Spring Data JPA provides basic CRUD operations out of the box
    // You can add custom query methods here if needed (e.g., findByName)
}