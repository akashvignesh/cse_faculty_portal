package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveCourseRepository extends org.springframework.data.repository.Repository<Faculty, String> {

    @Query(value = """
            SELECT TRIM(cc.primarysubject) AS subject,
                   TRIM(cc.primarycatalognumber) AS primaryCatalogNumber,
                   cc.coursetitlelong AS courseTitleLong
            FROM ps_rpt.ps_course_catalog_v cc
            WHERE cc.effectivestatus = 'A'
            ORDER BY TRIM(cc.primarysubject) ASC,
                     TRIM(cc.primarycatalognumber) ASC
            """, nativeQuery = true)
    List<ActiveCourseProjection> findActiveCourses();

    interface ActiveCourseProjection {
        String getSubject();

        String getPrimaryCatalogNumber();

        String getCourseTitleLong();
    }
}