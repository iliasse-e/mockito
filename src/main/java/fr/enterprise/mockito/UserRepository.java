package fr.enterprise.mockito;

public interface UserRepository {
    String findNameById(Long id);
    int countUsers();
    void save(String name);
}
