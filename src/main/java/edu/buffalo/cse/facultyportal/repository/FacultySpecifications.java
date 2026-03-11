package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class FacultySpecifications {

    private FacultySpecifications() {
    }

    /**
     * Builds a specification that requires every token to appear somewhere
     * in {@code full_name} (case-insensitive LIKE '%token%').
     */
    public static Specification<Faculty> fullNameContainsAllTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        Specification<Faculty> spec = (root, query, cb) -> cb.conjunction();
        for (String token : tokens) {
            String pattern = "%" + token.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("fullName")), pattern));
        }
        return spec;
    }
}
