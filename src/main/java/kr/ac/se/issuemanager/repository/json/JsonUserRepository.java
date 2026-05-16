package kr.ac.se.issuemanager.repository.json;

import kr.ac.se.issuemanager.model.User;
import kr.ac.se.issuemanager.repository.UserRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JsonUserRepository implements UserRepository {
    private final JsonListStore<User> store;
    private final List<User> users;

    public JsonUserRepository(Path dataDirectory) {
        this.store = new JsonListStore<>(dataDirectory.resolve("users.json"), User.class);
        this.users = store.load();
    }

    @Override
    public List<User> findAll() {
        return users.stream()
                .sorted(Comparator.comparing(User::getUsername))
                .toList();
    }

    @Override
    public Optional<User> findById(String id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    @Override
    public User save(User user) {
        users.removeIf(existing -> existing.getId().equals(user.getId()));
        users.add(user);
        store.save(users);
        return user;
    }

    @Override
    public void saveAll(List<User> users) {
        this.users.clear();
        this.users.addAll(new ArrayList<>(users));
        store.save(this.users);
    }
}

