package com.autocare.vehicleservice.service;

import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.entity.User;
import com.autocare.vehicleservice.enums.Role;
import com.autocare.vehicleservice.form.RegisterForm;
import com.autocare.vehicleservice.form.StaffForm;
import com.autocare.vehicleservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    //Returns the matching user only if the password also matches unless empty
    public Optional<User> authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    //Register a new customer
    public User registerCustomer(RegisterForm form) {
        if (emailExists(form.getEmail())) {
            throw new IllegalStateException("An account with that email already exists.");
        }
        User user = new User();
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(form.getPhone());
        user.setRole(Role.CUSTOMER);
        return userRepository.save(user);
    }

    //Creates a staff member for the given branch
    public User createStaff(StaffForm form, Branch branch) {
        Role role = form.getRole();
        if (role != Role.CASHIER && role != Role.MECHANIC && role != Role.INVENTORY_SERVICE_MANAGER) {
            throw new IllegalArgumentException("Staff role must be Cashier, Mechanic, or Inventory & Service Manager.");
        }
        if (emailExists(form.getEmail())) {
            throw new IllegalStateException("An account with that email already exists.");
        }
        User user = new User();
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(form.getPhone());
        user.setRole(role);
        user.setBranch(branch);
        return userRepository.save(user);
    }

    public List<User> findMechanicsByBranch(Long branchId) {
        return userRepository.findByBranch_IdAndRole(branchId, Role.MECHANIC);
    }

    // List All staff at a branch excluding the branch manager.
    public List<User> findStaffByBranch(Long branchId) {
        return userRepository.findByBranch_IdAndRoleNot(branchId, Role.BRANCH_MANAGER);
    }

    //Updates an existing staff member's name, phone, mail and role.
    public User updateStaff(Long staffId, StaffForm form, Long branchId) {
        User user = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found."));

        // Guard: can only edit staff that belong to this manager's branch
        if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("You can only edit staff at your own branch.");
        }
        // Guard: cannot touch the branch manager record
        if (user.getRole() == Role.BRANCH_MANAGER) {
            throw new IllegalArgumentException("Cannot edit the branch manager account here.");
        }
        // Guard: new role must be a valid staff role
        Role role = form.getRole();
        if (role != Role.CASHIER && role != Role.MECHANIC && role != Role.INVENTORY_SERVICE_MANAGER) {
            throw new IllegalArgumentException("Staff role must be Cashier, Mechanic, or Inventory & Service Manager.");
        }
        // Guard: if the email changed, make sure no one else already has it
        if (!user.getEmail().equalsIgnoreCase(form.getEmail())
                && userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalStateException("An account with that email already exists.");
        }

        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setRole(role);

        // Only re-hash if the manager actually typed a new password
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        return userRepository.save(user);
    }

    //Deletes a staff member
    public void deleteStaff(Long staffId, Long branchId) {
        User user = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found."));

        if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
            throw new IllegalArgumentException("You can only remove staff at your own branch.");
        }
        if (user.getRole() == Role.BRANCH_MANAGER) {
            throw new IllegalArgumentException("Cannot delete the branch manager account.");
        }

        userRepository.delete(user);
    }
}
