package com.mednex.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "patients")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "patient")
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	private LocalDate dob;
	private String gender;
	private String email;
	private String phone;
	private String address;

	// Structured demographic fields
	private String bloodGroup;
	private String occupation;
	private String emergencyContactName;
	private String emergencyContactPhone;
	private String insuranceProvider;
	private String policyNumber;

	// New Clinical/Demographic Fields (FR-09)
	private String middleName;
	private String suffix;
	private String preferredName;
	private String ssn;
	private String nationality;
	private String primaryLanguage;
	private String religion;
	private String maritalStatus;
	private Double weight;
	private Double height;
	private Double bmi;
	private String tobaccoUse;
	private String alcoholUse;
	private String drugUse;
	private String exerciseFrequency;
	private String dietaryPreference;
	private Boolean organDonor;
	private Boolean advancedDirective;
	private String preferredPharmacy;
	private String pharmacyPhone;
	private String employerName;
	private String employerPhone;
	private String employerAddress;
	private String emergencyContactRelation;
	private String emergencyContactEmail;
	private String emergencyContactAddress;
	private String primaryPhysicianName;
	private String primaryPhysicianPhone;
	private String referringPhysicianName;
	private String reasonForAdmission;
	private String knownAllergies;
	private String pastMedicalConditions;
	private String pastSurgeries;
	private String currentMedications;
	private String familyMedicalHistory;
	private String comments;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "medical_history", nullable = false, columnDefinition = "jsonb")
	private String medicalHistory = "{}";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	@jakarta.persistence.OneToMany(mappedBy = "patient", fetch = jakarta.persistence.FetchType.LAZY)
	@org.hibernate.annotations.BatchSize(size = 20)
	private java.util.List<Admission> admissions;

	@jakarta.persistence.OneToMany(mappedBy = "patient", fetch = jakarta.persistence.FetchType.LAZY)
	@org.hibernate.annotations.BatchSize(size = 20)
	private java.util.List<Appointment> appointments;

	// Getters and Setters
	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public LocalDate getDob() { return dob; }
	public void setDob(LocalDate dob) { this.dob = dob; }
	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getBloodGroup() { return bloodGroup; }
	public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
	public String getOccupation() { return occupation; }
	public void setOccupation(String occupation) { this.occupation = occupation; }
	public String getEmergencyContactName() { return emergencyContactName; }
	public void setEmergencyContactName(String name) { this.emergencyContactName = name; }
	public String getEmergencyContactPhone() { return emergencyContactPhone; }
	public void setEmergencyContactPhone(String ph) { this.emergencyContactPhone = ph; }
	public String getInsuranceProvider() { return insuranceProvider; }
	public void setInsuranceProvider(String i) { this.insuranceProvider = i; }
	public String getPolicyNumber() { return policyNumber; }
	public void setPolicyNumber(String p) { this.policyNumber = p; }
	
	// Delegate remaining setters to field or JSON logic in service
	public String getMiddleName() { return middleName; }
	public void setMiddleName(String s) { this.middleName = s; }
	public String getSuffix() { return suffix; }
	public void setSuffix(String s) { this.suffix = s; }
	public String getPreferredName() { return preferredName; }
	public void setPreferredName(String s) { this.preferredName = s; }
	public String getSsn() { return ssn; }
	public void setSsn(String s) { this.ssn = s; }
	public String getNationality() { return nationality; }
	public void setNationality(String s) { this.nationality = s; }
	public String getPrimaryLanguage() { return primaryLanguage; }
	public void setPrimaryLanguage(String s) { this.primaryLanguage = s; }
	public String getReligion() { return religion; }
	public void setReligion(String s) { this.religion = s; }
	public String getMaritalStatus() { return maritalStatus; }
	public void setMaritalStatus(String s) { this.maritalStatus = s; }
	public Double getWeight() { return weight; }
	public void setWeight(Double d) { this.weight = d; }
	public Double getHeight() { return height; }
	public void setHeight(Double d) { this.height = d; }
	public Double getBmi() { return bmi; }
	public void setBmi(Double d) { this.bmi = d; }
	public String getTobaccoUse() { return tobaccoUse; }
	public void setTobaccoUse(String s) { this.tobaccoUse = s; }
	public String getAlcoholUse() { return alcoholUse; }
	public void setAlcoholUse(String s) { this.alcoholUse = s; }
	public String getDrugUse() { return drugUse; }
	public void setDrugUse(String s) { this.drugUse = s; }
	public String getExerciseFrequency() { return exerciseFrequency; }
	public void setExerciseFrequency(String s) { this.exerciseFrequency = s; }
	public String getDietaryPreference() { return dietaryPreference; }
	public void setDietaryPreference(String s) { this.dietaryPreference = s; }
	public Boolean getOrganDonor() { return organDonor; }
	public void setOrganDonor(Boolean b) { this.organDonor = b; }
	public Boolean getAdvancedDirective() { return advancedDirective; }
	public void setAdvancedDirective(Boolean b) { this.advancedDirective = b; }
	public String getPreferredPharmacy() { return preferredPharmacy; }
	public void setPreferredPharmacy(String s) { this.preferredPharmacy = s; }
	public String getPharmacyPhone() { return pharmacyPhone; }
	public void setPharmacyPhone(String s) { this.pharmacyPhone = s; }
	public String getEmployerName() { return employerName; }
	public void setEmployerName(String s) { this.employerName = s; }
	public String getEmployerPhone() { return employerPhone; }
	public void setEmployerPhone(String s) { this.employerPhone = s; }
	public String getEmployerAddress() { return employerAddress; }
	public void setEmployerAddress(String s) { this.employerAddress = s; }
	public String getEmergencyContactRelation() { return emergencyContactRelation; }
	public void setEmergencyContactRelation(String s) { this.emergencyContactRelation = s; }
	public String getEmergencyContactEmail() { return emergencyContactEmail; }
	public void setEmergencyContactEmail(String s) { this.emergencyContactEmail = s; }
	public String getEmergencyContactAddress() { return emergencyContactAddress; }
	public void setEmergencyContactAddress(String s) { this.emergencyContactAddress = s; }
	public String getPrimaryPhysicianName() { return primaryPhysicianName; }
	public void setPrimaryPhysicianName(String s) { this.primaryPhysicianName = s; }
	public String getPrimaryPhysicianPhone() { return primaryPhysicianPhone; }
	public void setPrimaryPhysicianPhone(String s) { this.primaryPhysicianPhone = s; }
	public String getReferringPhysicianName() { return referringPhysicianName; }
	public void setReferringPhysicianName(String s) { this.referringPhysicianName = s; }
	public String getReasonForAdmission() { return reasonForAdmission; }
	public void setReasonForAdmission(String s) { this.reasonForAdmission = s; }
	public String getKnownAllergies() { return knownAllergies; }
	public void setKnownAllergies(String s) { this.knownAllergies = s; }
	public String getPastMedicalConditions() { return pastMedicalConditions; }
	public void setPastMedicalConditions(String s) { this.pastMedicalConditions = s; }
	public String getPastSurgeries() { return pastSurgeries; }
	public void setPastSurgeries(String s) { this.pastSurgeries = s; }
	public String getCurrentMedications() { return currentMedications; }
	public void setCurrentMedications(String s) { this.currentMedications = s; }
	public String getFamilyMedicalHistory() { return familyMedicalHistory; }
	public void setFamilyMedicalHistory(String s) { this.familyMedicalHistory = s; }
	public String getComments() { return comments; }
	public void setComments(String s) { this.comments = s; }

	public String getMedicalHistory() { return medicalHistory; }
	public void setMedicalHistory(String s) { this.medicalHistory = s; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant i) { this.createdAt = i; }
	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant i) { this.updatedAt = i; }
}
