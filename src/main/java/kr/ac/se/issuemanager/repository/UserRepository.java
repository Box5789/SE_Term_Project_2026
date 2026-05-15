package kr.ac.se.issuemanager.repository;

import kr.ac.se.issuemanager.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(String id);

    Optional<User> findByUsername(String username);

    User save(User user);

    void saveAll(List<User> users);
}

