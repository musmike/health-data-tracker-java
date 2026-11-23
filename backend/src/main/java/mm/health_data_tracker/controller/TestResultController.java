package mm.health_data_tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.health_data_tracker.dto.TestResultDto;
import mm.health_data_tracker.service.TestResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Test Results", description = "Endpoints for recording, retrieving and managing medical test results")
public class TestResultController {

    private final TestResultService resultService;

    @GetMapping
    @Operation(summary = "Retrieve all historical test results for authenticated user")
    public List<TestResultDto> getMyResults(Principal principal) {
        return resultService.getResultsForUser(principal.getName());
    }

    @PostMapping
    @Operation(summary = "Record a new medical test result with date, value, and parameter")
    public ResponseEntity<TestResultDto> addResult(@RequestBody TestResultDto dto, Principal principal) {
        return ResponseEntity.ok(resultService.addResult(dto, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a specific test result record")
    public ResponseEntity<Void> deleteResult(@PathVariable Long id, Principal principal) {
        resultService.deleteResult(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing test result value or date")
    public ResponseEntity<TestResultDto> updateResult(@PathVariable Long id,
                              @RequestBody TestResultDto dto, Principal principal) {
        return ResponseEntity.ok(resultService.updateResult(id, dto, principal.getName()));
    }
}