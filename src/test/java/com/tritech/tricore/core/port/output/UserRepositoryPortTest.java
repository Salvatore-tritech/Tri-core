package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.User;
import com.tritech.tricore.shared.config.ValidationTestConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({com.tritech.tricore.shared.config.TestJpaConfig.class,
        ValidationTestConfig.class})
class UserRepositoryPortTest {

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private Validator validator;

    @Nested
    class IntegrityConstraintTest {

        @Test
        void testFullnameNotNullViolation() {
            User user = User.builder()
                    .subject(1L)
                    .email("user@example.com")
                    .build(); // manca fullname

            assertThrows(DataIntegrityViolationException.class, () -> {
                userRepositoryPort.saveAndFlush(user);
            });
        }

        @Test
        void testEmailNotNullViolation() {
            User user = User.builder()
                    .subject(2L)
                    .fullname("Mario Rossi")
                    .build(); // manca email

            assertThrows(DataIntegrityViolationException.class, () -> {
                userRepositoryPort.saveAndFlush(user);
            });
        }

        @Test
        void testSubjectUniquenessViolation() {
            User user1 = User.builder()
                    .subject(3L)
                    .fullname("Mario")
                    .email("mario@example.com")
                    .build();

            User user2 = User.builder()
                    .subject(3L) // stesso subject
                    .fullname("Luigi")
                    .email("luigi@example.com")
                    .build();

            userRepositoryPort.save(user1);
            assertThrows(DataIntegrityViolationException.class, () -> {
                userRepositoryPort.saveAndFlush(user2);
            });
        }
    }

    @Nested
    class CrudTest {
        @Test
        void testSaveAndRetrieveUser() {
            // Preparazione: creo un oggetto User
            User user = User.builder()
                    .subject(123456789L)
                    .fullname("Mario Rossi")
                    .email("mario.rossi@example.com")
                    .picture("https://example.com/profile.jpg")
                    .build();

            // Azione: salvo l'utente nel database
            User savedUser = userRepositoryPort.save(user);

            // Verifica: controllo che l'utente sia stato salvato correttamente
            assertNotNull(savedUser);
            assertEquals(123456789L, savedUser.getSubject());

            // Azione: recupero l'utente dal database
            Optional<User> retrievedUserOpt =
                    userRepositoryPort.findById(123456789L);

            // Verifica: controllo che l'utente sia stato recuperato
            // correttamente
            assertTrue(retrievedUserOpt.isPresent());
            User retrievedUser = retrievedUserOpt.get();
            assertEquals("Mario Rossi", retrievedUser.getFullname());
            assertEquals("mario.rossi@example.com", retrievedUser.getEmail());
            assertEquals("https://example.com/profile.jpg",
                    retrievedUser.getPicture());
        }

        @Test
        void testUpdateUser() {
            // Preparazione: creo e salvo un utente
            User user = User.builder()
                    .subject(987654321L)
                    .fullname("Giuseppe Verdi")
                    .email("giuseppe.verdi@example.com")
                    .picture("https://example.com/verdi.jpg")
                    .build();

            userRepositoryPort.save(user);

            // Azione: modifico e aggiorno l'utente
            user.setFullname("Giuseppe Verdi Modificato");
            user.setEmail("nuovo.email@example.com");
            User updatedUser = userRepositoryPort.save(user);

            // Verifica: controllo che l'aggiornamento sia avvenuto
            // correttamente
            assertEquals("Giuseppe Verdi Modificato",
                    updatedUser.getFullname());
            assertEquals("nuovo.email@example.com", updatedUser.getEmail());

            // Doppia verifica: recupero nuovamente l'utente dal database
            Optional<User> retrievedUserOpt =
                    userRepositoryPort.findById(987654321L);
            assertTrue(retrievedUserOpt.isPresent());
            assertEquals("Giuseppe Verdi Modificato",
                    retrievedUserOpt.get().getFullname());
        }

        @Test
        void testDeleteUser() {
            // Preparazione: creo e salvo un utente
            User user = User.builder()
                    .subject(1122334455L)
                    .fullname("Anna Bianchi")
                    .email("anna.bianchi@example.com")
                    .picture("https://example.com/anna.jpg")
                    .build();
            userRepositoryPort.save(user);

            // Verifica che l'utente esista
            assertTrue(userRepositoryPort.existsById(1122334455L));

            // Azione: elimino l'utente
            userRepositoryPort.delete(user);

            // Verifica: controllo che l'utente sia stato eliminato
            assertFalse(userRepositoryPort.existsById(1122334455L));
        }

