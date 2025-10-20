package com.example.ProjectTeam121.Entity;

import com.example.ProjectTeam121.utils.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    /** Khóa chính UUID */
    @Id
    @Column(name = "ID", nullable = false, updatable = false, length = 36)
    private String id = UUID.randomUUID().toString();

    /** Trạng thái hoạt động (1 = active, 0 = inactive) */
    @Column(name = "FLAG_STATUS", nullable = false)
    private Integer flagStatus = 1;

    /** Đánh dấu xóa mềm (0 = chưa xóa, 1 = đã xóa) */
    @Column(name = "IS_DELETED", nullable = false)
    private Integer isDeleted = 0;

    /** Phiên bản để lock cập nhật đồng thời */
    @Version
    @Column(name = "VERSION")
    private Integer version;

    /** Người tạo bản ghi */
    @CreatedBy
    @Column(name = "CREATED_BY", length = 100, updatable = false)
    private String createdBy;

    /** Ngày tạo bản ghi */
    @CreatedDate
    @Column(name = "CREATE_DATE", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createDate;

    /** Người cập nhật gần nhất */
    @LastModifiedBy
    @Column(name = "LAST_UPDATED_BY", length = 100, insertable = false)
    private String lastUpdatedBy;

    /** Ngày cập nhật gần nhất */
    @LastModifiedDate
    @Column(name = "LAST_UPDATE_DATE", insertable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastUpdateDate;

    /** Dùng để bỏ qua auditing khi cần */
    private static final ThreadLocal<Boolean> skipAuditing = ThreadLocal.withInitial(() -> false);

    public static void setSkipAuditing(boolean skip) {
        skipAuditing.set(skip);
    }

    @PrePersist
    protected void onCreate() {
        if (flagStatus == null) flagStatus = 1;
        if (isDeleted == null) isDeleted = 0;

        if (!Boolean.TRUE.equals(skipAuditing.get())) {
            if (this.createdBy == null || this.createdBy.isEmpty()) {
                this.createdBy = SecurityUtils.getCurrentUsername();
            }
            this.createDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (!Boolean.TRUE.equals(skipAuditing.get())) {
            this.lastUpdatedBy = SecurityUtils.getCurrentUsername();
            this.lastUpdateDate = LocalDateTime.now();
        }
    }
}
