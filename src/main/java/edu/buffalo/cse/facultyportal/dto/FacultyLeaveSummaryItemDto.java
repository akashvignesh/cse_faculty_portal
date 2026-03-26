package edu.buffalo.cse.facultyportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FacultyLeaveSummaryItemDto {

    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String reason;
    private String backupFacultyPersonNumber;
}
