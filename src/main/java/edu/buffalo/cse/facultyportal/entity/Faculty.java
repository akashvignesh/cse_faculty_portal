package edu.buffalo.cse.facultyportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty {

    @Id
    @Column(name = "person_number", length = 30)
    private String personNumber;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "standard_load", nullable = false, precision = 5, scale = 2)
    private BigDecimal standardLoad;

    @Column(name = "next_promotion_date")
    private LocalDate nextPromotionDate;

    @Column(name = "backup_faculty_person_number", length = 30)
    private String backupFacultyPersonNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_photo_document_id", referencedColumnName = "document_id")
    private Document profilePhotoDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_document_id", referencedColumnName = "document_id")
    private Document cvDocument;

    @Column(name = "created_at", nullable = false, updatable = false,
            insertable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
