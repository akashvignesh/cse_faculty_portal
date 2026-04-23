package edu.buffalo.cse.facultyportal.repository;

import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyDetailRepository
        extends org.springframework.data.repository.Repository<Faculty, String> {

    @Query(value = """
            SELECT a.official_job_title AS title,
                   a.appointment_type AS rankName
            FROM cfp_appointments a
            WHERE a.faculty_person_number = :personNumber
            ORDER BY CASE WHEN a.appointment_end_date IS NULL THEN 0 ELSE 1 END ASC,
                     a.appointment_effective_date DESC,
                     a.appointment_id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<CurrentAppointmentProjection> findCurrentAppointment(
            @Param("personNumber") String personNumber);

    @Query(value = """
            SELECT fe.email_address AS emailAddress
            FROM cfp_faculty_emails fe
            WHERE fe.faculty_person_number = :personNumber
              AND fe.email_type = 'work'
              AND fe.is_primary = 1
            ORDER BY fe.faculty_email_id ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PrimaryEmailProjection> findPrimaryWorkEmail(
            @Param("personNumber") String personNumber);

    @Query(value = """
            SELECT fp.phone_number AS phoneNumber
            FROM cfp_faculty_phone_numbers fp
            WHERE fp.faculty_person_number = :personNumber
              AND fp.phone_type = 'office'
              AND fp.is_primary = 1
            ORDER BY fp.faculty_phone_id ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PrimaryPhoneProjection> findPrimaryOfficePhone(
            @Param("personNumber") String personNumber);

    @Query(value = """
            SELECT fa.address_line1 AS line1,
                   fa.city AS city,
                   fa.state_province AS state,
                   fa.postal_code AS postalCode,
                   fa.country AS country
            FROM cfp_faculty_addresses fa
            WHERE fa.faculty_person_number = :personNumber
              AND fa.address_type = 'office'
            ORDER BY fa.faculty_address_id ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<OfficeAddressProjection> findOfficeAddress(
            @Param("personNumber") String personNumber);

    @Query(value = """
            SELECT ram.area_name AS areaName
            FROM cfp_faculty_research_areas fra
            JOIN cfp_research_area_master ram
              ON ram.research_area_id = fra.research_area_id
            WHERE fra.faculty_person_number = :personNumber
            ORDER BY fra.faculty_research_area_id ASC
            """, nativeQuery = true)
    List<ResearchAreaProjection> findResearchAreas(@Param("personNumber") String personNumber);

    @Query(value = """
            SELECT tr.term_code AS termCode,
                   tr.reduction_type AS reductionType,
                   tr.reduction_amount AS reductionAmount,
                   tr.reason AS reason,
                   tr.approval_document_id AS approvalDocumentId,
                   tr.created_at AS createdAt
            FROM cfp_teaching_reductions tr
            WHERE tr.faculty_person_number = :personNumber
            ORDER BY tr.term_code DESC,
                     tr.teaching_reduction_id DESC
            """, nativeQuery = true)
    List<TeachingReductionProjection> findTeachingReductions(
            @Param("personNumber") String personNumber);

    @Query(value = """
            SELECT fl.leave_type AS leaveType,
                   fl.start_date AS startDate,
                   fl.end_date AS endDate,
                   fl.location AS location,
                   fl.reason AS reason,
                   fl.backup_faculty_person_number AS backupFacultyPersonNumber
            FROM cfp_faculty_leave fl
            WHERE fl.faculty_person_number = :personNumber
            ORDER BY fl.start_date DESC,
                     fl.leave_id DESC
            """, nativeQuery = true)
    List<LeaveSummaryProjection> findLeaveSummary(@Param("personNumber") String personNumber);

    @Query(value = """
            SELECT s.person_number AS studentPersonNumber,
                   s.full_name AS fullName,
                   s.program AS program
            FROM cfp_students s
            WHERE s.advisor_faculty_person_number = :personNumber
            ORDER BY s.full_name ASC,
                     s.person_number ASC
            """, nativeQuery = true)
    List<StudentProjection> findStudentsUnderProfessor(
            @Param("personNumber") String personNumber);

    interface CurrentAppointmentProjection {
        String getTitle();

        String getRankName();
    }

    interface PrimaryEmailProjection {
        String getEmailAddress();
    }

    interface PrimaryPhoneProjection {
        String getPhoneNumber();
    }

    interface OfficeAddressProjection {
        String getLine1();

        String getCity();

        String getState();

        String getPostalCode();

        String getCountry();
    }

    interface ResearchAreaProjection {
        String getAreaName();
    }

        interface TeachingReductionProjection {
                String getTermCode();

                String getReductionType();

                BigDecimal getReductionAmount();

                String getReason();

                Long getApprovalDocumentId();

                LocalDateTime getCreatedAt();
        }

    interface LeaveSummaryProjection {
        String getLeaveType();

        LocalDate getStartDate();

        LocalDate getEndDate();

        String getLocation();

        String getReason();

        String getBackupFacultyPersonNumber();
    }

    interface StudentProjection {
        String getStudentPersonNumber();

        String getFullName();

        String getProgram();
    }
}
