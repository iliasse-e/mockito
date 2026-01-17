# Mockito

Mockito est un framework Java qui permet de créer des objets factices (mocks) afin de tester une classe sans exécuter réellement ses dépendances.

L’idée est simple :

- On remplace une dépendance réelle (service, repository, client HTTP…) par un mock.

- On définit le comportement attendu du mock.

- On teste uniquement la logique de la classe.

C’est parfait pour des tests unitaires propres, rapides et déterministes.


## Pourquoi utiliser Mockito ?

*Isolation totale* : on teste une classe sans toucher à la base de données, au réseau, ou à d’autres services.

*Contrôle du comportement* : on simule des retours, des exceptions, des délais, etc.

*Vérification des interactions* : on peut vérifier qu’une méthode a été appelée, combien de fois, avec quels arguments.

*Lisibilité* : la syntaxe est fluide et expressive.


## Concepts essentiels

1. Mock

Simuler un objet

```java
MyService service = Mockito.mock(MyService.class);
```

2. Stubbing

Définit ce que doit renvoyer le mock

```java
when(service.getData()).thenReturn("hello");
```

3. Verify

Vérifie qu'une méthode a été appellée

```java
verify(service).getData();
```

4. Spy (et la différence avec un stub)

Un spy est utile dans des cas très spécifiques.
Il ne remplace pas un mock, et il ne sert pas à tester une classe simple.

#### Exemple concret où un spy est indispensable
Imaginons une classe qui :

- valide une entrée

- nettoie la donnée

- appelle une méthode interne pour la sauvegarde

```java
public class UserProcessor {

    public String process(String input) {
        validate(input);
        String cleaned = clean(input);
        return save(cleaned);
    }

    protected void validate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Invalid input");
        }
    }

    protected String clean(String input) {
        return input.trim().toLowerCase();
    }

    protected String save(String cleaned) {
        // Imagine un appel à une base ou API
        return "saved:" + cleaned;
    }
}

```

#### Test avec spy (et pourquoi un stub ne peut pas faire ça)
```java
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserProcessorTest {

    @Test
    void testProcessWithSpy() {
        UserProcessor spyProcessor = spy(new UserProcessor());

        // On veut tester la logique réelle, mais éviter l'appel réel à save()
        doReturn("saved:mocked").when(spyProcessor).save(anyString());

        String result = spyProcessor.process("  Alice  ");

        assertEquals("saved:mocked", result);

        // Vérifier que les méthodes internes ont été appelées
        verify(spyProcessor).validate("  Alice  ");
        verify(spyProcessor).clean("  Alice  ");
        verify(spyProcessor).save("alice");
    }
}
```

#### Pourquoi un stub ne peut pas faire ça ?
Si tu stubbes ``UserProcessor``, tu obtiens un mock vide :

``validate()`` ne fait rien

``clean()`` ne fait rien

``save()`` ne fait rien

``process()`` ne fait rien

Donc :

tu ne peux pas tester la logique réelle

tu ne peux pas vérifier les appels internes

tu ne peux pas contrôler seulement une partie du comportement

**Le stub détruit toute la logique.**

**Le spy la conserve.**

## @Mock et @InjectMocks

Avec JUnit 5 + Mockito Extension :

```java
@ExtendWith(MockitoExtension.class)
class MyTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    void test() {
        when(repository.findName()).thenReturn("Alice");

        String result = service.getName();

        assertEquals("Alice", result);
        verify(repository).findName();
    }
}
```