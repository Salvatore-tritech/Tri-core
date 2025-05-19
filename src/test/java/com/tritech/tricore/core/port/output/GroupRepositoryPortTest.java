package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.Group;
import com.tritech.tricore.shared.config.TestJpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class GroupRepositoryPortTest {

    @Autowired
    private GroupRepositoryPort groupRepositoryPort;

    @Test
    public void testSaveAndRetrieveGroup() {
        // Preparazione: creo un oggetto Group
        Group group = Group.builder()
                .groupName("admin")
                .build();

        // Azione: salvo il gruppo nel database
        Group savedGroup = groupRepositoryPort.save(group);

        // Verifica: controllo che il gruppo sia stato salvato correttamente
        assertNotNull(savedGroup);
        assertEquals("admin", savedGroup.getGroupName());

        // Azione: recupero il gruppo dal database
        Optional<Group> retrievedGroupOpt = groupRepositoryPort.findById("admin");

        // Verifica: controllo che il gruppo sia stato recuperato correttamente
        assertTrue(retrievedGroupOpt.isPresent());
        Group retrievedGroup = retrievedGroupOpt.get();
        assertEquals("admin", retrievedGroup.getGroupName());
        assertNotNull(retrievedGroup.getCreatedAt());
    }

    @Test
    public void testFindAllGroups() {
        // Preparazione: creo e salvo più gruppi
        LocalDateTime now = LocalDateTime.now();

        Group group1 = Group.builder().groupName("admin").build();
        Group group2 = Group.builder().groupName("user").build();
        Group group3 = Group.builder().groupName("guest").build();


        groupRepositoryPort.save(group1);
        groupRepositoryPort.save(group2);
        groupRepositoryPort.save(group3);

        // Azione: recupero tutti i gruppi
        List<Group> allGroups = groupRepositoryPort.findAll();

        // Verifica: controllo che tutti i gruppi siano stati recuperati
        assertEquals(3, allGroups.size());
        assertTrue(allGroups.stream().anyMatch(g -> g.getGroupName().equals("admin")));
        assertTrue(allGroups.stream().anyMatch(g -> g.getGroupName().equals("user")));
        assertTrue(allGroups.stream().anyMatch(g -> g.getGroupName().equals("guest")));
    }

    @Test
    public void testUpdateGroup() {
        // Preparazione: creo e salvo un gruppo
        Group group = Group.builder()
                .groupName("moderator")
                .build();

        groupRepositoryPort.save(group);

        // Recupero il gruppo e aggiorno l'updatedAt
        Optional<Group> savedGroupOpt = groupRepositoryPort.findById("moderator");
        assertTrue(savedGroupOpt.isPresent());

        Group savedGroup = savedGroupOpt.get();
        LocalDateTime updatedTime = LocalDateTime.now().plusHours(1);
        savedGroup.setUpdatedAt(updatedTime);

        // Azione: salvo le modifiche
        Group updatedGroup = groupRepositoryPort.save(savedGroup);

        // Verifica: controllo che l'aggiornamento sia avvenuto correttamente
        assertEquals(updatedTime, updatedGroup.getUpdatedAt());

        // Recupero nuovamente per verificare la persistenza
        Optional<Group> retrievedUpdatedOpt = groupRepositoryPort.findById("moderator");
        assertTrue(retrievedUpdatedOpt.isPresent());
        assertEquals(updatedTime, retrievedUpdatedOpt.get().getUpdatedAt());
    }

    @Test
    public void testDeleteGroup() {
        // Preparazione: creo e salvo un gruppo
        Group group = Group.builder()
                .groupName("temporary")
                .build();

        groupRepositoryPort.save(group);

        // Verifica che il gruppo esista
        assertTrue(groupRepositoryPort.existsById("temporary"));

        // Azione: elimino il gruppo
        groupRepositoryPort.delete(group);

        // Verifica: controllo che il gruppo sia stato eliminato
        assertFalse(groupRepositoryPort.existsById("temporary"));
    }

    @Test
    public void testGroupCount() {
        // Preparazione: creo e salvo più gruppi
        LocalDateTime now = LocalDateTime.now();

        Group group1 = Group.builder().groupName("role1").build();
        Group group2 = Group.builder().groupName("role2").build();

        groupRepositoryPort.save(group1);
        groupRepositoryPort.save(group2);

        // Azione: conto i gruppi
        long count = groupRepositoryPort.count();

        // Verifica: controllo che il conteggio sia corretto
        assertEquals(2, count);

        // Aggiungo un altro gruppo
        Group group3 = Group.builder().groupName("role3").build();
        group3.setCreatedAt(now);
        group3.setUpdatedAt(now);
        groupRepositoryPort.save(group3);

        // Verifico che il conteggio sia aggiornato
        assertEquals(3, groupRepositoryPort.count());
    }
}
