package edu.buffalo.cse.facultyportal.controller;

import edu.buffalo.cse.facultyportal.dto.ApiResponseDto;
import edu.buffalo.cse.facultyportal.dto.CommitteeMembershipDto;
import edu.buffalo.cse.facultyportal.service.CommitteeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/committees")
@RequiredArgsConstructor
public class CommitteeController {

    private final CommitteeService committeeService;

    @GetMapping("/memberships")
    public ResponseEntity<ApiResponseDto<List<CommitteeMembershipDto>>> getCommitteesByUserId(
            @RequestParam String userId) {
        List<CommitteeMembershipDto> result = committeeService.getCommitteesByUserId(userId);
        return ResponseEntity.ok(ApiResponseDto.success("Committee memberships fetched successfully", result));
    }
}
