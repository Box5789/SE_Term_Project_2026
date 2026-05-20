package kr.ac.se.issuemanager.service;

import kr.ac.se.issuemanager.model.Role;
import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.repository.UserRepository;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, Role role) {
        requireText(username, "사용자 이름");
        if (role == null) {
            throw new ServiceException("역할은 필수입니다.");
        }
        userRepository.findByUsername(username).ifPresent(user -> {
            throw new ServiceException("이미 존재하는 사용자입니다: " + username);
        });
        User user = new User(IdGenerator.nextId("USER", userRepository.findAll(), User::getId), username, role);
        return userRepository.save(user);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("사용자를 찾을 수 없습니다: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("사용자를 찾을 수 없습니다: " + username));
    }

    static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ServiceException(fieldName + "은(는) 필수입니다.");
        }
    }
}

