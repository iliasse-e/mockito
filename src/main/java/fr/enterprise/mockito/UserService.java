package fr.enterprise.mockito;

public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public String getUserName(Long id) {
        String name = repository.findNameById(id);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid user name");
        }
        return name.toUpperCase();
    }

    public int getDoubleUserCount() {
        return repository.countUsers() * 2;
    }

    public void createUser(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        repository.save(name);
    }
}
