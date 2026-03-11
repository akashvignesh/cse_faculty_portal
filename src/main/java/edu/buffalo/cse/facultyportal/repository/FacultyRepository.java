package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, String>,
        JpaSpecificationExecutor<Faculty> {

    Optional<Faculty> findByPersonNumber(String personNumber);
}
