package edu.buffalo.cse.facultyportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveTeachingPreferenceRequestItemDto {

    private String courseName;
    private String coursePref;
}
