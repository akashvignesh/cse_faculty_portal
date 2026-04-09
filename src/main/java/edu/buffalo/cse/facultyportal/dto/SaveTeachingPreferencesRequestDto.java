package edu.buffalo.cse.facultyportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveTeachingPreferencesRequestDto {

    private String facultyId;
    private List<SaveTeachingPreferenceRequestItemDto> preferences;
}
