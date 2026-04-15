package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyTeachingPreferenceRepository
        extends org.springframework.data.repository.Repository<Faculty, String> {

    @Query(value = """
            SELECT ftp.userid AS facultyId,
                   ftp.crse_id AS courseId,
                   ftp.pref AS pref,
                   cc.primarycatalognumber AS primaryCatalogNumber,
                   cc.coursetitlelong AS courseTitleLong
                                                FROM people.cfp_faculty_teaching_prefs ftp
            LEFT JOIN ps_rpt.ps_course_catalog_v cc
              ON cc.crse_id = ftp.crse_id
            WHERE ftp.userid = :facultyId
            ORDER BY ftp.crse_id
            """, nativeQuery = true)
    List<TeachingPreferenceProjection> findTeachingPreferences(
            @Param("facultyId") String facultyId);

    @Query(value = """
            SELECT cc.crse_id AS courseId,
                   cc.primarycatalognumber AS primaryCatalogNumber,
                   cc.coursetitlelong AS courseTitleLong
            FROM ps_rpt.ps_course_catalog_v cc
            WHERE TRIM(cc.primarycatalognumber) = :primaryCatalogNumber
              AND TRIM(cc.coursetitlelong) = :courseTitleLong
            """, nativeQuery = true)
    List<CourseCatalogProjection> findCoursesByCourseName(
            @Param("primaryCatalogNumber") String primaryCatalogNumber,
            @Param("courseTitleLong") String courseTitleLong);

    @Query(value = """
            SELECT COUNT(*)
                                                FROM people.cfp_faculty_teaching_prefs ftp
            WHERE ftp.userid = :facultyId
              AND ftp.crse_id = :courseId
            """, nativeQuery = true)
    long countTeachingPreference(
            @Param("facultyId") String facultyId,
            @Param("courseId") String courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                        UPDATE people.cfp_faculty_teaching_prefs
            SET pref = :pref,
                editor = :editor,
                ts = CURRENT_TIMESTAMP
            WHERE userid = :facultyId
              AND crse_id = :courseId
            """, nativeQuery = true)
    int updateTeachingPreference(
            @Param("facultyId") String facultyId,
            @Param("courseId") String courseId,
            @Param("pref") int pref,
            @Param("editor") String editor);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO people.cfp_faculty_teaching_prefs (userid, crse_id, pref, editor, ts)
            VALUES (:facultyId, :courseId, :pref, :editor, CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    int insertTeachingPreference(
            @Param("facultyId") String facultyId,
            @Param("courseId") String courseId,
            @Param("pref") int pref,
            @Param("editor") String editor);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                                                DELETE FROM people.cfp_faculty_teaching_prefs
            WHERE userid = :facultyId
              AND crse_id = :courseId
            """, nativeQuery = true)
    int deleteTeachingPreference(
            @Param("facultyId") String facultyId,
            @Param("courseId") String courseId);

    interface TeachingPreferenceProjection {
        String getFacultyId();

        String getCourseId();

        Integer getPref();

        String getPrimaryCatalogNumber();

        String getCourseTitleLong();
    }

    interface CourseCatalogProjection {
        String getCourseId();

        String getPrimaryCatalogNumber();

        String getCourseTitleLong();
    }
}
