package com.zorvyn.finance.app.service.impl;

import com.zorvyn.finance.app.dtos.request.UserRegisterRequestDto;
import com.zorvyn.finance.app.dtos.response.PageResponse;
import com.zorvyn.finance.app.dtos.response.UserRegisterResponse;
import com.zorvyn.finance.app.entity.User;
import com.zorvyn.finance.app.entity.enums.Role;
import com.zorvyn.finance.app.entity.enums.Status;
import com.zorvyn.finance.app.exception.ResourceNotFoundException;
import com.zorvyn.finance.app.exception.UserAlreadyExistsException;
import com.zorvyn.finance.app.repository.UserRepository;
import com.zorvyn.finance.app.service.UserService;
import com.zorvyn.finance.app.specification.UserSpecifications;
import com.zorvyn.finance.app.utils.IdentifierUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public UserRegisterResponse registerUser(UserRegisterRequestDto request) {

        log.info("Registering new user | email={}", request.email());

        if(userRepository.existsByEmail(request.email())){
            log.warn("Registration failed — email already in use | email={}", request.email());
            throw new UserAlreadyExistsException("An active account with this email already exists.");
        }

        if(userRepository.existsByEmailAndDeletedTrue(request.email())){
            log.warn("Registration failed — email belongs to a deleted account | email={}", request.email());
            throw new UserAlreadyExistsException("This email was previously registered. Please contact support for reactivation.");
        }

        User user = User.builder()
                .userName(request.userName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.VIEWER)
                .status(Status.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully | userId={} email={} role={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return modelMapper.map(savedUser, UserRegisterResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserRegisterResponse> getAllUsers(String email, Role role, Status status, Pageable pageable) {

        log.info("Fetching all users | email={} role={} status={} page={} size={}",
                email, role, status, pageable.getPageNumber(), pageable.getPageSize());

        Specification<User> spec = Specification.where(UserSpecifications.hasEmail(email))
                .and(UserSpecifications.hasRole(role))
                .and(UserSpecifications.hasStatus(status));

        Page<User> userPage = userRepository.findAll(spec, pageable);

        log.info("Users fetch complete | totalElements={} totalPages={}",
                userPage.getTotalElements(), userPage.getTotalPages());

        return PageResponse.of(
                userPage.map(map -> modelMapper.map(
                        map,UserRegisterResponse.class
                ))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserRegisterResponse getUserById(String id) {
        log.info("Fetching user by id | userId={}", id);
        User user = userRepository.findById(IdentifierUtils.parseUuid(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserRegisterResponse.class);
    }

    @Override
    @Transactional
    public UserRegisterResponse updateUserRole(String id, Role newRole) {

        log.info("Updating user role | userId={} newRole={}", id, newRole);

        User user = userRepository.findById(IdentifierUtils.parseUuid(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id:" + id));

        Role previousRole = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);

        log.info("User role updated successfully | userId={} previousRole={} newRole={}",
                id, previousRole, newRole);

        return modelMapper.map(saved, UserRegisterResponse.class);
    }

    @Override
    @Transactional
    public UserRegisterResponse updateUserStatus(String id, Status newStatus) {
        log.info("Updating user status | userId={} newStatus={}", id, newStatus);
        User user = userRepository.findById(IdentifierUtils.parseUuid(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Status previousStatus = user.getStatus();
        user.setStatus(newStatus);
        User saved = userRepository.save(user);

        log.info("User status updated successfully | userId={} previousStatus={} newStatus={}",
                id, previousStatus, newStatus);

        return modelMapper.map(saved, UserRegisterResponse.class);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {

        log.info("Deleting user | userId={}", id);

        UUID userId = IdentifierUtils.parseUuid(id);

        if(!userRepository.existsById(userId)){
            throw  new ResourceNotFoundException("Cannot delete: User not found with this id: " + id);
        }

        userRepository.deleteById(IdentifierUtils.parseUuid(id));
        log.info("User soft-deleted successfully | userId={}", id);
    }

}
