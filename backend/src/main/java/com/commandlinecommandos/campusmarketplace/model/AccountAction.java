package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking account actions (suspension, reactivation, deletion)
 * Used for admin accountability and account recovery
 */
@Entity
@Table(name = "account_actions", indexes = {
    @Index(name = "idx_account_user", columnList = "user_id"),
    @Index(name = "idx_account_admin", columnList = "performed_by"),
    @Index(name = "idx_account_type", columnList = "action_type"),
    @Index(name = "idx_account_created", columnList = "created_at")
})
public class AccountAction {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "action_id", updatable = false, nullable = false)
    private UUID actionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "scheduled_revert_at")
    private LocalDateTime scheduledRevertAt;  // For temporary suspensions
    
    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;
    
    @Column(name = "is_reverted")
    private boolean isReverted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum ActionType {
        SUSPEND,
        REACTIVATE,
        DELETE,
        ROLE_CHANGE,
        PASSWORD_RESET,
        EMAIL_CHANGE,
        VERIFICATION_STATUS_CHANGE
    }
    
    // Constructors
    public AccountAction() {
    }
    
    public AccountAction(User user, User performedBy, ActionType actionType, String reason) {
        this.user = user;
        this.performedBy = performedBy;
        this.actionType = actionType;
        this.reason = reason;
    }

    // Getters and Setters
    public UUID getActionId() {
        return actionId;
    }

    public void setActionId(UUID actionId) {
        this.actionId = actionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getScheduledRevertAt() {
        return scheduledRevertAt;
    }

    public void setScheduledRevertAt(LocalDateTime scheduledRevertAt) {
        this.scheduledRevertAt = scheduledRevertAt;
    }

    public LocalDateTime getRevertedAt() {
        return revertedAt;
    }

    public void setRevertedAt(LocalDateTime revertedAt) {
        this.revertedAt = revertedAt;
    }

    public boolean isReverted() {
        return isReverted;
    }

    public void setReverted(boolean reverted) {
        isReverted = reverted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