        @Test
        void testFindAllUsers() {
            // Preparazione: eliminazione di eventuali utenti esistenti
            userRepositoryPort.deleteAll();
            entityManager.flush();

            // Creo e salvo più utenti
            User user1 = User.builder()
                    .subject(11111L)
                    .fullname("Utente Uno")
                    .email("utente1@example.com")
                    .picture("https://example.com/pictures/utente1.jpg")
                    .build();

            User user2 = User.builder()
                    .subject(22222L)
                    .fullname("Utente Due")
                    .email("utente2@example.com")
                    .picture("https://example.com/pictures/utente2.jpg")
                    .build();

            User user3 = User.builder()
                    .subject(33333L)
                    .fullname("Utente Tre")
                    .email("utente3@example.com")
                    .picture("https://example.com/pictures/utente3.jpg")
                    .build();

            userRepositoryPort.saveAll(List.of(user1, user2, user3));
            entityManager.flush();
            entityManager.clear();

            // Azione: recupero tutti gli utenti
            List<User> allUsers = userRepositoryPort.findAll();

            // Verifica: controllo che siano stati recuperati tutti e tre gli
            // utenti
            assertEquals(3, allUsers.size());
            assertTrue(allUsers.stream().anyMatch(u -> u.getSubject().equals(11111L)));
            assertTrue(allUsers.stream().anyMatch(u -> u.getSubject().equals(22222L)));
            assertTrue(allUsers.stream().anyMatch(u -> u.getSubject().equals(33333L)));
        }
    }

    @Nested
    class ConcurrencyTest {
        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void testOptimisticLocking() {
            // Step 1: salvataggio iniziale
            User user = User.builder()
                    .subject(123L)
                    .fullname("Alice")
                    .email("alice@example.com")
                    .build();
            userRepositoryPort.save(user);

            // Step 2: carica l'entità in due contesti distinti
            User user1 = entityManager.find(User.class, 123L);
            entityManager.detach(user1); // Simula due EntityManager
            User user2 = entityManager.find(User.class, 123L);
            entityManager.detach(user2);

            // Step 3: Primo aggiornamento
            user1.setFullname("Alice Updated");
            userRepositoryPort.save(user1); // OK: version = 0 → 1

            // Step 4: Secondo aggiornamento → dovrebbe fallire
            user2.setFullname("Alice Conflict");
            Assertions.assertThrows(
                    ObjectOptimisticLockingFailureException.class,
                    () -> userRepositoryPort.saveAndFlush(user2)
            );
        }
    }

    @Nested
    class AuditFieldsTest {
        @Test
        void testCreatedAtAndUpdatedAtAreSet() {
            User user = User.builder()
                    .subject(20L)
                    .fullname("Luca")
                    .email("luca@example.com")
                    .build();

            User saved = userRepositoryPort.save(user);
            assertNotNull(saved.getCreatedAt());
            assertNotNull(saved.getUpdatedAt());
        }

        @Test
        void testUpdatedAtChangesOnUpdate() throws InterruptedException {
            User user = User.builder()
                    .subject(21L)
                    .fullname("Laura")
                    .email("laura@example.com")
                    .build();

            user = userRepositoryPort.saveAndFlush(user);
            LocalDateTime firstUpdate = user.getUpdatedAt();

            Thread.sleep(1000); // garantisce una differenza temporale
            user.setFullname("Laura Aggiornata");
            user = userRepositoryPort.saveAndFlush(user);

            assertTrue(user.getUpdatedAt().isAfter(firstUpdate));
        }
    }

    @Nested
    class BeanValidationTest {
        @Test
        void testInvalidEmailFormatFailsValidation() {
            User user = User.builder()
                    .subject(30L)
                    .fullname("Marco")
                    .email("non-valida") // no @
                    .build();

            Set<ConstraintViolation<User>> violations =
                    validator.validate(user);
            assertFalse(violations.isEmpty());
        }
    }
}
