CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE s text;
BEGIN
  FOREACH s IN ARRAY ARRAY ['hospital_a', 'hospital_b'] LOOP
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', s);
    
    -- Table: patients
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.patients (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        first_name text NOT NULL,
        last_name text NOT NULL,
        dob date NULL,
        gender text NULL,
        email text NULL,
        phone text NULL,
        address text NULL,
        blood_group text NULL,
        occupation text NULL,
        emergency_contact_name text NULL,
        emergency_contact_phone text NULL,
        insurance_provider text NULL,
        policy_number text NULL,
        middle_name text NULL,
        suffix text NULL,
        preferred_name text NULL,
        ssn text NULL,
        nationality text NULL,
        primary_language text NULL,
        religion text NULL,
        marital_status text NULL,
        weight numeric NULL,
        height numeric NULL,
        bmi numeric NULL,
        tobacco_use text NULL,
        alcohol_use text NULL,
        drug_use text NULL,
        exercise_frequency text NULL,
        dietary_preference text NULL,
        organ_donor boolean NULL,
        advanced_directive boolean NULL,
        preferred_pharmacy text NULL,
        pharmacy_phone text NULL,
        employer_name text NULL,
        employer_phone text NULL,
        employer_address text NULL,
        emergency_contact_relation text NULL,
        emergency_contact_email text NULL,
        emergency_contact_address text NULL,
        primary_physician_name text NULL,
        primary_physician_phone text NULL,
        referring_physician_name text NULL,
        reason_for_admission text NULL,
        known_allergies text NULL,
        past_medical_conditions text NULL,
        past_surgeries text NULL,
        current_medications text NULL,
        family_medical_history text NULL,
        comments text NULL,
        medical_history jsonb NOT NULL DEFAULT '{}'::jsonb,
        created_at timestamptz NOT NULL DEFAULT now(),
        updated_at timestamptz NOT NULL DEFAULT now()
      );
    $f$, s);

    -- Table: doctors
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.doctors (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        first_name text NOT NULL,
        last_name text NOT NULL,
        email text NULL,
        specialisation text NOT NULL,
        licence_number text NOT NULL,
        tenant_id text NOT NULL,
        created_at timestamptz NOT NULL DEFAULT now()
      );
    $f$, s);

    -- Table: beds
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.beds (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        ward text NOT NULL,
        room text NOT NULL,
        bed_number text NOT NULL,
        status text NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'CLEANING', 'RESERVED')),
        current_admission_id uuid NULL,
        created_at timestamptz NOT NULL DEFAULT now(),
        UNIQUE (ward, room, bed_number)
      );
    $f$, s);

    -- Table: admissions
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.admissions (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        patient_id uuid NOT NULL REFERENCES %I.patients(id),
        bed_id uuid NOT NULL REFERENCES %I.beds(id),
        admitting_doctor_id uuid NULL REFERENCES %I.doctors(id),
        admitted_at timestamptz NOT NULL DEFAULT now(),
        discharged_at timestamptz NULL,
        diagnosis text NULL,
        notes text NULL,
        vitals_log jsonb NOT NULL DEFAULT '[]'::jsonb,
        created_by text NULL,
        created_at timestamptz NOT NULL DEFAULT now()
      );
    $f$, s, s, s, s);

    -- Table: appointments
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.appointments (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        patient_id uuid NOT NULL REFERENCES %I.patients(id),
        doctor_id uuid NOT NULL REFERENCES %I.doctors(id),
        start_time timestamptz NOT NULL,
        end_time timestamptz NOT NULL,
        status text NOT NULL DEFAULT 'BOOKED' CHECK (status IN ('BOOKED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
        notes text NULL,
        created_at timestamptz NOT NULL DEFAULT now(),
        UNIQUE (doctor_id, start_time)
      );
    $f$, s, s, s);

    -- Table: audit_log
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.audit_log (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        tenant_id text NOT NULL,
        user_id text NULL,
        action text NOT NULL,
        entity_type text NULL,
        entity_id text NULL,
        patient_id uuid NULL,
        ip_address text NULL,
        occurred_at timestamptz NOT NULL DEFAULT now(),
        metadata jsonb NOT NULL DEFAULT '{}'::jsonb
      );
    $f$, s);

    -- Indices
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I.appointments (doctor_id, start_time)', 'idx_app_doc_time_' || s, s);
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I.appointments (patient_id)', 'idx_app_pat_' || s, s);
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I.admissions (patient_id)', 'idx_adm_pat_' || s, s);
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I.admissions (bed_id) WHERE discharged_at IS NULL', 'idx_adm_active_' || s, s);

    -- Initial Seeding
    -- Doctors
    EXECUTE format($f$
      INSERT INTO %I.doctors (first_name, last_name, email, specialisation, licence_number, tenant_id)
      VALUES 
        ('Alan', 'Turing', 'alan.turing@mednex.local', 'Neurology', 'LIC-001', %L),
        ('Ada', 'Lovelace', 'ada.lovelace@mednex.local', 'Surgery', 'LIC-002', %L),
        ('Grace', 'Hopper', 'grace.hopper@mednex.local', 'Pediatrics', 'LIC-003', %L),
        ('John', 'Neumann', 'john.vn@mednex.local', 'Cardiology', 'LIC-004', %L),
        ('Margaret', 'Hamilton', 'm.hamilton@mednex.local', 'Systems', 'LIC-005', %L)
      ;
    $f$, s, s, s, s, s, s);

    -- Patients
    EXECUTE format($f$
      INSERT INTO %I.patients (first_name, last_name, dob, gender, email, phone, blood_group, occupation)
      VALUES 
        ('John', 'Doe', '1985-05-15', 'MALE', 'john.doe@email.com', '123-456-7890', 'O+', 'Software Engineer'),
        ('Jane', 'Smith', '1992-08-22', 'FEMALE', 'jane.smith@email.com', '987-654-3210', 'A-', 'Architect'),
        ('Robert', 'Johnson', '1970-01-10', 'MALE', 'robert.j@email.com', '555-0101', 'B+', 'Teacher'),
        ('Emily', 'Davis', '1995-11-30', 'FEMALE', 'emily.d@email.com', '555-0202', 'AB-', 'Designer'),
        ('Michael', 'Wilson', '1982-03-14', 'MALE', 'm.wilson@email.com', '555-0303', 'O-', 'Analyst')
      ON CONFLICT DO NOTHING;
    $f$, s);

    -- Beds
    EXECUTE format($f$
      INSERT INTO %I.beds (ward, room, bed_number, status)
      VALUES 
        ('ICU', '101', 'A', 'AVAILABLE'),
        ('ICU', '101', 'B', 'OCCUPIED'),
        ('ICU', '102', 'A', 'AVAILABLE'),
        ('General', '201', '1', 'OCCUPIED'),
        ('General', '201', '2', 'AVAILABLE'),
        ('General', '201', '3', 'AVAILABLE'),
        ('General', '202', '1', 'AVAILABLE'),
        ('General', '202', '2', 'AVAILABLE'),
        ('Post-Op', '305', '1', 'AVAILABLE'),
        ('Emergency', 'ER-1', '1', 'AVAILABLE')
      ON CONFLICT DO NOTHING;
    $f$, s);

  END LOOP;
END $$;

-- Grants
DO $$
DECLARE s text;
BEGIN
  -- We assume the user specified in Docker environment (mednex) is the owner/superuser.
  -- These grants are for ensuring explicit access if mednex is not superuser.
  FOREACH s IN ARRAY ARRAY ['hospital_a', 'hospital_b'] LOOP
    EXECUTE format('GRANT USAGE, CREATE ON SCHEMA %I TO mednex', s);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO mednex', s);
    EXECUTE format('GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA %I TO mednex', s);
  END LOOP;
END $$;
