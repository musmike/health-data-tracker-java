package mm.health_data_tracker.security;

import mm.health_data_tracker.dto.UserDto;
import mm.health_data_tracker.model.AppUser;
import mm.health_data_tracker.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("testuser");
        appUser.setPassword("hashedPassword");
        appUser.setEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(appUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistent"));
    }

    @Test
    void save_WhenUsernameExists_ShouldThrowException() {
        UserDto dto = new UserDto();
        dto.setUsername("testuser");
        dto.setEmail("new@example.com");
        dto.setPassword("pass");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userDetailsService.save(dto));
    }

    @Test
    void save_WhenEmailExists_ShouldThrowException() {
        UserDto dto = new UserDto();
        dto.setUsername("newuser");
        dto.setEmail("test@example.com");
        dto.setPassword("pass");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userDetailsService.save(dto));
    }

    @Test
    void save_WhenValid_ShouldSaveUser() {
        UserDto dto = new UserDto();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("plainPassword");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = userDetailsService.save(dto);

        assertNotNull(saved);
        assertEquals("newuser", saved.getUsername());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals("new@example.com", saved.getEmail());
    }
}
