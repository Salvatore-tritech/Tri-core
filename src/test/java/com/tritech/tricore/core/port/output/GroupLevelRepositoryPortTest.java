package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.Group;
import com.tritech.tricore.core.domain.GroupLevel;
import com.tritech.tricore.core.domain.primarykeys.GroupLevelId;
import com.tritech.tricore.shared.config.TestJpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class GroupLevelRepositoryPortTest {

    @Autowired
    private GroupLevelRepositoryPort groupLevelRepositoryPort;

    @Autowired
    private GroupRepositoryPort groupRepositoryPort;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testSaveAndRetrieveGroupLevel() {
        Group group = Group.builder()
                .groupName("admin")
                .build();
        groupRepositoryPort.save(group);

        GroupLevel groupLevel = GroupLevel.builder()
                .groupName("admin")
                .levelName("superadmin")
                .build();
        groupLevelRepositoryPort.save(groupLevel);

        // Importante: svuota e ripulisce il contesto per evitare cache
        entityManager.flush();
        entityManager.clear();

        TypedQuery<GroupLevel> query = entityManager.createQuery(
                "SELECT gl FROM GroupLevel gl JOIN FETCH gl.group WHERE gl" +
                        ".groupName = :groupName AND gl.levelName = :levelName",
                GroupLevel.class);
        query.setParameter("groupName", "admin");
        query.setParameter("levelName", "superadmin");

        GroupLevel fetchedGroupLevel = query.getSingleResult();

        assertNotNull(fetchedGroupLevel);
        assertEquals("admin", fetchedGroupLevel.getGroupName());
        assertNotNull(fetchedGroupLevel.getGroup());
        assertEquals("admin", fetchedGroupLevel.getGroup().getGroupName());
    }


    @Test
    void testFindAllGroupLevels() {

        Group group1 = Group.builder().groupName("admin").build();
        Group group2 = Group.builder().groupName("user").build();

        groupRepositoryPort.save(group1);
        groupRepositoryPort.save(group2);

        // Creazione dei GroupLevel
        GroupLevel groupLevel1 = GroupLevel.builder()
                .groupName("admin")
                .levelName("superadmin")
                .build();

        GroupLevel groupLevel2 = GroupLevel.builder()
                .groupName("user")
                .levelName("entry user")
                .build();

        GroupLevel groupLevel3 = GroupLevel.builder()
                .groupName("user")
                .levelName("mid user")
                .build();

        groupLevelRepositoryPort.save(groupLevel1);
        groupLevelRepositoryPort.save(groupLevel2);
        groupLevelRepositoryPort.save(groupLevel3);

        // Azione: recupero tutti i groupLevel
        List<GroupLevel> allGroupLevels = groupLevelRepositoryPort.findAll();

        // Verifica: controllo che tutti i groupLevel siano stati recuperati
        assertEquals(3, allGroupLevels.size());
        assertTrue(allGroupLevels.stream().anyMatch(gl ->
                gl.getGroupName().equals("admin") && Objects.equals(gl.getLevelName(), "superadmin")));
        assertTrue(allGroupLevels.stream().anyMatch(gl ->
                gl.getGroupName().equals("user") && Objects.equals(gl.getLevelName(), "entry user")));
        assertTrue(allGroupLevels.stream().anyMatch(gl ->
                gl.getGroupName().equals("user") && Objects.equals(gl.getLevelName(), "mid user")));
    }

    @Test
    void testDeleteGroupLevel() {
        // Preparazione: creo i gruppi necessari
        Group group1 = Group.builder().groupName("admin").build();
        Group group2 = Group.builder().groupName("user").build();

        groupRepositoryPort.save(group1);
        groupRepositoryPort.save(group2);

        // Creazione dei GroupLevel
        GroupLevel groupLevel1 = GroupLevel.builder()
                .groupName("admin")
                .levelName("superadmin")
                .build();

        GroupLevel groupLevel2 = GroupLevel.builder()
                .groupName("user")
                .levelName("basic user")
                .build();

        groupLevelRepositoryPort.save(groupLevel1);
        groupLevelRepositoryPort.save(groupLevel2);

        // Verifico che ci siano due record
        List<GroupLevel> allGroupLevelsBefore = groupLevelRepositoryPort.findAll();
        assertEquals(2, allGroupLevelsBefore.size());

        // Azione: elimino un GroupLevel
        groupLevelRepositoryPort.delete(groupLevel1);

        // Verifica: controllo che sia stato eliminato
        List<GroupLevel> allGroupLevelsAfter = groupLevelRepositoryPort.findAll();
        assertEquals(1, allGroupLevelsAfter.size());

        // Verifico che sia stato eliminato il record giusto
        GroupLevelId id1 = new GroupLevelId("admin", "superadmin");
        GroupLevelId id2 = new GroupLevelId("user", "basic user");

        assertFalse(groupLevelRepositoryPort.findById(id1).isPresent());
        assertTrue(groupLevelRepositoryPort.findById(id2).isPresent());
    }

    @Test
    void testDeleteAndVerifyForeignKeyConstraint() {
        // Preparazione: creo un gruppo
        Group group = Group.builder().groupName("admin").build();
        groupRepositoryPort.save(group);

        // Creazione di due GroupLevel associati al gruppo
        GroupLevel groupLevel1 = GroupLevel.builder()
                .groupName("admin")
                .levelName("superadmin")
                .build();

        GroupLevel groupLevel2 = GroupLevel.builder()
                .groupName("admin")
                .levelName("basic admin")
                .build();

        groupLevelRepositoryPort.save(groupLevel1);
        groupLevelRepositoryPort.save(groupLevel2);

        // Verifico che ci siano due GroupLevel
        List<GroupLevel> groupLevels = groupLevelRepositoryPort.findAll();
        assertEquals(2, groupLevels.size());

        // Elimino uno dei GroupLevel
        groupLevelRepositoryPort.delete(groupLevel1);

        // Verifico che sia stato eliminato solo il GroupLevel selezionato
        List<GroupLevel> remainingGroupLevels = groupLevelRepositoryPort.findAll();
        assertEquals(1, remainingGroupLevels.size());
        assertEquals("basic admin", remainingGroupLevels.getFirst().getLevelName());

        // Verifico che il gruppo esista ancora (il vincolo di integrità referenziale è rispettato)
        assertTrue(groupRepositoryPort.findById("admin").isPresent());
    }

    @Test
    void testBulkDelete() {
        // Preparazione: creo i gruppi necessari
        Group group1 = Group.builder().groupName("admin").build();
        Group group2 = Group.builder().groupName("user").build();

        groupRepositoryPort.save(group1);
        groupRepositoryPort.save(group2);

        // Creazione dei GroupLevel
        GroupLevel groupLevel1 = GroupLevel.builder()
                .groupName("admin")
                .levelName("superadmin")
                .build();

        GroupLevel groupLevel2 = GroupLevel.builder()
                .groupName("user")
                .levelName("entry user")
                .build();

        GroupLevel groupLevel3 = GroupLevel.builder()
                .groupName("user")
                .levelName("mid user")
                .build();

        groupLevelRepositoryPort.save(groupLevel1);
        groupLevelRepositoryPort.save(groupLevel2);
        groupLevelRepositoryPort.save(groupLevel3);

        // Verifico che ci siano tre record
        List<GroupLevel> allGroupLevelsBefore = groupLevelRepositoryPort.findAll();
        assertEquals(3, allGroupLevelsBefore.size());

        // Azione: elimino tutti i GroupLevel associati al gruppo "user"
        List<GroupLevel> userGroupLevels = allGroupLevelsBefore.stream()
                .filter(gl -> gl.getGroupName().equals("user"))
                .toList();

        groupLevelRepositoryPort.deleteAll(userGroupLevels);

        // Verifica: controllo che siano stati eliminati i record giusti
        List<GroupLevel> allGroupLevelsAfter = groupLevelRepositoryPort.findAll();
        assertEquals(1, allGroupLevelsAfter.size());
        assertEquals("admin", allGroupLevelsAfter.get(0).getGroupName());
        assertEquals("superadmin", allGroupLevelsAfter.get(0).getLevelName());
    }

    @Test
    void testDeleteGroupCascadesToGroupLevels() {
        // Preparazione: creo i gruppi necessari
        Group group1 = Group.builder().groupName("admin").build();
        Group group2 = Group.builder().groupName("user").build();

        groupRepositoryPort.save(group1);
        groupRepositoryPort.save(group2);

        // Creazione dei GroupLevel
        GroupLevel groupLevel1 = GroupLevel.builder()
                .groupName("admin")
                .levelName("superadmin")
                .build();

        GroupLevel groupLevel2 = GroupLevel.builder()
                .groupName("user")
                .levelName("entry user")
                .build();

        GroupLevel groupLevel3 = GroupLevel.builder()
                .groupName("user")
                .levelName("mid user")
                .build();

        groupLevelRepositoryPort.save(groupLevel1);
        groupLevelRepositoryPort.save(groupLevel2);
        groupLevelRepositoryPort.save(groupLevel3);

        // Verifico che ci siano tre GroupLevel
        List<GroupLevel> allGroupLevelsBefore = groupLevelRepositoryPort.findAll();
        assertEquals(3, allGroupLevelsBefore.size());

        // Azione: elimino il gruppo "user"
        groupRepositoryPort.delete(group2);

        // Importante: eseguo un flush e clear per sincronizzare con il database
        entityManager.flush();
        entityManager.clear();

        // Verifica con query nativa per assicurarsi che i record siano stati eliminati
        Query nativeQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM group_levels WHERE group_name = :groupName");
        nativeQuery.setParameter("groupName", "user");

        Number countResult = (Number) nativeQuery.getSingleResult();
        assertEquals(0, countResult.intValue());

        // Verifico anche con il repository JPA
        List<GroupLevel> allGroupLevelsAfter = groupLevelRepositoryPort.findAll();
        assertEquals(1, allGroupLevelsAfter.size());
        assertEquals("admin", allGroupLevelsAfter.getFirst().getGroupName());
    }


    @Test
    void testIntegrityConstraint_GroupMustExist() {
        // Preparazione: creo un gruppo che non viene salvato nel database
        Group nonExistentGroup = Group.builder().groupName("nonexistent").build();

        // Creo un GroupLevel che fa riferimento al gruppo non esistente
        GroupLevel groupLevel = GroupLevel.builder()
                .groupName("nonexistent")  // Riferimento a un gruppo che non esiste nel DB
                .levelName("admin")
                .build();

        // Azione e Verifica: tentare di salvare dovrebbe generare un'eccezione
        assertThrows(ConstraintViolationException.class, () -> {
            groupLevelRepositoryPort.save(groupLevel);
            entityManager.flush();  // Forza l'esecuzione delle operazioni SQL
        });
    }

    @Test
    void testFindByGroupName() {
        // Preparazione: creo un gruppo con diversi livelli
        Group group = Group.builder().groupName("multiLevel").build();
        groupRepositoryPort.save(group);

        // Creo 3 livelli per lo stesso gruppo
        GroupLevel groupLevel1 = GroupLevel.builder()
                .groupName("multiLevel")
                .levelName("entry")
                .build();

        GroupLevel groupLevel2 = GroupLevel.builder()
                .groupName("multiLevel")
                .levelName("intermediate")
                .build();

        GroupLevel groupLevel3 = GroupLevel.builder()
                .groupName("multiLevel")
                .levelName("advanced")
                .build();

        groupLevelRepositoryPort.save(groupLevel1);
        groupLevelRepositoryPort.save(groupLevel2);
        groupLevelRepositoryPort.save(groupLevel3);

        // Azione: cerco tutti i livelli per il gruppo "multiLevel"
        List<GroupLevel> foundLevels = groupLevelRepositoryPort.findByGroupName("multiLevel");

        // Verifica: controllo che siano stati trovati tutti e 3 i livelli
        assertEquals(3, foundLevels.size());
        assertTrue(foundLevels.stream().allMatch(gl -> gl.getGroupName().equals("multiLevel")));
        assertTrue(foundLevels.stream().anyMatch(gl -> gl.getLevelName().equals("entry")));
        assertTrue(foundLevels.stream().anyMatch(gl -> gl.getLevelName().equals("intermediate")));
        assertTrue(foundLevels.stream().anyMatch(gl -> gl.getLevelName().equals("advanced")));
    }
}
