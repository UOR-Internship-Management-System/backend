-- ==============================================================================
-- Migration V095: Seed comprehensive multi-page data for pagination validation
-- ==============================================================================

-- 1. Seed 25 Student User Accounts
INSERT INTO user_accounts (id, university_email, password_hash, account_status)
VALUES
    ('11111111-0000-0000-0000-000000000001', 'sc2022001@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000002', 'sc2022002@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000003', 'sc2022003@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000004', 'sc2022004@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000005', 'sc2022005@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000006', 'sc2022006@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000007', 'sc2022007@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000008', 'sc2022008@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000009', 'sc2022009@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000010', 'sc2022010@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000011', 'sc2022011@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000012', 'sc2022012@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000013', 'sc2022013@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000014', 'sc2022014@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000015', 'sc2022015@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000016', 'sc2022016@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000017', 'sc2022017@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000018', 'sc2022018@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000019', 'sc2022019@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000020', 'sc2022020@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000021', 'sc2022021@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000022', 'sc2022022@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000023', 'sc2022023@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000024', 'sc2022024@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE'),
    ('11111111-0000-0000-0000-000000000025', 'sc2022025@dcs.ruh.ac.lk', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy', 'ACTIVE')
ON CONFLICT (university_email) DO NOTHING;

-- 2. Grant ROLE_STUDENT to all 25 Student User Accounts
INSERT INTO user_roles (user_id, role_id)
SELECT ua.id, r.id
FROM user_accounts ua
CROSS JOIN roles r
WHERE ua.university_email LIKE 'sc20220%@dcs.ruh.ac.lk'
  AND r.name = 'ROLE_STUDENT'
ON CONFLICT DO NOTHING;

-- 3. Seed 25 Eligible Students (linked to user_accounts)
INSERT INTO eligible_students (id, index_number, university_email, full_name, academic_level, is_active, user_account_id)
VALUES
    ('22222222-0000-0000-0000-000000000001', 'SC/2022/00001', 'sc2022001@dcs.ruh.ac.lk', 'Kavindu Perera', 3, TRUE, '11111111-0000-0000-0000-000000000001'),
    ('22222222-0000-0000-0000-000000000002', 'SC/2022/00002', 'sc2022002@dcs.ruh.ac.lk', 'Asha Silva', 3, TRUE, '11111111-0000-0000-0000-000000000002'),
    ('22222222-0000-0000-0000-000000000003', 'SC/2022/00003', 'sc2022003@dcs.ruh.ac.lk', 'Dulani Fernando', 3, TRUE, '11111111-0000-0000-0000-000000000003'),
    ('22222222-0000-0000-0000-000000000004', 'SC/2022/00004', 'sc2022004@dcs.ruh.ac.lk', 'Chathura Jayawardena', 3, TRUE, '11111111-0000-0000-0000-000000000004'),
    ('22222222-0000-0000-0000-000000000005', 'SC/2022/00005', 'sc2022005@dcs.ruh.ac.lk', 'Nisala Bandara', 3, TRUE, '11111111-0000-0000-0000-000000000005'),
    ('22222222-0000-0000-0000-000000000006', 'SC/2022/00006', 'sc2022006@dcs.ruh.ac.lk', 'Thilini Gunasekara', 4, TRUE, '11111111-0000-0000-0000-000000000006'),
    ('22222222-0000-0000-0000-000000000007', 'SC/2022/00007', 'sc2022007@dcs.ruh.ac.lk', 'Rashmi Wickramasinghe', 4, TRUE, '11111111-0000-0000-0000-000000000007'),
    ('22222222-0000-0000-0000-000000000008', 'SC/2022/00008', 'sc2022008@dcs.ruh.ac.lk', 'Kaveen Senanayake', 4, TRUE, '11111111-0000-0000-0000-000000000008'),
    ('22222222-0000-0000-0000-000000000009', 'SC/2022/00009', 'sc2022009@dcs.ruh.ac.lk', 'Bhanuka Rajapaksha', 3, TRUE, '11111111-0000-0000-0000-000000000009'),
    ('22222222-0000-0000-0000-000000000010', 'SC/2022/00010', 'sc2022010@dcs.ruh.ac.lk', 'Sanduni Weerasinghe', 3, TRUE, '11111111-0000-0000-0000-000000000010'),
    ('22222222-0000-0000-0000-000000000011', 'SC/2022/00011', 'sc2022011@dcs.ruh.ac.lk', 'Isuru Dissanayake', 3, TRUE, '11111111-0000-0000-0000-000000000011'),
    ('22222222-0000-0000-0000-000000000012', 'SC/2022/00012', 'sc2022012@dcs.ruh.ac.lk', 'Nimasha Mendis', 3, TRUE, '11111111-0000-0000-0000-000000000012'),
    ('22222222-0000-0000-0000-000000000013', 'SC/2022/00013', 'sc2022013@dcs.ruh.ac.lk', 'Praveen Samaraweera', 4, TRUE, '11111111-0000-0000-0000-000000000013'),
    ('22222222-0000-0000-0000-000000000014', 'SC/2022/00014', 'sc2022014@dcs.ruh.ac.lk', 'Harini Abeysekara', 4, TRUE, '11111111-0000-0000-0000-000000000014'),
    ('22222222-0000-0000-0000-000000000015', 'SC/2022/00015', 'sc2022015@dcs.ruh.ac.lk', 'Sachintha Karunaratne', 3, TRUE, '11111111-0000-0000-0000-000000000015'),
    ('22222222-0000-0000-0000-000000000016', 'SC/2022/00016', 'sc2022016@dcs.ruh.ac.lk', 'Shenali Rathnayake', 3, TRUE, '11111111-0000-0000-0000-000000000016'),
    ('22222222-0000-0000-0000-000000000017', 'SC/2022/00017', 'sc2022017@dcs.ruh.ac.lk', 'Dhanuka Jayasuriya', 3, TRUE, '11111111-0000-0000-0000-000000000017'),
    ('22222222-0000-0000-0000-000000000018', 'SC/2022/00018', 'sc2022018@dcs.ruh.ac.lk', 'Poornima Gamage', 4, TRUE, '11111111-0000-0000-0000-000000000018'),
    ('22222222-0000-0000-0000-000000000019', 'SC/2022/00019', 'sc2022019@dcs.ruh.ac.lk', 'Asanka Wijesinghe', 3, TRUE, '11111111-0000-0000-0000-000000000019'),
    ('22222222-0000-0000-0000-000000000020', 'SC/2022/00020', 'sc2022020@dcs.ruh.ac.lk', 'Charitha Alahakoon', 3, TRUE, '11111111-0000-0000-0000-000000000020'),
    ('22222222-0000-0000-0000-000000000021', 'SC/2022/00021', 'sc2022021@dcs.ruh.ac.lk', 'Nethmi Liyanage', 4, TRUE, '11111111-0000-0000-0000-000000000021'),
    ('22222222-0000-0000-0000-000000000022', 'SC/2022/00022', 'sc2022022@dcs.ruh.ac.lk', 'Malith De Silva', 3, TRUE, '11111111-0000-0000-0000-000000000022'),
    ('22222222-0000-0000-0000-000000000023', 'SC/2022/00023', 'sc2022023@dcs.ruh.ac.lk', 'Chamari Kulatunga', 4, TRUE, '11111111-0000-0000-0000-000000000023'),
    ('22222222-0000-0000-0000-000000000024', 'SC/2022/00024', 'sc2022024@dcs.ruh.ac.lk', 'Ruwantha Ekanayake', 3, TRUE, '11111111-0000-0000-0000-000000000024'),
    ('22222222-0000-0000-0000-000000000025', 'SC/2022/00025', 'sc2022025@dcs.ruh.ac.lk', 'Gimhani Fonseka', 3, TRUE, '11111111-0000-0000-0000-000000000025')
ON CONFLICT (index_number) DO NOTHING;

-- 4. Seed 25 Student Profiles
INSERT INTO student_profiles (id, student_id, display_name, headline, summary, phone, location, personal_email)
VALUES
    ('33333333-0000-0000-0000-000000000001', '22222222-0000-0000-0000-000000000001', 'Kavindu Perera', 'Computer Science Undergraduate | Full-Stack Developer', 'Passionate about cloud architecture, microservices, and modern web frameworks.', '+94 77 123 4501', 'Matara, Sri Lanka', 'kavindu.p@example.com'),
    ('33333333-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000002', 'Asha Silva', 'Undergraduate Software Engineer | DevOps & Kubernetes Enthusiast', 'Focused on CI/CD pipelines, container orchestration, and automated testing.', '+94 77 123 4502', 'Galle, Sri Lanka', 'asha.silva@example.com'),
    ('33333333-0000-0000-0000-000000000003', '22222222-0000-0000-0000-000000000003', 'Dulani Fernando', 'Data Science & Machine Learning Undergraduate', 'Experienced in Python, data visualization, predictive modeling, and deep learning.', '+94 77 123 4503', 'Colombo, Sri Lanka', 'dulani.f@example.com'),
    ('33333333-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000004', 'Chathura Jayawardena', 'Mobile Application Developer | Flutter & Android', 'Building responsive, cross-platform mobile apps with native performance.', '+94 77 123 4504', 'Kandy, Sri Lanka', 'chathura.j@example.com'),
    ('33333333-0000-0000-0000-000000000005', '22222222-0000-0000-0000-000000000005', 'Nisala Bandara', 'Backend Systems Developer | Java & Spring Boot', 'Specialized in RESTful APIs, transactional persistence, and event-driven architecture.', '+94 77 123 4505', 'Kurunegala, Sri Lanka', 'nisala.b@example.com'),
    ('33333333-0000-0000-0000-000000000006', '22222222-0000-0000-0000-000000000006', 'Thilini Gunasekara', 'Frontend Developer | React, Next.js & UI/UX', 'Creating engaging user interfaces and accessible design systems.', '+94 77 123 4506', 'Gampaha, Sri Lanka', 'thilini.g@example.com'),
    ('33333333-0000-0000-0000-000000000007', '22222222-0000-0000-0000-000000000007', 'Rashmi Wickramasinghe', 'Quality Assurance & Automation Tester', 'Focused on test-driven development, Cypress, Playwright, and Vitest testing.', '+94 77 123 4507', 'Matara, Sri Lanka', 'rashmi.w@example.com'),
    ('33333333-0000-0000-0000-000000000008', '22222222-0000-0000-0000-000000000008', 'Kaveen Senanayake', 'Cybersecurity & Systems Administration Student', 'Experienced in network protocols, ethical hacking, and vulnerability assessments.', '+94 77 123 4508', 'Colombo, Sri Lanka', 'kaveen.s@example.com'),
    ('33333333-0000-0000-0000-000000000009', '22222222-0000-0000-0000-000000000009', 'Bhanuka Rajapaksha', 'Full Stack Developer | Node.js & React', 'Building scalable enterprise applications with clean architecture.', '+94 77 123 4509', 'Galle, Sri Lanka', 'bhanuka.r@example.com'),
    ('33333333-0000-0000-0000-000000000010', '22222222-0000-0000-0000-000000000010', 'Sanduni Weerasinghe', 'Cloud Computing & Infrastructure Specialist', 'Certified AWS Cloud Practitioner focusing on serverless microservices.', '+94 77 123 4510', 'Kalutara, Sri Lanka', 'sanduni.w@example.com'),
    ('33333333-0000-0000-0000-000000000011', '22222222-0000-0000-0000-000000000011', 'Isuru Dissanayake', 'AI/ML Enthusiast | Natural Language Processing', 'Researching transformer models, speech recognition, and information extraction.', '+94 77 123 4511', 'Anuradhapura, Sri Lanka', 'isuru.d@example.com'),
    ('33333333-0000-0000-0000-000000000012', '22222222-0000-0000-0000-000000000012', 'Nimasha Mendis', 'Frontend Engineer | Modern Web Standards', 'Crafting pixel-perfect web experiences with CSS animations and responsive layouts.', '+94 77 123 4512', 'Ratnapura, Sri Lanka', 'nimasha.m@example.com'),
    ('33333333-0000-0000-0000-000000000013', '22222222-0000-0000-0000-000000000013', 'Praveen Samaraweera', 'Distributed Systems & Database Engineer', 'Passionate about PostgreSQL tuning, distributed caching, and database internals.', '+94 77 123 4513', 'Matara, Sri Lanka', 'praveen.s@example.com'),
    ('33333333-0000-0000-0000-000000000014', '22222222-0000-0000-0000-000000000014', 'Harini Abeysekara', 'Product & Software Engineering Student', 'Interested in technical product management and full-stack software delivery.', '+94 77 123 4514', 'Badulla, Sri Lanka', 'harini.a@example.com'),
    ('33333333-0000-0000-0000-000000000015', '22222222-0000-0000-0000-000000000015', 'Sachintha Karunaratne', 'Embedded Systems & IoT Developer', 'Working with microcontrollers, sensor telemetry, and low-latency networking.', '+94 77 123 4515', 'Kurunegala, Sri Lanka', 'sachintha.k@example.com'),
    ('33333333-0000-0000-0000-000000000016', '22222222-0000-0000-0000-000000000016', 'Shenali Rathnayake', 'FinTech & Blockchain Solutions Researcher', 'Exploring smart contracts, cryptographic ledgers, and secure transaction workflows.', '+94 77 123 4516', 'Colombo, Sri Lanka', 'shenali.r@example.com'),
    ('33333333-0000-0000-0000-000000000017', '22222222-0000-0000-0000-000000000017', 'Dhanuka Jayasuriya', 'Game Developer & Computer Graphics Student', 'Developing interactive 3D simulations with WebGL and Unity.', '+94 77 123 4517', 'Kandy, Sri Lanka', 'dhanuka.j@example.com'),
    ('33333333-0000-0000-0000-000000000018', '22222222-0000-0000-0000-000000000018', 'Poornima Gamage', 'Data Analyst & Business Intelligence Specialist', 'Transforming complex academic and institutional data into actionable insights.', '+94 77 123 4518', 'Matara, Sri Lanka', 'poornima.g@example.com'),
    ('33333333-0000-0000-0000-000000000019', '22222222-0000-0000-0000-000000000019', 'Asanka Wijesinghe', 'DevOps & Site Reliability Engineer', 'Automating infrastructure as code using Terraform and Docker.', '+94 77 123 4519', 'Galle, Sri Lanka', 'asanka.w@example.com'),
    ('33333333-0000-0000-0000-000000000020', '22222222-0000-0000-0000-000000000020', 'Charitha Alahakoon', 'Full Stack Web Developer | Vue & Spring', 'Designing robust enterprise web portals with secure authentication.', '+94 77 123 4520', 'Hambantota, Sri Lanka', 'charitha.a@example.com'),
    ('33333333-0000-0000-0000-000000000021', '22222222-0000-0000-0000-000000000021', 'Nethmi Liyanage', 'Computer Vision & Deep Learning Specialist', 'Object detection, image segmentation, and edge AI inference models.', '+94 77 123 4521', 'Matara, Sri Lanka', 'nethmi.l@example.com'),
    ('33333333-0000-0000-0000-000000000022', '22222222-0000-0000-0000-000000000022', 'Malith De Silva', 'Backend Software Engineer | Go & Microservices', 'High-throughput concurrency, gRPC protocols, and containerized backends.', '+94 77 123 4522', 'Colombo, Sri Lanka', 'malith.d@example.com'),
    ('33333333-0000-0000-0000-000000000023', '22222222-0000-0000-0000-000000000023', 'Chamari Kulatunga', 'Information Systems Analyst & Technical Writer', 'Bridging system requirements, UX wireframing, and software specifications.', '+94 77 123 4523', 'Gampaha, Sri Lanka', 'chamari.k@example.com'),
    ('33333333-0000-0000-0000-000000000024', '22222222-0000-0000-0000-000000000024', 'Ruwantha Ekanayake', 'Network & Security Engineer', 'Configuring routing protocols, firewalls, and enterprise access controls.', '+94 77 123 4524', 'Kandy, Sri Lanka', 'ruwantha.e@example.com'),
    ('33333333-0000-0000-0000-000000000025', '22222222-0000-0000-0000-000000000025', 'Gimhani Fonseka', 'Mobile & Cross-Platform UI Developer', 'Creating interactive experiences with Flutter and Material Design 3.', '+94 77 123 4525', 'Galle, Sri Lanka', 'gimhani.f@example.com')
ON CONFLICT (student_id) DO NOTHING;

-- 5. Seed 6 Industry Software Companies
INSERT INTO companies (id, name, website_url, contact_person, contact_email, contact_phone, notes)
VALUES
    ('44444444-0000-0000-0000-000000000001', 'WSO2 Lanka', 'https://wso2.com', 'Niroshan Perera', 'talent@wso2.com', '+94 11 214 5340', 'Enterprise middleware, identity server, and API management.'),
    ('44444444-0000-0000-0000-000000000002', 'Sysco LABS Sri Lanka', 'https://syscolabs.lk', 'Anoma Wickramasinghe', 'careers@syscolabs.com', '+94 11 202 4000', 'Enterprise food-service cloud technology and supply chain solutions.'),
    ('44444444-0000-0000-0000-000000000003', 'Virtusa Private Limited', 'https://virtusa.com', 'Sanjaya Jayatilleke', 'internships@virtusa.com', '+94 11 244 8888', 'Global digital business strategy and IT services consulting.'),
    ('44444444-0000-0000-0000-000000000004', 'IFS R&D International', 'https://ifs.com', 'Chaminda Karunaratne', 'recruitment@ifs.com', '+94 11 236 4400', 'Enterprise resource planning and industrial AI software suites.'),
    ('44444444-0000-0000-0000-000000000005', 'London Stock Exchange Group (LSEG)', 'https://lseg.com', 'Tharaka Senanayake', 'tech-careers@lseg.com', '+94 11 241 6000', 'Financial market infrastructure and mission-critical trading platforms.'),
    ('44444444-0000-0000-0000-000000000006', 'Mitra Innovation', 'https://mitrai.com', 'Dhammika Fernando', 'hr@mitrai.com', '+94 11 278 1200', 'Cloud-native product engineering, AWS consulting, and modern web apps.')
ON CONFLICT (normalized_name) DO NOTHING;

-- 6. Seed 18 Internship Requests
INSERT INTO internship_requests (id, company_id, title, description, shortlist_guidance_value, created_by_account_id)
VALUES
    ('55555555-0000-0000-0000-000000000001', '44444444-0000-0000-0000-000000000001', 'Backend Software Engineering Intern (Java & Spring Boot)', 'Developing enterprise integration middleware, microservices, and OAuth identity gateways.', 8, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000002', '44444444-0000-0000-0000-000000000001', 'API Gateway & Cloud Integration Intern', 'Focusing on distributed routing, Ballerina programming, and OpenAPI contract validation.', 6, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000003', '44444444-0000-0000-0000-000000000001', 'Frontend Web Application Intern (React & TypeScript)', 'Building interactive administrator consoles and responsive design systems.', 5, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000004', '44444444-0000-0000-0000-000000000002', 'Full Stack Cloud Developer Intern (AWS & Node.js)', 'Building scalable e-commerce microservices with Amazon Web Services and serverless Lambdas.', 10, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000005', '44444444-0000-0000-0000-000000000002', 'DevOps & Site Reliability Intern (Terraform & K8s)', 'Deploying Docker containers, Helm charts, and continuous integration pipelines.', 6, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000006', '44444444-0000-0000-0000-000000000002', 'Data Analytics & ETL Pipeline Intern (Python & SQL)', 'Building scalable telemetry extractors and analytics dashboards using PostgreSQL and BigQuery.', 7, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000007', '44444444-0000-0000-0000-000000000003', 'QA Automation Engineering Intern (Playwright & Java)', 'Writing end-to-end integration test suites and automated performance regression tests.', 8, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000008', '44444444-0000-0000-0000-000000000003', 'Mobile Application Developer Intern (Flutter)', 'Developing cross-platform mobile apps for enterprise logistics and fleet tracking.', 5, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000009', '44444444-0000-0000-0000-000000000003', 'Cybersecurity Analyst & Threat Defense Intern', 'Auditing application vulnerabilities, conducting penetration testing, and verifying SAIF guidelines.', 4, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000010', '44444444-0000-0000-0000-000000000004', 'ERP Systems Architecture Intern (C# & Angular)', 'Designing enterprise supply chain modules, data models, and business logic pipelines.', 6, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000011', '44444444-0000-0000-0000-000000000004', 'Industrial AI & Predictive Maintenance Intern', 'Applying machine learning algorithms on industrial IoT telemetry for anomaly detection.', 5, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000012', '44444444-0000-0000-0000-000000000004', 'Database Performance Tuning Intern (PostgreSQL & Oracle)', 'Query plan optimization, indexing strategies, and database connection pooling.', 5, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000013', '44444444-0000-0000-0000-000000000005', 'High-Frequency Financial Systems Intern (C++ & Linux)', 'Low-latency financial order routing, algorithmic trading engine design, and Linux kernel tuning.', 6, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000014', '44444444-0000-0000-0000-000000000005', 'Real-Time Financial Data Analytics Intern (Python & Spark)', 'Processing high-velocity market data feeds and building analytics pipelines.', 7, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000015', '44444444-0000-0000-0000-000000000005', 'Enterprise Security & Identity Access Intern', 'Implementing zero-trust access, SAML/OIDC federated auth, and cryptographic key protection.', 4, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000016', '44444444-0000-0000-0000-000000000006', 'Cloud Native App Developer Intern (Serverless & React)', 'Rapid prototyping of client solutions using AWS Amplify, Next.js, and GraphQL.', 8, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000017', '44444444-0000-0000-0000-000000000006', 'UI/UX Design Engineering Intern (Figma to Code)', 'Translating user journey wireframes into accessible React components and micro-animations.', 5, '00000000-0000-0000-0000-000000000001'),
    ('55555555-0000-0000-0000-000000000018', '44444444-0000-0000-0000-000000000006', 'Software Quality Assurance Intern (Cypress & Vitest)', 'Automating frontend unit, component, and visual regression tests.', 6, '00000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

-- 7. Seed 14 Shortlists (8 FINALIZED, 6 DRAFT)
INSERT INTO shortlists (
    id, internship_request_id, name, status, guidance_value_snapshot,
    guidance_warning_acknowledged, finalization_note, created_by_account_id,
    finalized_by_account_id, finalized_at
)
VALUES
    ('66666666-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000001', 'WSO2 Backend Interns Shortlist 2026', 'FINALIZED', 8, FALSE, 'Approved by departmental coordinator for WSO2 placement.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000002', '55555555-0000-0000-0000-000000000002', 'WSO2 API Gateway Shortlist 2026', 'FINALIZED', 6, FALSE, 'Top qualified candidate batch approved.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000003', '55555555-0000-0000-0000-000000000003', 'WSO2 Frontend React Shortlist 2026', 'FINALIZED', 5, FALSE, 'Frontend specialization candidates approved.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000004', '55555555-0000-0000-0000-000000000004', 'Sysco LABS Cloud Developers 2026', 'FINALIZED', 10, FALSE, '10 candidates selected for AWS cloud roles.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000005', '55555555-0000-0000-0000-000000000005', 'Sysco LABS DevOps Engineering 2026', 'FINALIZED', 6, FALSE, 'Finalized with Docker and Kubernetes competencies.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000006', '55555555-0000-0000-0000-000000000006', 'Sysco LABS Data Analytics Shortlist 2026', 'FINALIZED', 7, FALSE, 'Data analytics candidates finalized.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000007', '55555555-0000-0000-0000-000000000007', 'Virtusa QA Automation Batch 2026', 'FINALIZED', 8, FALSE, 'Test automation team selection finalized.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000008', '55555555-0000-0000-0000-000000000008', 'Virtusa Mobile Flutter Batch 2026', 'FINALIZED', 5, FALSE, 'Mobile development candidates finalized.', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', NOW()),
    ('66666666-0000-0000-0000-000000000009', '55555555-0000-0000-0000-000000000009', 'Virtusa Cybersecurity Draft 2026', 'DRAFT', 4, FALSE, NULL, '00000000-0000-0000-0000-000000000001', NULL, NULL),
    ('66666666-0000-0000-0000-000000000010', '55555555-0000-0000-0000-000000000010', 'IFS ERP Systems Draft 2026', 'DRAFT', 6, FALSE, NULL, '00000000-0000-0000-0000-000000000001', NULL, NULL),
    ('66666666-0000-0000-0000-000000000011', '55555555-0000-0000-0000-000000000011', 'IFS Industrial AI Draft 2026', 'DRAFT', 5, FALSE, NULL, '00000000-0000-0000-0000-000000000001', NULL, NULL),
    ('66666666-0000-0000-0000-000000000012', '55555555-0000-0000-0000-000000000012', 'IFS Database Tuning Draft 2026', 'DRAFT', 5, FALSE, NULL, '00000000-0000-0000-0000-000000000001', NULL, NULL),
    ('66666666-0000-0000-0000-000000000013', '55555555-0000-0000-0000-000000000013', 'LSEG Financial Systems Draft 2026', 'DRAFT', 6, FALSE, NULL, '00000000-0000-0000-0000-000000000001', NULL, NULL),
    ('66666666-0000-0000-0000-000000000014', '55555555-0000-0000-0000-000000000014', 'LSEG Market Analytics Draft 2026', 'DRAFT', 7, FALSE, NULL, '00000000-0000-0000-0000-000000000001', NULL, NULL)
ON CONFLICT (internship_request_id) DO NOTHING;

-- 8. Seed Shortlist Candidate Memberships
INSERT INTO shortlist_candidates (shortlist_id, student_id, selected_by_account_id)
VALUES
    ('66666666-0000-0000-0000-000000000001', '22222222-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000001', '22222222-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000001', '22222222-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000003', '22222222-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000005', '22222222-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000006', '22222222-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000007', '22222222-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001'),
    ('66666666-0000-0000-0000-000000000008', '22222222-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001')
ON CONFLICT (shortlist_id, student_id) DO NOTHING;

-- 9. Seed 20 Student Projects (attached to Student 1 and Sahan Basnayake)
INSERT INTO student_projects (id, student_id, title, description, repository_url, demo_url, start_date, end_date, include_in_cv)
VALUES
    ('77777777-0000-0000-0000-000000000001', '22222222-0000-0000-0000-000000000001', 'Smart Healthcare Diagnostics & Patient Portal', 'Full-stack patient appointment booking and diagnostic telemetry portal built with React and Spring Boot.', 'https://github.com/ruh-cs/healthcare-portal', 'https://health-demo.ruh.ac.lk', '2025-01-10', '2025-05-20', TRUE),
    ('77777777-0000-0000-0000-000000000002', '22222222-0000-0000-0000-000000000001', 'Autonomous Delivery Drone Route Optimization', 'Genetic algorithm based pathfinding and wind-resistance calculation engine for autonomous quadcopters.', 'https://github.com/ruh-cs/drone-routing', 'https://drone-demo.ruh.ac.lk', '2025-02-01', '2025-06-15', TRUE),
    ('77777777-0000-0000-0000-000000000003', '22222222-0000-0000-0000-000000000001', 'Campus Shuttle Tracking & Real-Time ETA System', 'GPS telemetry ingestion via MQTT and WebSocket broadcast for real-time university bus tracking.', 'https://github.com/ruh-cs/shuttle-tracker', 'https://shuttle.ruh.ac.lk', '2025-03-01', '2025-07-01', TRUE),
    ('77777777-0000-0000-0000-000000000004', '22222222-0000-0000-0000-000000000001', 'Collaborative Markdown & Code Editor with WebSockets', 'Operational transformation engine enabling concurrent real-time document editing.', 'https://github.com/ruh-cs/collab-editor', 'https://editor.ruh.ac.lk', '2025-04-10', '2025-08-15', TRUE),
    ('77777777-0000-0000-0000-000000000005', '22222222-0000-0000-0000-000000000001', 'Microservices Banking Core Platform', 'Transactional ledger banking service with Kafka event streaming and idempotency keys.', 'https://github.com/ruh-cs/banking-core', NULL, '2025-05-01', '2025-09-01', TRUE),
    ('77777777-0000-0000-0000-000000000006', '22222222-0000-0000-0000-000000000001', 'AI Resume Matcher & Skill Extractor', 'NLP pipeline extracting candidate technical skills and scoring role suitability automatically.', 'https://github.com/ruh-cs/resume-matcher', 'https://matcher.ruh.ac.lk', '2025-06-01', '2025-10-01', TRUE),
    ('77777777-0000-0000-0000-000000000007', '22222222-0000-0000-0000-000000000001', 'Precision Hydroponic Greenhouse Monitoring System', 'Embedded ESP32 telemetry with dashboard visualizing soil pH, moisture, and ambient temperature.', 'https://github.com/ruh-cs/smart-greenhouse', NULL, '2025-07-15', '2025-11-20', TRUE),
    ('77777777-0000-0000-0000-000000000008', '22222222-0000-0000-0000-000000000001', 'High-Performance Distributed Key-Value Store', 'Raft-consensus distributed key-value store supporting linearizable reads and atomic writes.', 'https://github.com/ruh-cs/distributed-kv', NULL, '2025-08-01', '2025-12-10', TRUE),
    ('77777777-0000-0000-0000-000000000009', '22222222-0000-0000-0000-000000000001', 'Peer-to-Peer Encrypted File Exchange Protocol', 'BitTorrent-inspired decentralized file chunking with end-to-end asymmetric cryptography.', 'https://github.com/ruh-cs/p2p-exchange', NULL, '2025-09-01', '2026-01-15', TRUE),
    ('77777777-0000-0000-0000-000000000010', '22222222-0000-0000-0000-000000000001', 'E-Commerce Real-Time Recommendations Engine', 'Collaborative filtering model with Redis caching generating real-time product recommendations.', 'https://github.com/ruh-cs/recs-engine', 'https://recs.ruh.ac.lk', '2025-10-01', '2026-02-10', TRUE),
    ('77777777-0000-0000-0000-000000000011', '22222222-0000-0000-0000-000000000001', 'Deep Learning Industrial Quality Control System', 'YOLOv8 computer vision model detecting manufacturing defects in real-time video streams.', 'https://github.com/ruh-cs/quality-control-cv', NULL, '2025-11-01', '2026-03-01', TRUE),
    ('77777777-0000-0000-0000-000000000012', '22222222-0000-0000-0000-000000000001', 'Voice-Controlled Smart Lab Ambient Controller', 'Edge speech recognition running on Raspberry Pi controlling hardware relays and HVAC.', 'https://github.com/ruh-cs/smart-lab-voice', NULL, '2025-12-01', '2026-03-15', TRUE),
    ('77777777-0000-0000-0000-000000000013', '22222222-0000-0000-0000-000000000001', 'Blockchain Land Registry Transparency Network', 'Solidity smart contract verifying title deeds and ownership histories immutably.', 'https://github.com/ruh-cs/land-registry', NULL, '2026-01-05', '2026-04-10', TRUE),
    ('77777777-0000-0000-0000-000000000014', '22222222-0000-0000-0000-000000000001', 'Distributed Log Aggregator and Metrics Engine', 'Go-based agent shipping log events into ClickHouse with Grafana query visualizations.', 'https://github.com/ruh-cs/log-metrics-engine', NULL, '2026-02-01', '2026-05-01', TRUE),
    ('77777777-0000-0000-0000-000000000015', '22222222-0000-0000-0000-000000000001', 'Personal Portfolio & ATS Curriculum Vitae Builder', 'Interactive web CV designer exporting compliant ATS PDF documents.', 'https://github.com/ruh-cs/cv-builder-web', 'https://cv.ruh.ac.lk', '2026-03-01', '2026-06-01', TRUE),
    ('77777777-0000-0000-0000-000000000016', '22222222-0000-0000-0000-000000000001', 'Interactive Graph Algorithm Visualizer', 'Visual walkthrough of Dijkstra, A-star, and Bellman-Ford algorithms with step-by-step playback.', 'https://github.com/ruh-cs/graph-algorithms', 'https://graph.ruh.ac.lk', '2026-04-01', '2026-07-01', TRUE),
    ('77777777-0000-0000-0000-000000000017', '22222222-0000-0000-0000-000000000001', 'Automated Online Code Assessment Platform', 'Sandboxed Docker execution environment running and grading student code submissions against test cases.', 'https://github.com/ruh-cs/code-judge', NULL, '2026-05-01', '2026-07-20', TRUE),
    ('77777777-0000-0000-0000-000000000018', '22222222-0000-0000-0000-000000000001', 'Real-Time Speech-to-Text Meeting Assistant', 'Whisper-powered audio stream transcription and automated meeting minutes summarizer.', 'https://github.com/ruh-cs/meeting-assistant', NULL, '2026-06-01', '2026-08-01', TRUE),
    ('77777777-0000-0000-0000-000000000019', '22222222-0000-0000-0000-000000000001', 'Traffic Incident Detection via Computer Vision', 'Highway CCTV feed analysis alerting authorities to stalled vehicles and traffic congestions.', 'https://github.com/ruh-cs/traffic-vision', NULL, '2026-07-01', '2026-08-20', TRUE),
    ('77777777-0000-0000-0000-000000000020', '22222222-0000-0000-0000-000000000001', 'Library Catalog & Digital Circulation Manager', 'Modern OPAC catalog search with barcode scanner integration and overdue alerts.', 'https://github.com/ruh-cs/library-manager', 'https://lib.ruh.ac.lk', '2026-08-01', '2026-09-01', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Also seed 20 projects for student Sahan Basnayake (SC/2021/00101) so student login testing also has full pagination
INSERT INTO student_projects (id, student_id, title, description, repository_url, demo_url, start_date, end_date, include_in_cv)
SELECT
    gen_random_uuid(),
    '534fb44e-59b0-40aa-9e2b-7528efd752c2',
    p.title || ' (v2)',
    p.description,
    p.repository_url,
    p.demo_url,
    p.start_date,
    p.end_date,
    TRUE
FROM student_projects p
WHERE p.student_id = '22222222-0000-0000-0000-000000000001'
ON CONFLICT DO NOTHING;

-- 10. Seed 12 Student Declared Skills for Student 1
INSERT INTO student_declared_skills (student_id, skill_id, competency_level)
VALUES
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000003', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000004', 'INTERMEDIATE'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000005', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000006', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000007', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000008', 'INTERMEDIATE'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000010', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000011', 'ADVANCED'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000012', 'INTERMEDIATE'),
    ('22222222-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000015', 'ADVANCED')
ON CONFLICT (student_id, skill_id) DO NOTHING;

-- 11. Seed 6 Work Experiences for Student 1
INSERT INTO student_work_experience (student_id, organization, position_title, start_date, end_date, description, cv_include, location, is_current_role)
VALUES
    ('22222222-0000-0000-0000-000000000001', 'Virtusa', 'Trainee Software Engineer', '2025-06-01', '2025-12-01', 'Contributed to microservices development with Spring Boot and PostgreSQL.', TRUE, 'Colombo, Sri Lanka', FALSE),
    ('22222222-0000-0000-0000-000000000001', 'Faculty of Science, University of Ruhuna', 'Student System Administrator', '2025-01-15', '2025-05-30', 'Assisted in managing computer laboratory Linux workstations and network switches.', TRUE, 'Matara, Sri Lanka', FALSE),
    ('22222222-0000-0000-0000-000000000001', 'Freelance Tech Solutions', 'Web Developer', '2024-06-01', '2024-12-31', 'Delivered full-stack React and Node.js solutions for regional business clients.', TRUE, 'Matara, Sri Lanka', FALSE),
    ('22222222-0000-0000-0000-000000000001', 'Open Source Contributor', 'Core Contributor', '2024-01-01', '2024-06-01', 'Submitted PRs and bug fixes for open-source UI libraries and utility packages.', TRUE, 'Remote', FALSE),
    ('22222222-0000-0000-0000-000000000001', 'IEEE Student Branch Ruhuna', 'Technical Webmaster', '2023-06-01', '2024-06-01', 'Maintained branch web portal and event registration infrastructure.', TRUE, 'Matara, Sri Lanka', FALSE),
    ('22222222-0000-0000-0000-000000000001', 'DevOps Study Group', 'Lab Coordinator', '2023-01-01', '2023-06-01', 'Conducted peer study sessions covering Git workflows and Docker containerization.', TRUE, 'Matara, Sri Lanka', FALSE)
ON CONFLICT DO NOTHING;
