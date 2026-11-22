package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTests {

    @Mock UserRepository userRepository;

    private UserRoleService userRoleService;

    private User actingAdmin;
    private User student;

    @BeforeEach
    void setUp() {
        userRoleService = new UserRoleService(userRepository);

        actingAdmin = user(1L, "Zara Admin", Role.ADMIN);
        student = user(7L, "Anna Andersson", Role.STUDENT);
    }

    private User user(long id, String fullName, Role role) {
        User user = new User();
        user.setId(id);
        user.setFullName(fullName);
        user.setEmail(fullName.toLowerCase().replace(' ', '.') + "@example.com");
        user.setRole(role);
        return user;
    }

    /** The service always looks the caller up by the email from the JWT. */
    private void actingAs(User admin) {
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
    }

    @Test
    void promotingAStudentMakesThemAnAdministrator() {
        actingAs(actingAdmin);
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(student));

        LearnerSummaryResponse result = userRoleService.changeRole(7L, Role.ADMIN, actingAdmin.getEmail());

        assertEquals(Role.ADMIN, student.getRole());
        assertEquals(new LearnerSummaryResponse(7L, "Anna Andersson", Role.ADMIN), result);
        verify(userRepository).save(student);
    }

    @Test
    void demotingAnAdministratorMakesThemAStudent() {
        User other = user(5L, "Bertil Berg", Role.ADMIN);
        actingAs(actingAdmin);
        when(userRepository.findById(5L)).thenReturn(Optional.of(other));
        when(userRepository.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(actingAdmin, other));

        LearnerSummaryResponse result = userRoleService.changeRole(5L, Role.STUDENT, actingAdmin.getEmail());

        assertEquals(Role.STUDENT, other.getRole());
        assertEquals(new LearnerSummaryResponse(5L, "Bertil Berg", Role.STUDENT), result);
        verify(userRepository).save(other);
    }

    @Test
    void theLastAdministratorCannotBeDemoted() {
        // The locked list holds only the target, so demoting them would leave
        // the application without a single administrator.
        User lastAdmin = user(5L, "Bertil Berg", Role.ADMIN);
        actingAs(actingAdmin);
        when(userRepository.findById(5L)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(lastAdmin));

        ConflictException failure = assertThrows(ConflictException.class,
                () -> userRoleService.changeRole(5L, Role.STUDENT, actingAdmin.getEmail()));

        assertEquals("The last administrator cannot be demoted.", failure.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void theLastAdministratorCheckReadsTheLockedRowsNotAPlainCount() {
        User other = user(5L, "Bertil Berg", Role.ADMIN);
        actingAs(actingAdmin);
        when(userRepository.findById(5L)).thenReturn(Optional.of(other));
        when(userRepository.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(actingAdmin, other));

        userRoleService.changeRole(5L, Role.STUDENT, actingAdmin.getEmail());

        verify(userRepository).findByRoleForUpdate(Role.ADMIN);
        verify(userRepository, never()).countByRole(any());
    }

    @Test
    void anAdministratorCannotDemoteTheirOwnAccount() {
        actingAs(actingAdmin);

        ConflictException failure = assertThrows(ConflictException.class,
                () -> userRoleService.changeRole(1L, Role.STUDENT, actingAdmin.getEmail()));

        assertEquals("You cannot change your own role.", failure.getMessage());
        verify(userRepository, never()).findByRoleForUpdate(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void demotingAStudentIsRejected() {
        actingAs(actingAdmin);
        when(userRepository.findById(7L)).thenReturn(Optional.of(student));
        when(userRepository.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(actingAdmin));

        ConflictException failure = assertThrows(ConflictException.class,
                () -> userRoleService.changeRole(7L, Role.STUDENT, actingAdmin.getEmail()));

        assertEquals("This user is already a student.", failure.getMessage());
    }

    @Test
    void promotingAnAdministratorIsRejected() {
        actingAs(actingAdmin);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(actingAdmin));

        ConflictException failure = assertThrows(ConflictException.class,
                () -> userRoleService.changeRole(1L, Role.ADMIN, actingAdmin.getEmail()));

        assertEquals("This user is already an administrator.", failure.getMessage());
    }

    @Test
    void unknownTargetGivesNotFound() {
        actingAs(actingAdmin);
        when(userRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userRoleService.changeRole(99L, Role.ADMIN, actingAdmin.getEmail()));
        assertThrows(NotFoundException.class,
                () -> userRoleService.changeRole(99L, Role.STUDENT, actingAdmin.getEmail()));
    }

    @Test
    void onlyAdministratorsCanChangeRoles() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));

        assertThrows(AccessDeniedException.class,
                () -> userRoleService.changeRole(7L, Role.ADMIN, student.getEmail()));

        verify(userRepository, never()).findByIdForUpdate(any());
        verify(userRepository, never()).findByRoleForUpdate(any());
    }

    @Test
    void unknownActingUserGivesNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userRoleService.changeRole(7L, Role.ADMIN, "ghost@example.com"));

        verify(userRepository, never()).findByIdForUpdate(any());
        verify(userRepository, never()).findByRoleForUpdate(any());
    }
}
