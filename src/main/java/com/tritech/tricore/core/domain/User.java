package com.tritech.tricore.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * <p>Represents a user entity within the system. This class is mapped to the
 * "users" table in the database.</p>
 * <p>Each user is uniquely identified by a subject ID (obtained from Google
 * OpenId Connect) and contains additional
 * attributes such as full name, email address, and profile picture.</p>
 * <p>Includes technical fields for database versioning and audit tracking, such
 * as
 * created and updated timestamps.</p>
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * Represents the unique identifier for a user within the system. This field
     * is mapped to the "subject" column in the "users" table and serves as the
     * primary key for the User entity.
     * <p>
     * The subject is obtained from Google OpenID Connect and ensures that each
     * user is uniquely identifiable within the system.
     */
    @Id
    @Column(name = "subject", nullable = false, unique = true)
    private Long subject;

    /**
     * Represents the full name of a user within the system. This field is
     * mapped to the "fullname" column in the "users" table and is a required
     * attribute, as specified by the non-null constraint.
     */
    @Column(name = "fullname", nullable = false)
    private String fullname;

    /**
     * Represents the email address associated with a user in the system. This
     * field is mapped to the "email" column in the database and is a required
     * attribute.
     */
    @Email(message = "Invalid email address")
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * Represents the profile picture associated with a user within the system.
     * This field is mapped to the "picture" column in the "users" table in the
     * database.
     * <p>
     * The picture attribute is expected to store the URL or path of the user's
     * profile picture, enabling its retrieval and display in the system's user
     * interface.
     * <p>
     * The picture URL is obtained by Google OpenID Connect
     */
    @Column(name = "picture")
    private String picture;

    // Technical Fields

    /**
     * Represents the version of the entity for optimistic locking purposes.
     * This field is used by the persistence framework to ensure consistency and
     * prevent concurrent update conflicts during database transactions.
     */
    @Version
    private Long version;

    /**
     * Represents the creation timestamp of the entity. This field is
     * automatically populated with the date and time when the entity is
     * persisted for the first time.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private java.time.LocalDateTime createdAt;

    /**
     * Represents the timestamp of the last modification made to the entity.
     * This field is automatically updated with the current date and time
     * whenever the entity is updated in the database.
     */
    @Column(name = "updated_at")
    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;
}
