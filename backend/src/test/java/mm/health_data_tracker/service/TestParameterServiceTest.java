package mm.health_data_tracker.service;

import mm.health_data_tracker.dto.TestParameterDto;
import mm.health_data_tracker.model.AppUser;
import mm.health_data_tracker.model.TestParameter;
import mm.health_data_tracker.repository.AppUserRepository;
import mm.health_data_tracker.repository.TestParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestParameterServiceTest {

    @Mock
    private TestParameterRepository parameterRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private TestParameterService parameterService;

    private AppUser user;
    private TestParameter parameter;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");

        parameter = new TestParameter();
        parameter.setId(10L);
        parameter.setName("Glukoza");
        parameter.setUnit("mg/dl");
        parameter.setMinNorm(70.0);
        parameter.setMaxNorm(99.0);
        parameter.setCreator(user);
    }

    @Test
    void getAllParametersForUser_ShouldReturnDtoList() {
        when(parameterRepository.findSystemAndUserParameters("testuser"))
                .thenReturn(List.of(parameter));

        List<TestParameterDto> result = parameterService.getAllParametersForUser("testuser");

        assertEquals(1, result.size());
        assertEquals("Glukoza", result.get(0).getName());
        assertEquals(70.0, result.get(0).getMinNorm());
        assertTrue(result.get(0).isPrivateParam());
    }

    @Test
    void createPrivateParameter_ShouldSaveAndReturnDto() {
        TestParameterDto dto = new TestParameterDto();
        dto.setName("Żelazo");
        dto.setUnit("µg/dl");
        dto.setMinNorm(60.0);
        dto.setMaxNorm(170.0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(parameterRepository.save(any(TestParameter.class))).thenAnswer(invocation -> {
            TestParameter p = invocation.getArgument(0);
            p.setId(20L);
            return p;
        });

        TestParameterDto created = parameterService.createPrivateParameter(dto, "testuser");

        assertNotNull(created);
        assertEquals(20L, created.getId());
        assertEquals("Żelazo", created.getName());
        assertTrue(created.isPrivateParam());
        verify(parameterRepository).save(any(TestParameter.class));
    }

    @Test
    void createPrivateParameter_WithInvalidNorms_ShouldThrowException() {
        TestParameterDto dto = new TestParameterDto();
        dto.setName("Złe normy");
        dto.setMinNorm(100.0);
        dto.setMaxNorm(50.0); // min > max

        assertThrows(IllegalArgumentException.class, () ->
                parameterService.createPrivateParameter(dto, "testuser"));
    }

    @Test
    void deleteParameter_WhenOwner_ShouldDelete() {
        when(parameterRepository.findById(10L)).thenReturn(Optional.of(parameter));

        parameterService.deleteParameter(10L, "testuser");

        verify(parameterRepository).delete(parameter);
    }

    @Test
    void deleteParameter_WhenNotOwner_ShouldThrowException() {
        when(parameterRepository.findById(10L)).thenReturn(Optional.of(parameter));

        assertThrows(RuntimeException.class, () ->
                parameterService.deleteParameter(10L, "otheruser"));

        verify(parameterRepository, never()).delete(any());
    }
}
