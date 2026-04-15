package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.FacultyDetailDto;
import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingHistoryResponseDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.dto.PaginatedResponseDto;
import edu.buffalo.cse.facultyportal.dto.ProfilePhotoUpdateResponseDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesRequestDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.entity.Document;
import org.springframework.web.multipart.MultipartFile;

public interface FacultyService {

    PaginatedResponseDto<FacultyListItemDto> listFaculty(int page, int size, String search);

    FacultyDetailDto getFacultyDetails(String personNumber);

    FacultyTeachingHistoryResponseDto getTeachingHistory(String facultySourceKey);

    FacultyTeachingPreferencesResponseDto getTeachingPreferences(String facultyId);

    SaveTeachingPreferencesResponseDto saveTeachingPreferences(
            String facultyId,
            SaveTeachingPreferencesRequestDto request);

    Document getProfilePhoto(String personNumber);

    ProfilePhotoUpdateResponseDto updateProfilePhoto(String personNumber,
                                                     MultipartFile profilePhoto);
}
