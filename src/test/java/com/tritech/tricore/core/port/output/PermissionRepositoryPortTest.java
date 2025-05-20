package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.Group;
import com.tritech.tricore.core.domain.GroupLevel;
import com.tritech.tricore.core.domain.Permission;
import com.tritech.tricore.core.domain.User;
import com.tritech.tricore.core.domain.primarykeys.PermissionId;
import com.tritech.tricore.shared.config.TestJpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class PermissionRepositoryPortTest {

	@Autowired
	private PermissionRepositoryPort permissionRepositoryPort;

	@Autowired
	private GroupLevelRepositoryPort groupLevelRepositoryPort;

	@Autowired
	private UserRepositoryPort userRepositoryPort;

	@Autowired
	private GroupRepositoryPort groupRepositoryPort;

	@PersistenceContext
	private EntityManager entityManager;

	@Nested
	class CrudTest {

		@Test
		void testSaveAndRetrievePermission() {
			// Preparazione del gruppo, livello e dell'utente associato
			Group group = Group.builder().groupName("admin").build();
			groupRepositoryPort.save(group);

			GroupLevel groupLevel = GroupLevel.builder().groupName("admin").levelName("superadmin").build();
			groupLevelRepositoryPort.save(groupLevel);

			User user = User.builder().subject(123456L).build();
			userRepositoryPort.save(user);

			// Creo e salvo un Permission
			Permission permission = Permission.builder()
				.subject(123456L)
				.groupName("admin")
				.levelName("superadmin")
				.build();
			permissionRepositoryPort.save(permission);

			// Verifico il salvataggio
			PermissionId id = new PermissionId(123456L, "admin", "superadmin");
			Permission fetchedPermission = permissionRepositoryPort.findById(id).orElseThrow();

			assertNotNull(fetchedPermission);
			assertEquals(123456L, fetchedPermission.getSubject());
			assertEquals("admin", fetchedPermission.getGroupName());
			assertEquals("superadmin", fetchedPermission.getLevelName());
		}

		@Test
		void testFindAllPermissions() {
			// Preparazione del gruppo, livello e dell'utente
			Group group = Group.builder().groupName("teamA").build();
			groupRepositoryPort.save(group);

			GroupLevel level1 = GroupLevel.builder().groupName("teamA").levelName("level1").build();
			GroupLevel level2 = GroupLevel.builder().groupName("teamA").levelName("level2").build();
			groupLevelRepositoryPort.save(level1);
			groupLevelRepositoryPort.save(level2);

			User user = User.builder().subject(123456L).fullname("user").email("user@gmail" + ".com").build();
			userRepositoryPort.save(user);

			// Creazione delle Permissions
			permissionRepositoryPort
				.save(Permission.builder().subject(123456L).groupName("teamA").levelName("level1").build());
			permissionRepositoryPort
				.save(Permission.builder().subject(123456L).groupName("teamA").levelName("level2").build());

			// Recupero tutte le Permissions
			List<Permission> allPermissions = permissionRepositoryPort.findAll();

			// Verifica
			assertEquals(2, allPermissions.size());
			assertTrue(allPermissions.stream().anyMatch(p -> p.getLevelName().equals("level1")));
			assertTrue(allPermissions.stream().anyMatch(p -> p.getLevelName().equals("level2")));
		}

		@Test
		void testDeletePermission() {
			// Preparazione
			Group group = Group.builder().groupName("teamB").build();
			groupRepositoryPort.save(group);

			GroupLevel level = GroupLevel.builder().groupName("teamB").levelName("admin").build();
			groupLevelRepositoryPort.save(level);

			User user = User.builder().subject(123456L).build();
			userRepositoryPort.save(user);

			Permission permission = Permission.builder().subject(123456L).groupName("teamB").levelName("admin").build();
			permissionRepositoryPort.save(permission);

			// Verifica presenza Permission
			PermissionId id = new PermissionId(123456L, "teamB", "admin");
			assertTrue(permissionRepositoryPort.findById(id).isPresent());

			// Elimino
			permissionRepositoryPort.delete(permission);

			// Verifico rimozione
			assertFalse(permissionRepositoryPort.findById(id).isPresent());
		}

	}

	@Nested
	class ReferentialIntegrityTest {

		@Test
		void testDeleteGroupLevelCascadesToPermission() {
			// Preparazione
			Group group = Group.builder().groupName("groupA").build();
			groupRepositoryPort.save(group);

			GroupLevel groupLevel = GroupLevel.builder().groupName("groupA").levelName("level1").build();
			groupLevelRepositoryPort.save(groupLevel);

			User user = User.builder().subject(123456L).fullname("user").email("user@gmail" + ".com").build();
			userRepositoryPort.save(user);

			permissionRepositoryPort
				.save(Permission.builder().subject(123456L).groupName("groupA").levelName("level1").build());

			// Verifico che ci sia la Permission
			assertEquals(1, permissionRepositoryPort.findAll().size());

			// Elimino il GroupLevel
			groupLevelRepositoryPort.delete(groupLevel);

			entityManager.flush();
			entityManager.clear();

			// Verifico che la Permission sia stata eliminata
			assertEquals(0, permissionRepositoryPort.findAll().size());
		}

		@Test
		void testDeleteUserCascadesToPermission() {
			// Preparazione
			Group group = Group.builder().groupName("groupA").build();
			groupRepositoryPort.save(group);

			GroupLevel groupLevel = GroupLevel.builder().groupName("groupA").levelName("level1").build();
			groupLevelRepositoryPort.save(groupLevel);

			User user = User.builder().subject(123456L).fullname("user").email("user@gmail" + ".com").build();
			userRepositoryPort.save(user);

			permissionRepositoryPort
				.save(Permission.builder().subject(123456L).groupName("groupA").levelName("level1").build());

			// Verifico che ci sia la Permission
			assertEquals(1, permissionRepositoryPort.findAll().size());

			// Elimino il GroupLevel
			userRepositoryPort.delete(user);

			entityManager.flush();
			entityManager.clear();

			// Verifico che la Permission sia stata eliminata
			assertEquals(0, permissionRepositoryPort.findAll().size());
		}

	}

	@Nested
	class ConcurrencyTest {

		@Test
		void testOptimisticLocking() {
			// Preparazione
			Group group = Group.builder().groupName("groupX").build();
			groupRepositoryPort.save(group);

			GroupLevel groupLevel = GroupLevel.builder().groupName("groupX").levelName("admin").build();
			groupLevelRepositoryPort.save(groupLevel);

			User user = User.builder().subject(123456L).fullname("user").email("user@gmail" + ".com").build();
			userRepositoryPort.save(user);

			Permission permission = Permission.builder()
				.subject(123456L)
				.groupName("groupX")
				.levelName("admin")
				.build();
			permissionRepositoryPort.saveAndFlush(permission);

			// Recupero due versioni
			Permission p1 = entityManager.find(Permission.class, new PermissionId(123456L, "groupX", "admin"));
			entityManager.detach(p1);

			Permission p2 = entityManager.find(Permission.class, new PermissionId(123456L, "groupX", "admin"));
			entityManager.detach(p2);

			// Primo aggiornamento
			p1.setUpdatedAt(LocalDateTime.now());
			permissionRepositoryPort.saveAndFlush(p1);

			// Secondo aggiornamento (con versione obsoleta)
			p2.setUpdatedAt(LocalDateTime.now());

			assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
				permissionRepositoryPort.saveAndFlush(p2); // Deve fallire
				// per conflitto di version
			});
		}

	}

	@Nested
	class AuditTest {

		@Test
		void testAuditFieldsAreSet() {
			// Preparazione
			Group group = Group.builder().groupName("groupY").build();
			groupRepositoryPort.save(group);

			GroupLevel groupLevel = GroupLevel.builder().groupName("groupY").levelName("audit").build();
			groupLevelRepositoryPort.save(groupLevel);

			User user = User.builder().subject(123456L).fullname("user").email("user@gmail" + ".com").build();
			userRepositoryPort.save(user);

			Permission permission = Permission.builder()
				.subject(123456L)
				.groupName("groupY")
				.levelName("audit")
				.build();

			// Salvo
			permission = permissionRepositoryPort.saveAndFlush(permission);

			// Verifico campi audit
			assertNotNull(permission.getCreatedAt());
			assertNotNull(permission.getUpdatedAt());
		}

	}

}
