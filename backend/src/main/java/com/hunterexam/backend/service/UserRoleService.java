package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Promotes students to administrators and demotes administrators to students.
 * <p>
 * Three rules are enforced here, on top of the {@code /api/admin/**} rule in
 * SecurityConfig that already keeps students out:
 * <ul>
 *   <li>an administrator cannot change their own role (no self-demotion);</li>
 *   <li>the last administrator cannot be demoted;</li>
 *   <li>only administrators can change roles at all.</li>
 * </ul>
 * <p>
 * The last rule is checked inside one transaction that first locks every
 * administrator row ({@link UserRepository#findByRoleForUpdate}), so two
 * concurrent demotions run one after the other and the second one sees the
 * count the first one left behind. A plain count-then-update would let both
 * requests pass the check with the same stale number.
 */
@Service
public class UserRoleService {

    private final UserRepository userRepository;

    public UserRoleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sets a user's role to the requested value.
     *
     * @param targetId         id of the user to change
     * @param requestedRole    ADMIN (promote) or STUDENT (demote)
     * @param actingAdminEmail email of the administrator making the change,
     *                         taken from the JWT, never from the request
     * @return the user's new name and role, as the learner list shows it
     * @throws NotFoundException    if no user has this id
     * @throws AccessDeniedException if the caller is not an administrator
     * @throws ConflictException    if the change is not allowed (own account,
     *                              last administrator, or the role already is
     *                              the requested one)
     */
    @Transactional
    public LearnerSummaryResponse changeRole(Long targetId, Role requestedRole, String actingAdminEmail) {

        User actingAdmin = userRepository.findByEmail(actingAdminEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Defense in depth: the security layer already only lets administrators
        // reach this endpoint, using this same database role.
        if (actingAdmin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can change roles.");
        }

        return requestedRole == Role.STUDENT
                ? demote(targetId, actingAdmin)
                : promote(targetId);
    }

    /** Student → administrator. Locks the target row so the change is atomic. */
    private LearnerSummaryResponse promote(Long targetId) {
        User target = userRepository.findByIdForUpdate(targetId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (target.getRole() == Role.ADMIN) {
            throw new ConflictException("This user is already an administrator.");
        }

        target.setRole(Role.ADMIN);
        userRepository.save(target);

        return toSummary(target);
    }

    /**
     * Administrator → student.
     * <p>
     * The target row is locked through the administrator list: demoting an
     * administrator only ever touches administrator rows, and holding all of
     * them until the transaction ends is what makes the last-administrator
     * check safe against concurrent requests.
     */
    private LearnerSummaryResponse demote(Long targetId, User actingAdmin) {

        if (targetId.equals(actingAdmin.getId())) {
            throw new ConflictException("You cannot change your own role.");
        }

        // Checked before locking, so an unknown id answers 404 instead of 409.
        userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<User> admins = userRepository.findByRoleForUpdate(Role.ADMIN);

        User target = admins.stream()
                .filter(admin -> admin.getId().equals(targetId))
                .findFirst()
                // Lost the race: someone else already demoted this user.
                .orElseThrow(() -> new ConflictException("This user is already a student."));

        if (admins.size() <= 1) {
            throw new ConflictException("The last administrator cannot be demoted.");
        }

        target.setRole(Role.STUDENT);
        userRepository.save(target);

        return toSummary(target);
    }

    private static LearnerSummaryResponse toSummary(User user) {
        return new LearnerSummaryResponse(user.getId(), user.getFullName(), user.getRole());
    }
}
