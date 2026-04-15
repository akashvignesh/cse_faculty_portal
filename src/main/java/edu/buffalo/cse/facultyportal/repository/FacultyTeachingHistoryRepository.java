package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacultyTeachingHistoryRepository
        extends org.springframework.data.repository.Repository<Faculty, String> {

    @Query(value = """
            SELECT cs.faculty AS faculty,
                   cs.facultysourcekey AS facultySourceKey,
                   cs.classnumber AS classNumber,
                   cs.termsourcekey AS termSourceKey,
                   cs.coursetype AS courseType,
                   cs.coursecareersourcekey AS courseCareerSourceKey,
                   cs.courseid AS courseId,
                   cc.primarycatalognumber AS primaryCatalogNumber,
                   cc.coursetitlelong AS courseTitleLong,
                   cc.effectivestatus AS effectiveStatus,
                   cc.effectivedate AS effectiveDate
            FROM ps_rpt.classschedule_v cs
            LEFT JOIN (
                SELECT ranked.crse_id,
                       ranked.primarycatalognumber,
                       ranked.coursetitlelong,
                       ranked.effectivestatus,
                       ranked.effectivedate
                FROM (
                    SELECT cc.crse_id,
                           cc.primarycatalognumber,
                           cc.coursetitlelong,
                           cc.effectivestatus,
                           cc.effectivedate,
                           ROW_NUMBER() OVER (
                               PARTITION BY cc.crse_id
                               ORDER BY cc.effectivedate DESC
                           ) AS rn
                    FROM ps_rpt.ps_course_catalog_v cc
                    WHERE cc.effectivestatus = 'A'
                      AND cc.effectivedate <= CURRENT_DATE
                ) ranked
                WHERE ranked.rn = 1
            ) cc
              ON cc.crse_id = cs.courseid
            WHERE cs.facultysourcekey = :facultySourceKey
            ORDER BY cs.termsourcekey DESC,
                     cs.classnumber ASC,
                     cs.courseid ASC
            """, nativeQuery = true)
    List<TeachingHistoryProjection> findTeachingHistory(
            @Param("facultySourceKey") String facultySourceKey);

    interface TeachingHistoryProjection {
        String getFaculty();

        String getFacultySourceKey();

        String getClassNumber();

        String getTermSourceKey();

        String getCourseType();

        String getCourseCareerSourceKey();

        String getCourseId();

        String getPrimaryCatalogNumber();

        String getCourseTitleLong();

        String getEffectiveStatus();

        LocalDate getEffectiveDate();
    }
}
