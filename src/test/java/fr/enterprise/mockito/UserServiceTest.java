package fr.enterprise.mockito;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository mockRepo;

    @InjectMocks
    private UserService service;

    @Test
    void testMockAndStub() {
        // Stub le repository du service
        when(mockRepo.findNameById(1L)).thenReturn("alice"); 

        String result = service.getUserName(1L);

        // Assert
        assertEquals("ALICE", result);

        // Vérifie que la méthode a été appellée
        verify(mockRepo).findNameById(1L);
    }

    @Test
    void testExceptionThrown() {
        // Stub
        when(mockRepo.findNameById(2L)).thenReturn("  ");

        // Act
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getUserName(2L)
        );

        assertEquals("Invalid user name", ex.getMessage());
    }

    @Test
    void testUserCreationError() {

      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        service.createUser(" ");
      });

      assert ex.getMessage().equals("Name cannot be empty");
    }

    @Test
    void testSpy() {
        // C'est un test qui n'a rien à voir avec le fichier UserService
        List<String> realList = new ArrayList<>();
        List<String> spyList = spy(realList);

        spyList.add("A"); // vraie méthode
        when(spyList.size()).thenReturn(10); // méthode mockée

        assertEquals(10, spyList.size());
        verify(spyList).add("A");
    }

    @Test
    void testFakeRepository() {
        FakeUserRepository fake = new FakeUserRepository();
        UserService localService = new UserService(fake);

        localService.createUser("Bob");
        localService.createUser("Alice");

        assertEquals(2, fake.countUsers());
        assertEquals("Bob", fake.findNameById(1L));
    }

    @Test
    void testArgumentCaptor() {
        // On veut tester que l'argument qu'on donne à la méthode .createUser() est aussi donnée à la méthode .save()
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        service.createUser("Charlie");

        verify(mockRepo).save(captor.capture());
        assertEquals("Charlie", captor.getValue());
    }

    @Test
    void testDoubleUserCount() {
        when(mockRepo.countUsers()).thenReturn(5);

        int result = service.getDoubleUserCount();

        assertEquals(10, result);
        verify(mockRepo).countUsers();
    }
}

