package com.eduardo.rpg.User.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eduardo.rpg.User.DTO.CreateUserRequest;
import com.eduardo.rpg.User.DTO.UserMapper;
import com.eduardo.rpg.User.DTO.UserResponseDTO;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.exception.UserAlreadyExistsException;

@DisplayName("UserService Unit Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest("testuser", "test@example.com", "password123");
        user = new User(1L, "testuser", "test@example.com", "encoded_password", Role.PLAYER, null, null);
        userResponseDTO = new UserResponseDTO(1L, "testuser", "test@example.com", Role.PLAYER, null);
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(new User(null, "testuser", "test@example.com", "password123", null, null, null));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(createUserRequest);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username exists")
    void testCreateUserWithExistingUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email exists")
    void testCreateUserWithExistingEmail() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void testFindUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("testuser", result.username());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by id")
    void testFindUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(1L));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when id is null")
    void testFindUserByIdNull() {
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(null));
    }

    @Test
    @DisplayName("Should find all users successfully")
    void testFindAllUsersSuccess() {
        User user2 = new User(2L, "testuser2", "test2@example.com", "encoded_password", Role.PLAYER, null, null);
        UserResponseDTO userResponseDTO2 = new UserResponseDTO(2L, "testuser2", "test2@example.com", Role.PLAYER, null);

        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(user, user2)));
        when(userMapper.toResponse(user)).thenReturn(userResponseDTO);
        when(userMapper.toResponse(user2)).thenReturn(userResponseDTO2);

        Page<UserResponseDTO> result = userService.findAllUsers(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        verify(userRepository, times(1)).findAll(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    void testFindAllUsersEmpty() {
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));

        Page<UserResponseDTO> result = userService.findAllUsers(PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting with null id")
    void testDeleteUserWithNullId() {
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(null));
    }
}

