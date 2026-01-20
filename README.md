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

## ArgumentCaptor

C’est un objet fourni par Mockito qui permet de capturer les arguments réellement passés à une méthode d’un mock, afin de les inspecter dans ton test.

En clair :

  Quand ton code appelle une méthode d’un mock, l’ArgumentCaptor te permet de récupérer les valeurs exactes qui ont été envoyées.

C’est comme un “enregistreur” d’arguments.

### Pourquoi en a‑t‑on besoin ?

Parce que parfois, tu veux tester ce que ton code envoie à une dépendance.

Exemples typiques :

vérifier qu’un service envoie le bon DTO à un repository

vérifier qu’un mapper produit la bonne valeur

vérifier qu’un service nettoie ou transforme une donnée avant de l’envoyer

vérifier qu’un ID généré est bien passé à la méthode suivante

vérifier qu’un objet complexe contient les bonnes valeurs

*Dans ces cas-là, un simple verify(mock).save("Alice") ne suffit pas (surtout si l’argument est dynamique, construit dans la méthode, modifié avant l’appel)*

```java
ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

service.createUser("Charlie");

verify(mockRepo).save(captor.capture());

assertEquals("Charlie", captor.getValue());
```

Ici :

``capture()`` intercepte l’argument passé à ``save()``

``getValue()`` te donne la valeur capturée

L’ArgumentCaptor est donc un outil d’inspection, pas un outil de simulation.

### Quand utiliser ArgumentCaptor ?

Utilise-le quand tu veux vérifier :

un argument calculé dans la méthode testée

un argument transformé (trim, lowercase, mapping…)

un argument complexe (DTO, entity, map…)

un argument généré (UUID, timestamp…)

un argument intermédiaire dans un workflow

### Différence avec un spy

L’ArgumentCaptor sert à observer ce que ton code envoie à un mock.

Il ne modifie rien.
Il ne simule rien.
Il ne remplace rien.

Un spy sert à tester la vraie logique d’un objet, tout en permettant de surcharger certaines méthodes.

Un spy :

exécute les vraies méthodes par défaut

peut être partiellement mocké

permet de vérifier les appels internes

permet de contrôler une partie du comportement

## Mockito & Spring

Dans un projet Spring, tes classes sont souvent des beans gérés par le conteneur (services, repositories, controllers…).
Quand tu veux tester un bean, tu veux généralement :

isoler la classe testée

remplacer ses dépendances par des mocks

éviter de charger tout le contexte Spring (lent et inutile pour un test unitaire)

C’est exactement ce que Mockito permet de faire, et Spring fournit même des annotations dédiées pour faciliter l’intégration.

### Les 3 façons d’utiliser Mockito avec Spring

#### Tests unitaires purs (sans Spring)

C’est la méthode la plus rapide et la plus propre pour tester un service Spring.

Tu utilises :

``@ExtendWith(MockitoExtension.class)``

``@Mock``

``@InjectMocks``

👉 Aucun contexte Spring n’est chargé.
👉 Parfait pour tester la logique métier.

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    @Test
    void testGetUser() {
        when(repo.findNameById(1L)).thenReturn("Alice");

        String result = service.getUserName(1L);

        assertEquals("ALICE", result);
    }
}
```

#### Tests Spring Boot avec @MockBean

Quand tu veux tester un bean Spring dans un contexte Spring, mais en remplaçant certaines dépendances par des mocks, tu utilises :

``@SpringBootTest``

``@MockBean``

👉 Spring crée le contexte
👉 Mockito remplace certains beans par des mocks
👉 Tu testes un vrai bean Spring, mais isolé

```java
@SpringBootTest
class UserServiceSpringTest {

    @MockBean
    private UserRepository repo;

    @Autowired
    private UserService service;

    @Test
    void testGetUser() {
        when(repo.findNameById(1L)).thenReturn("Bob");

        String result = service.getUserName(1L);

        assertEquals("BOB", result);
    }
}
```

#### Tests de contrôleurs avec @WebMvcTest
Pour tester un controller Spring MVC sans charger tout le contexte, tu utilises :

``@WebMvcTest``

``@MockBean`` pour les services

👉 Très rapide
👉 Parfait pour tester les endpoints REST

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Test
    void testGetUser() throws Exception {
        when(service.getUserName(1L)).thenReturn("ALICE");

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("ALICE"));
    }
}
```
