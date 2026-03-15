package com.mednex.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "admissions")
public class Admission {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bed_id", nullable = false)
	private Bed bed;

	@Column(name = "admitted_at", nullable = false)
	private Instant admittedAt = Instant.now();

	@Column(name = "discharged_at")
	private Instant dischargedAt;

	private String notes;

	@Column(name = "created_by")
	private String createdBy;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "vitals_log", columnDefinition = "jsonb")
	private String vitals = "[]";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Bed getBed() {
		return bed;
	}

	public void setBed(Bed bed) {
		this.bed = bed;
	}

	public Instant getAdmittedAt() {
		return admittedAt;
	}

	public void setAdmittedAt(Instant admittedAt) {
		this.admittedAt = admittedAt;
	}

	public Instant getDischargedAt() {
		return dischargedAt;
	}

	public void setDischargedAt(Instant dischargedAt) {
		this.dischargedAt = dischargedAt;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getVitals() {
		return vitals;
	}

	public void setVitals(String vitals) {
		this.vitals = vitals;
	}
}
