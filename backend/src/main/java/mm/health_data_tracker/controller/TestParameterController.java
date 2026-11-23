package mm.health_data_tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.health_data_tracker.dto.TestParameterDto;
import mm.health_data_tracker.service.TestParameterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/parameters")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Test Parameters", description = "Endpoints for managing medical test parameters and reference norms")
public class TestParameterController {

    private final TestParameterService parameterService;

    @GetMapping
    @Operation(summary = "Retrieve all accessible test parameters (system norms & user-defined)")
    public List<TestParameterDto> getParameters(Principal principal) {
        return parameterService.getAllParametersForUser(principal.getName());
    }

    @PostMapping
    @Operation(summary = "Define a new custom test parameter with unit and reference ranges")
    public ResponseEntity<TestParameterDto> addParameter(@RequestBody TestParameterDto dto, Principal principal) {
        return ResponseEntity.ok(parameterService.createPrivateParameter(dto, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a custom test parameter and its associated results")
    public ResponseEntity<Void> deleteParameter(@PathVariable Long id, Principal principal) {
        parameterService.deleteParameter(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing custom test parameter")
    public ResponseEntity<TestParameterDto> updateParameter(@PathVariable Long id,
                                    @RequestBody TestParameterDto dto, Principal principal) {
        return ResponseEntity.ok(parameterService.updateParameter(id, dto, principal.getName()));
    }
}