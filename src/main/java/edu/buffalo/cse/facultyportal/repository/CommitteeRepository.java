package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitteeRepository extends org.springframework.data.repository.Repository<Faculty, String> {

    @Query(value = """
            SELECT c.name AS committeeName,
                   m.role AS role
            FROM committees.committees c
            JOIN committees.members m ON m.committee_id = c.id
            WHERE m.userid = :userId
            ORDER BY c.name ASC
            """, nativeQuery = true)
    List<CommitteeMembershipProjection> findCommitteesByUserId(@Param("userId") String userId);

    interface CommitteeMembershipProjection {
        String getCommitteeName();
        String getRole();
    }
}
