package mm.health_data_tracker.service;

import mm.health_data_tracker.dto.TestResultDto;
import mm.health_data_tracker.model.AppUser;
import mm.health_data_tracker.model.TestParameter;
import mm.health_data_tracker.model.TestResult;
import mm.health_data_tracker.repository.AppUserRepository;
import mm.health_data_tracker.repository.TestParameterRepository;
import mm.health_data_tracker.repository.TestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestResultServiceTest {

    @Mock
    private TestResultRepository resultRepository;

    @Mock
    private TestParameterRepository parameterRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private TestResultService resultService;

    private AppUser user;
    private TestParameter parameter;
    private TestResult result;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");

        parameter = new TestParameter();
        parameter.setId(5L);
        parameter.setName("Glukoza");
        parameter.setUnit("mg/dl");
        parameter.setMinNorm(70.0);
        parameter.setMaxNorm(99.0);

        result = new TestResult();
        result.setId(100L);
        result.setValue(92.5);
        result.setTestDate(LocalDate.of(2026, 11, 1));
        result.setUser(user);
        result.setParameter(parameter);
    }

    @Test
    void getResultsForUser_ShouldReturnDtoList() {
        when(resultRepository.findByUser_UsernameOrderByTestDateDesc("testuser"))
                .thenReturn(List.of(result));

        List<TestResultDto> list = resultService.getResultsForUser("testuser");

        assertEquals(1, list.size());
        assertEquals(92.5, list.get(0).getValue());
        assertEquals("Glukoza", list.get(0).getParameterName());
        assertEquals(LocalDate.of(2026, 11, 1), list.get(0).getDate());
    }

    @Test
    void addResult_ShouldSaveAndReturnDto() {
        TestResultDto dto = new TestResultDto();
        dto.setParameterId(5L);
        dto.setValue(85.0);
        dto.setDate(LocalDate.of(2026, 11, 15));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(parameterRepository.findById(5L)).thenReturn(Optional.of(parameter));
        when(resultRepository.save(any(TestResult.class))).thenAnswer(invocation -> {
            TestResult r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        TestResultDto saved = resultService.addResult(dto, "testuser");

        assertNotNull(saved);
        assertEquals(101L, saved.getId());
        assertEquals(85.0, saved.getValue());
        assertEquals("Glukoza", saved.getParameterName());
        verify(resultRepository).save(any(TestResult.class));
    }

    @Test
    void deleteResult_WhenOwner_ShouldDelete() {
        when(resultRepository.findById(100L)).thenReturn(Optional.of(result));

        resultService.deleteResult(100L, "testuser");

        verify(resultRepository).delete(result);
    }

    @Test
    void deleteResult_WhenNotOwner_ShouldThrowException() {
        when(resultRepository.findById(100L)).thenReturn(Optional.of(result));

        assertThrows(RuntimeException.class, () ->
                resultService.deleteResult(100L, "otheruser"));

        verify(resultRepository, never()).delete(any());
    }
}
