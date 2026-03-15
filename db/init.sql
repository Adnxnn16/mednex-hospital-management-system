CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
  s text;
BEGIN
  FOREACH s IN ARRAY ARRAY['hospital_a', 'hospital_b'] LOOP
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
        medical_history jsonb NOT NULL DEFAULT '{}'::jsonb,
        created_at timestamptz NOT NULL DEFAULT now(),
        updated_at timestamptz NOT NULL DEFAULT now()
      );
    $f$, s);

    -- Table: doctors
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.doctors (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        full_name text NOT NULL,
        email text NULL,
        specialty text NULL,
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
        status text NOT NULL DEFAULT 'AVAILABLE',
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
        admitted_at timestamptz NOT NULL DEFAULT now(),
        discharged_at timestamptz NULL,
        notes text NULL,
        vitals_log jsonb NOT NULL DEFAULT '[]'::jsonb,
        created_by text NULL,
        created_at timestamptz NOT NULL DEFAULT now()
      );
    $f$, s, s, s);

    -- Index: admissions_one_active_bed
    EXECUTE format($f$
      CREATE UNIQUE INDEX IF NOT EXISTS admissions_one_active_bed
      ON %I.admissions (bed_id)
      WHERE discharged_at IS NULL;
    $f$, s);

    -- Table: appointments
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.appointments (
        id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
        patient_id uuid NOT NULL REFERENCES %I.patients(id),
        doctor_id uuid NOT NULL REFERENCES %I.doctors(id),
        start_time timestamptz NOT NULL,
        end_time timestamptz NOT NULL,
        status text NOT NULL DEFAULT 'BOOKED',
        created_at timestamptz NOT NULL DEFAULT now(),
        UNIQUE (doctor_id, start_time)
      );
    $f$, s, s, s);

    -- Table: audit_log
    EXECUTE format($f$
      CREATE TABLE IF NOT EXISTS %I.audit_log (
        id bigserial PRIMARY KEY,
        tenant_id text NOT NULL,
        user_id text NULL,
        action text NOT NULL,
        entity_type text NULL,
        entity_id text NULL,
        occurred_at timestamptz NOT NULL DEFAULT now(),
        metadata jsonb NOT NULL DEFAULT '{}'::jsonb
      );
    $f$, s);

    -- SEEDING
    
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

    -- Doctors
    EXECUTE format($f$
      INSERT INTO %I.doctors (full_name, email, specialty)
      VALUES
        ('Dr. Alan Turing', 'alan.turing@mednex.local', 'Neurology'),
        ('Dr. Ada Lovelace', 'ada.lovelace@mednex.local', 'Surgery'),
        ('Dr. Grace Hopper', 'grace.hopper@mednex.local', 'Pediatrics'),
        ('Dr. John von Neumann', 'john.vn@mednex.local', 'Cardiology'),
        ('Dr. Margaret Hamilton', 'm.hamilton@mednex.local', 'Systems')
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

    -- Appointment 1
    EXECUTE format($f$
      INSERT INTO %I.appointments (patient_id, doctor_id, start_time, end_time, status)
      SELECT p.id, d.id, CURRENT_DATE + interval '9 hours', CURRENT_DATE + interval '9 hours 30 minutes', 'BOOKED'
      FROM %I.patients p, %I.doctors d
      WHERE p.first_name = 'John' AND d.full_name = 'Dr. Alan Turing'
      ON CONFLICT DO NOTHING;
    $f$, s, s, s);

    -- Appointment 2
    EXECUTE format($f$
      INSERT INTO %I.appointments (patient_id, doctor_id, start_time, end_time, status)
      SELECT p.id, d.id, CURRENT_DATE + interval '10 hours', CURRENT_DATE + interval '10 hours 45 minutes', 'BOOKED'
      FROM %I.patients p, %I.doctors d
      WHERE p.first_name = 'Jane' AND d.full_name = 'Dr. Ada Lovelace'
      ON CONFLICT DO NOTHING;
    $f$, s, s, s);

    -- Admission 1
    EXECUTE format($f$
      INSERT INTO %I.admissions (patient_id, bed_id, admitted_at, notes, vitals_log)
      SELECT p.id, b.id, CURRENT_DATE - interval '2 days', 'Post-surgery recovery routine.', '[]'::jsonb
      FROM %I.patients p, %I.beds b
      WHERE p.first_name = 'Jane' AND b.ward = 'ICU' AND b.bed_number = 'B'
      ON CONFLICT DO NOTHING;
    $f$, s, s, s);

    -- Admission 2
    EXECUTE format($f$
      INSERT INTO %I.admissions (patient_id, bed_id, admitted_at, notes, vitals_log)
      SELECT p.id, b.id, CURRENT_DATE - interval '10 hours', 'Observation for mild fever and dehydration.', 
        '[{"bloodPressure": "118/76", "heartRate": "82", "temperature": "100.2", "oxygenLevel": "98%%", "recordedBy": "nurse1"}]'::jsonb
      FROM %I.patients p, %I.beds b
      WHERE p.first_name = 'Robert' AND b.ward = 'General' AND b.room = '201' AND b.bed_number = '1'
      ON CONFLICT DO NOTHING;
    $f$, s, s, s);

  END LOOP;
END $$;
