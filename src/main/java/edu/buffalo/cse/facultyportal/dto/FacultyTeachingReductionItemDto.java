package edu.buffalo.cse.facultyportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FacultyTeachingReductionItemDto {

    private String termCode;
    private String reductionType;
    private BigDecimal reductionAmount;
    private String reason;
    private Long approvalDocumentId;
    private LocalDate createdAt;
}