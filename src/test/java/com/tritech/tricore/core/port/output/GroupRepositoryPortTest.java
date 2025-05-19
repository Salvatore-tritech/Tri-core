package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.Group;
import com.tritech.tricore.shared.config.TestJpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class GroupRepositoryPortTest {

	@Autowired
	private GroupRepositoryPort groupRepositoryPort;

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
	void cleanDatabase() {
		groupRepositoryPort.deleteAll();
	}

	@Nested
	class IntegrityConstraintTest {

		@Test
		void testGroupNameCannotBeNull() {
			Group group = Group.builder().build(); // manca groupName

			Exception exception = assertThrows(JpaSystemException.class, () -> {
				groupRepositoryPort.saveAndFlush(Group.builder().build());
			});
		}

		@Test
		void testGroupNameUniqueness() {
			Group g1 = Group.builder().groupName("admin").build();
			Group g2 = Group.builder().groupName("admin").build(); // stesso
			// nome

			groupRepositoryPort.save(g1);
			assertThrows(DataIntegrityViolationException.class, () -> {
				groupRepositoryPort.saveAndFlush(g2);
			});
		}

	}

	@Nested
	class CrudTest {

		@Test
		void testSaveAndRetrieveGroup() {
			// Preparazione: creo un oggetto Group
			Group group = Group.builder().groupName("admin").build();

			// Azione: salvo il gruppo nel database
			Group savedGroup = groupRepositoryPort.save(group);

			// Verifica: controllo che il gruppo sia stato salvato correttamente
			assertNotNull(savedGroup);
			assertEquals("admin", savedGroup.getGroupName());

			// Azione: recupero il gruppo dal database
			Optional<Group> retrievedGroupOpt = groupRepositoryPort.findById("admin");

			// Verifica: controllo che il gruppo sia stato recuperato
			// correttamente
			assertTrue(retrievedGroupOpt.isPresent());
			Group retrievedGroup = retrievedGroupOpt.get();
			assertEquals("admin", retrievedGroup.getGroupName());
			assertNotNull(retrievedGroup.getCreatedAt());
		}

		@Test
		void testFindAllGroups() {
			// Preparazione: creo e salvo più gruppi

			Group group1 = Group.builder().groupName("admin").build();
			Group group2 = Group.builder().groupName("user").build();
			Group group3 = Group.builder().groupName("guest").build();

			groupRepositoryPort.saveAndFlush(group1);
			groupRepositoryPort.saveAndFlush(group2);
			groupRepositoryPort.saveAndFlush(group3);

			// Azione: recupero tutti i gruppi
			List<Group> allGroups = groupRepositoryPort.findAll();

			// Verifica: controllo che tutti i gruppi siano stati recuperati
			assertEquals(3, allGroups.size());
			assertTrue(allGroups.stream().anyMatch(g -> g.getGroupName().equals("admin")));
			assertTrue(allGroups.stream().anyMatch(g -> g.getGroupName().equals("user")));
			assertTrue(allGroups.stream().anyMatch(g -> g.getGroupName().equals("guest")));
		}

		@Test
		void testUpdateGroup() {
			// Preparazione: creo e salvo un gruppo
			Group group = Group.builder().groupName("moderator").build();

			groupRepositoryPort.save(group);

			// Recupero il gruppo e aggiorno l'updatedAt
			Optional<Group> savedGroupOpt = groupRepositoryPort.findById("moderator");
			assertTrue(savedGroupOpt.isPresent());

			Group savedGroup = savedGroupOpt.get();
			LocalDateTime updatedTime = LocalDateTime.now().plusHours(1);
			savedGroup.setUpdatedAt(updatedTime);

			// Azione: salvo le modifiche
			Group updatedGroup = groupRepositoryPort.save(savedGroup);

			// Verifica: controllo che l'aggiornamento sia avvenuto
			// correttamente
			assertEquals(updatedTime, updatedGroup.getUpdatedAt());

			// Recupero nuovamente per verificare la persistenza
			Optional<Group> retrievedUpdatedOpt = groupRepositoryPort.findById("moderator");
			assertTrue(retrievedUpdatedOpt.isPresent());
			assertEquals(updatedTime, retrievedUpdatedOpt.get().getUpdatedAt());
		}

		@Test
		void testDeleteGroup() {
			// Preparazione: creo e salvo un gruppo
			Group group = Group.builder().groupName("temporary").build();

			groupRepositoryPort.save(group);

			// Verifica che il gruppo esista
			assertTrue(groupRepositoryPort.existsById("temporary"));

			// Azione: elimino il gruppo
			groupRepositoryPort.delete(group);

			// Verifica: controllo che il gruppo sia stato eliminato
			assertFalse(groupRepositoryPort.existsById("temporary"));
		}

		@Test
		void testGroupCount() {
			// Preparazione: creo e salvo più gruppi
			LocalDateTime now = LocalDateTime.now();

			Group group1 = Group.builder().groupName("role1").build();
			Group group2 = Group.builder().groupName("role2").build();

			groupRepositoryPort.saveAndFlush(group1);
			groupRepositoryPort.saveAndFlush(group2);

			// Azione: conto i gruppi
			long count = groupRepositoryPort.count();

			// Verifica: controllo che il conteggio sia corretto
			assertEquals(2, count);

			// Aggiungo un altro gruppo
			Group group3 = Group.builder().groupName("role3").build();
			group3.setCreatedAt(now);
			group3.setUpdatedAt(now);
			groupRepositoryPort.saveAndFlush(group3);

			// Verifico che il conteggio sia aggiornato
			assertEquals(3, groupRepositoryPort.count());
		}

	}

	@Nested
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	class ConcurrencyTest {

		@Test
		void testOptimisticLocking() {
			Group group = Group.builder().groupName("marketing").build();
			groupRepositoryPort.saveAndFlush(group); // flush per avere version iniziale

			Group g1 = entityManager.find(Group.class, "marketing");
			entityManager.detach(g1);

			Group g2 = entityManager.find(Group.class, "marketing");
			entityManager.detach(g2);

			g1.setUpdatedAt(LocalDateTime.now());
			groupRepositoryPort.saveAndFlush(g1); // OK, version +1

			g2.setUpdatedAt(LocalDateTime.now());
			assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
				groupRepositoryPort.saveAndFlush(g2); // fallisce: version non aggiornata
			});
		}

	}

	@Nested
	class AuditFieldsTest {

		@Test
		void testCreatedAtAndUpdatedAtAreSet() {
			Group group = Group.builder().groupName("Admins").build();

			Group saved = groupRepositoryPort.save(group);
			assertNotNull(saved.getCreatedAt());
			assertNotNull(saved.getUpdatedAt());
		}

	}

}
