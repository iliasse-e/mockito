package fr.enterprise.mockito;

import java.util.HashMap;
import java.util.Map;

public class FakeUserRepository implements UserRepository {

    private final Map<Long, String> store = new HashMap<>();
    private long idCounter = 1;

    @Override
    public String findNameById(Long id) {
        return store.get(id);
    }

    @Override
    public int countUsers() {
        return store.size();
    }

    @Override
    public void save(String name) {
        store.put(idCounter++, name);
    }
}

