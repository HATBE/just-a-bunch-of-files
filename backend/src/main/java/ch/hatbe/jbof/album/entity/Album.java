package ch.hatbe.jbof.album.entity;

import ch.hatbe.jbof.core.entity.AuditableEntity;
import ch.hatbe.jbof.mediaFile.entity.MediaFile;
import ch.hatbe.jbof.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UuidGenerator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "albums")
public class Album extends AuditableEntity {
    @Id
    @UuidGenerator
    @Column(name = "album_id", nullable = false, updatable = false, length = 36)
    private UUID albumId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "album_media_files",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "media_file_id")
    )
    private Set<MediaFile> mediaFiles = new LinkedHashSet<>();

    @Basic(fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE) // remove the setter
    @Formula("(select count(*) from album_media_files amf where amf.album_id = album_id)")
    private long mediaFileCount;
}
