package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.dto.PaginatedResponseDto;
import edu.buffalo.cse.facultyportal.dto.ProfilePhotoUpdateResponseDto;
import edu.buffalo.cse.facultyportal.entity.Document;
import edu.buffalo.cse.facultyportal.entity.Faculty;
import edu.buffalo.cse.facultyportal.exception.InvalidFileException;
import edu.buffalo.cse.facultyportal.exception.ResourceNotFoundException;
import edu.buffalo.cse.facultyportal.mapper.FacultyMapper;
import edu.buffalo.cse.facultyportal.repository.DocumentRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyRepository;
import edu.buffalo.cse.facultyportal.repository.FacultySpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {

    private static final long MAX_FILE_SIZE = 10_485_760L; // 10 MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final FacultyRepository facultyRepository;
    private final DocumentRepository documentRepository;
    private final FacultyMapper facultyMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<FacultyListItemDto> listFaculty(int page, int size, String search) {
        Sort sort = Sort.by(
                Sort.Order.asc("fullName"),
                Sort.Order.asc("personNumber"));
        Pageable pageable = PageRequest.of(page, size, sort);

        List<String> tokens = parseSearchTokens(search);
        Specification<Faculty> spec = FacultySpecifications.fullNameContainsAllTokens(tokens);

        Page<Faculty> facultyPage = facultyRepository.findAll(spec, pageable);

        List<FacultyListItemDto> content = facultyPage.getContent().stream()
                .map(facultyMapper::toListItemDto)
                .toList();

        return PaginatedResponseDto.<FacultyListItemDto>builder()
                .content(content)
                .page(facultyPage.getNumber())
                .size(facultyPage.getSize())
                .totalElements(facultyPage.getTotalElements())
                .totalPages(facultyPage.getTotalPages())
                .hasNext(facultyPage.hasNext())
                .hasPrevious(facultyPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Document getProfilePhoto(String personNumber) {
        Faculty faculty = findFacultyOrThrow(personNumber);

        // Accessing the proxy object itself is safe (not null check), but we need to
        // get the FK value without triggering lazy initialization of the full proxy.
        // getDocumentId() on a Hibernate proxy is safe — Hibernate knows the PK without
        // issuing a SELECT. We then fetch the real Document row inside this transaction.
        Document photoProxy = faculty.getProfilePhotoDocument();
        if (photoProxy == null) {
            throw new ResourceNotFoundException(
                    "No profile photo linked for faculty: " + personNumber);
        }

        Long documentId = photoProxy.getDocumentId();
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile photo document not found for faculty: " + personNumber));
    }

    @Override
    @Transactional
    public ProfilePhotoUpdateResponseDto updateProfilePhoto(String personNumber,
                                                            MultipartFile profilePhoto) {
        if (profilePhoto == null || profilePhoto.isEmpty()) {
            throw new InvalidFileException("Profile photo file must be provided");
        }

        validateFile(profilePhoto, ALLOWED_IMAGE_TYPES, "Profile photo");

        Faculty faculty = findFacultyOrThrow(personNumber);

        Document photoDoc = upsertDocument(
                faculty.getProfilePhotoDocument(),
                profilePhoto,
                "PROFILE_PHOTO");
        faculty.setProfilePhotoDocument(photoDoc);
        facultyRepository.save(faculty);

        return ProfilePhotoUpdateResponseDto.builder()
                .personNumber(faculty.getPersonNumber())
                .profilePhotoDocumentId(photoDoc.getDocumentId())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Faculty findFacultyOrThrow(String personNumber) {
        return facultyRepository.findByPersonNumber(personNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Faculty not found with person number: " + personNumber));
    }

    private List<String> parseSearchTokens(String search) {
        if (search == null || search.isBlank()) {
            return List.of();
        }
        return Arrays.stream(search.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, String label) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    label + " exceeds maximum allowed size of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    label + " has invalid file type: " + contentType
                            + ". Allowed types: " + allowedTypes);
        }
    }

    /**
     * If an existing document row is linked, overwrite it in place.
     * Otherwise, create a new document row.
     */
    private Document upsertDocument(Document existing, MultipartFile file, String documentType) {
        try {
            byte[] data = file.getBytes();
            String fileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : documentType.toLowerCase() + "_upload";

            if (existing != null) {
                existing.setDocumentName(fileName);
                existing.setDocumentType(documentType);
                existing.setMimeType(file.getContentType());
                existing.setFileSizeBytes(file.getSize());
                existing.setFileData(data);
                return documentRepository.save(existing);
            }

            Document doc = Document.builder()
                    .documentName(fileName)
                    .documentType(documentType)
                    .mimeType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .fileData(data)
                    .build();
            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }
}
