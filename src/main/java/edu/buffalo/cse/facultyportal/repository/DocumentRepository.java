package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
