package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.FacultyDetailDto;
import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.dto.PaginatedResponseDto;
import edu.buffalo.cse.facultyportal.dto.ProfilePhotoUpdateResponseDto;
import edu.buffalo.cse.facultyportal.entity.Document;
import org.springframework.web.multipart.MultipartFile;

public interface FacultyService {

    PaginatedResponseDto<FacultyListItemDto> listFaculty(int page, int size, String search);

    FacultyDetailDto getFacultyDetails(String personNumber);

    Document getProfilePhoto(String personNumber);

    ProfilePhotoUpdateResponseDto updateProfilePhoto(String personNumber,
                                                     MultipartFile profilePhoto);
}
