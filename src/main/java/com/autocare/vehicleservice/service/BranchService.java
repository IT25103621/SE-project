package com.autocare.vehicleservice.service;


import com.autocare.vehicleservice.entity.Branch;
import com.autocare.vehicleservice.entity.User;
import com.autocare.vehicleservice.enums.BranchStatus;
import com.autocare.vehicleservice.enums.Role;
import com.autocare.vehicleservice.form.BranchRegisterForm;
import com.autocare.vehicleservice.repository.BranchRepository;
import com.autocare.vehicleservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BranchService(BranchRepository branchRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Branch registerBranch(BranchRegisterForm form) {
        if (userRepository.existsByEmail(form.getManagerEmail())) {
            throw new IllegalStateException("An account with that email already exists.");
        }

        User manager = new User();
        manager.setName(form.getManagerName());
        manager.setEmail(form.getManagerEmail());
        manager.setPassword(passwordEncoder.encode(form.getManagerPassword()));
        manager.setPhone(form.getManagerPhone());
        manager.setRole(Role.BRANCH_MANAGER);
        manager = userRepository.save(manager);

        Branch branch = new Branch();
        branch.setName(form.getBranchName());
        branch.setCity(form.getCity());
        branch.setAddress(form.getAddress());
        branch.setStatus(BranchStatus.PENDING);
        branch.setManager(manager);
        branch = branchRepository.save(branch);

        manager.setBranch(branch);
        userRepository.save(manager);

        return branch;
    }


    public List<Branch> listPendingBranches() {
        return branchRepository.findByStatus(BranchStatus.PENDING);
    }
    public List<Branch> listApprovedBranches() {
        return branchRepository.findByStatus(BranchStatus.APPROVED);
    }


    public List<Branch> listAll() {
        return branchRepository.findAll();
    }

    


}

