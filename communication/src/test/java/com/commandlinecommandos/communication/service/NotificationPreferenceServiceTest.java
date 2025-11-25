package com.commandlinecommandos.communication.service;

import com.commandlinecommandos.communication.model.NotificationPreference;
import com.commandlinecommandos.communication.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private NotificationPreferenceService preferenceService;

    private Long testUserId = 123L;
    private NotificationPreference testPreference;

    @BeforeEach
    void setUp() {
        testPreference = new NotificationPreference(
            testUserId, true, "user@example.com", "John"
        );
        testPreference.setPreferenceId(1L);
        testPreference.setCreatedAt(LocalDateTime.now());
        testPreference.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetPreference_ExistingPreference() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPreference));

        // Act
        NotificationPreference result = preferenceService.getPreference(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testPreference, result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(true, result.getEmailNotificationsEnabled());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        verify(preferenceRepository).findByUserId(testUserId);
        verify(preferenceRepository, never()).save(any(NotificationPreference.class));
    }

    @Test
    void testGetPreference_NoPreferenceFound_ReturnsDefault() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // Act
        NotificationPreference result = preferenceService.getPreference(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(false, result.getEmailNotificationsEnabled());
        assertNull(result.getEmail());
        assertNull(result.getFirstName());
        verify(preferenceRepository).findByUserId(testUserId);
        verify(preferenceRepository, never()).save(any(NotificationPreference.class));
    }

    @Test
    void testUpdatePreference_ExistingPreference() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

        Boolean newEmailEnabled = false;
        String newEmail = "newemail@example.com";
        String newFirstName = "Jane";

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, newEmailEnabled, newEmail, newFirstName
        );

        // Assert
        assertNotNull(result);
        assertEquals(testPreference, result);
        verify(preferenceRepository).findByUserId(testUserId);
        verify(preferenceRepository).save(testPreference);
        assertEquals(newEmailEnabled, testPreference.getEmailNotificationsEnabled());
        assertEquals(newEmail, testPreference.getEmail());
        assertEquals(newFirstName, testPreference.getFirstName());
    }

    @Test
    void testUpdatePreference_CreateNewPreference() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        
        NotificationPreference newPreference = new NotificationPreference(
            testUserId, true, "newuser@example.com", "Alice"
        );
        newPreference.setPreferenceId(2L);
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(newPreference);

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, true, "newuser@example.com", "Alice"
        );

        // Assert
        assertNotNull(result);
        assertEquals(newPreference, result);
        verify(preferenceRepository).findByUserId(testUserId);
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void testUpdatePreference_WithNullValues() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, false, null, null
        );

        // Assert
        assertNotNull(result);
        verify(preferenceRepository).findByUserId(testUserId);
        verify(preferenceRepository).save(testPreference);
        assertEquals(false, testPreference.getEmailNotificationsEnabled());
        assertNull(testPreference.getEmail());
        assertNull(testPreference.getFirstName());
    }

    @Test
    void testUpdatePreference_EnableNotifications() {
        // Arrange
        NotificationPreference disabledPreference = new NotificationPreference(
            testUserId, false, "user@example.com", "John"
        );
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(disabledPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(disabledPreference);

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, true, "user@example.com", "John"
        );

        // Assert
        assertNotNull(result);
        assertEquals(true, disabledPreference.getEmailNotificationsEnabled());
        verify(preferenceRepository).save(disabledPreference);
    }

    @Test
    void testUpdatePreference_DisableNotifications() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, false, "user@example.com", "John"
        );

        // Assert
        assertNotNull(result);
        assertEquals(false, testPreference.getEmailNotificationsEnabled());
        verify(preferenceRepository).save(testPreference);
    }

    @Test
    void testUpdatePreference_UpdateEmail() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

        String newEmail = "updated@example.com";

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, true, newEmail, "John"
        );

        // Assert
        assertNotNull(result);
        assertEquals(newEmail, testPreference.getEmail());
        verify(preferenceRepository).save(testPreference);
    }

    @Test
    void testUpdatePreference_UpdateFirstName() {
        // Arrange
        when(preferenceRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

        String newFirstName = "UpdatedName";

        // Act
        NotificationPreference result = preferenceService.updatePreference(
            testUserId, true, "user@example.com", newFirstName
        );

        // Assert
        assertNotNull(result);
        assertEquals(newFirstName, testPreference.getFirstName());
        verify(preferenceRepository).save(testPreference);
    }
}

