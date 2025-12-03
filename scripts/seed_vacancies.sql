-- =============================================
-- NotDjinni Vacancies Seed Script
-- =============================================
-- Run AFTER reset_and_seed.sql and after application startup
-- 
-- Employment Types (ordinal): 0=FULL_TIME, 1=PART_TIME, 2=CONTRACT, 3=TEMPORARY, 4=INTERNSHIP, 5=FREELANCE
-- Job Categories (ordinal): 0=SOFTWARE_DEV, 1=DATA_SCIENCE, 2=DEVOPS, 3=QA, 4=PRODUCT_MGMT, 5=DESIGN, 6=MARKETING, 7=SALES, 8=HR, 9=FINANCE, 10=OPERATIONS, 11=SUPPORT
-- Statuses (ordinal): 0=DRAFT, 1=ACTIVE, 2=PAUSED, 3=CLOSED, 4=EXPIRED

-- =============================================
-- Google Ukraine (company_id = 1)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(1, 'Senior Software Engineer - Cloud', 'Join Google Cloud team to build next-generation cloud infrastructure. Work on distributed systems, Kubernetes, and large-scale data processing. You''ll be designing and implementing services used by millions of developers worldwide.', 6000, 10000, 5, 0, 0, 1, NOW(), NOW()),
(1, 'Machine Learning Engineer', 'Work on cutting-edge ML models for Google Search and Assistant. Experience with TensorFlow, PyTorch, and large language models required. You''ll be pushing the boundaries of what''s possible with AI.', 7000, 12000, 4, 0, 1, 1, NOW(), NOW()),
(1, 'Site Reliability Engineer', 'Ensure Google services run smoothly 24/7. Work on automation, monitoring, and incident response. Experience with large-scale distributed systems and SRE practices required.', 5500, 9000, 3, 0, 2, 1, NOW(), NOW()),
(1, 'Software Engineering Intern', 'Summer internship program for talented CS students. Work on real Google products alongside experienced engineers. Mentorship and learning opportunities included.', 2000, 3000, 0, 4, 0, 1, NOW(), NOW());

-- =============================================
-- Microsoft Ukraine (company_id = 2)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(2, 'Azure Cloud Developer', 'Build and enhance Azure cloud services. Work with .NET, C#, and cloud-native technologies. Join a team that powers millions of businesses worldwide.', 5000, 8500, 3, 0, 0, 1, NOW(), NOW()),
(2, 'DevOps Engineer - Azure DevOps', 'Improve Azure DevOps platform used by millions of developers. Experience with CI/CD, containerization, and infrastructure as code required.', 5500, 8000, 4, 0, 2, 1, NOW(), NOW()),
(2, 'Senior QA Engineer', 'Lead quality assurance for Office 365 products. Design test strategies, automation frameworks, and ensure excellent user experience across platforms.', 4500, 7000, 5, 0, 3, 1, NOW(), NOW()),
(2, 'Product Manager - Teams', 'Define product strategy for Microsoft Teams features. Work with engineering, design, and customers to deliver impactful collaboration tools.', 6000, 9000, 5, 0, 4, 1, NOW(), NOW());

-- =============================================
-- Amazon Development Center (company_id = 3)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(3, 'Backend Engineer - AWS Lambda', 'Design and build serverless computing infrastructure. Work on AWS Lambda, event-driven architectures, and scalable systems serving millions of requests.', 5500, 9500, 4, 0, 0, 1, NOW(), NOW()),
(3, 'Data Engineer', 'Build data pipelines and analytics infrastructure for Amazon retail. Work with petabytes of data, real-time processing, and ML pipelines.', 5000, 8500, 3, 0, 1, 1, NOW(), NOW()),
(3, 'Software Development Manager', 'Lead a team of engineers building AWS services. Technical leadership, mentoring, and strategic planning. 8+ years of experience required.', 8000, 14000, 8, 0, 0, 1, NOW(), NOW()),
(3, 'Solutions Architect', 'Help enterprise customers design and implement AWS solutions. Technical consulting, architecture reviews, and best practices guidance.', 6000, 10000, 5, 0, 0, 1, NOW(), NOW());

-- =============================================
-- SoftServe (company_id = 6)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(6, 'Java Developer', 'Join our enterprise team building solutions for Fortune 500 clients. Spring Boot, microservices, and cloud experience preferred. Remote-friendly position.', 3000, 5500, 2, 0, 0, 1, NOW(), NOW()),
(6, 'React Frontend Developer', 'Build modern web applications for healthcare clients. React, TypeScript, and state management experience required. Agile team environment.', 2500, 4500, 2, 0, 0, 1, NOW(), NOW()),
(6, 'Python Developer - AI/ML', 'Work on AI-powered solutions for automotive industry. Python, TensorFlow, computer vision experience. Exciting projects with global impact.', 3500, 6000, 3, 0, 1, 1, NOW(), NOW()),
(6, 'Manual QA Engineer', 'Ensure quality of web and mobile applications. Test planning, execution, and bug tracking. Great opportunity to grow into automation.', 1500, 2500, 1, 0, 3, 1, NOW(), NOW()),
(6, 'DevOps Engineer', 'Implement CI/CD pipelines and cloud infrastructure. AWS, Kubernetes, Terraform experience. Join a growing DevOps practice.', 3500, 5500, 3, 0, 2, 1, NOW(), NOW()),
(6, 'UI/UX Designer', 'Design intuitive interfaces for enterprise applications. Figma, user research, and design systems experience. Collaborative design team.', 2500, 4000, 2, 0, 5, 1, NOW(), NOW());

-- =============================================
-- EPAM Ukraine (company_id = 7)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(7, 'Senior .NET Developer', 'Enterprise solutions for banking sector. C#, .NET Core, Azure. Work on high-load financial systems with strict security requirements.', 4000, 6500, 4, 0, 0, 1, NOW(), NOW()),
(7, 'Salesforce Developer', 'Customize and extend Salesforce platform for enterprise clients. Apex, Lightning, integrations. Salesforce certification is a plus.', 3500, 6000, 2, 0, 0, 1, NOW(), NOW()),
(7, 'Data Scientist', 'Build predictive models for retail analytics. Python, SQL, machine learning. Work with large datasets and deliver business insights.', 4000, 7000, 3, 0, 1, 1, NOW(), NOW()),
(7, 'Automation QA Engineer', 'Design and implement test automation frameworks. Selenium, Java, API testing. Lead automation initiatives for major projects.', 3000, 5000, 3, 0, 3, 1, NOW(), NOW()),
(7, 'Technical Project Manager', 'Lead delivery of complex software projects. Agile methodologies, stakeholder management, technical background required.', 4000, 6500, 5, 0, 4, 1, NOW(), NOW());

-- =============================================
-- Grammarly (company_id = 11)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(11, 'NLP Engineer', 'Work on core language processing algorithms. Deep learning, transformers, linguistics. Help millions write better every day.', 6000, 10000, 4, 0, 1, 1, NOW(), NOW()),
(11, 'Senior Backend Engineer - Go', 'Build high-performance services handling billions of requests. Go, distributed systems, performance optimization. Scale matters here.', 5500, 9000, 4, 0, 0, 1, NOW(), NOW()),
(11, 'iOS Engineer', 'Develop Grammarly keyboard and editor for iOS. Swift, excellent UX sensibility. Work on app used by millions daily.', 5000, 8000, 3, 0, 0, 1, NOW(), NOW()),
(11, 'Product Designer', 'Design intuitive writing experiences across platforms. User research, prototyping, design systems. Shape how people communicate.', 4500, 7500, 3, 0, 5, 1, NOW(), NOW()),
(11, 'Engineering Manager', 'Lead a team of talented engineers. Technical mentorship, project delivery, team growth. Help build world-class engineering culture.', 7000, 11000, 6, 0, 0, 1, NOW(), NOW());

-- =============================================
-- Monobank (company_id = 16)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(16, 'iOS Developer', 'Build Ukraine''s favorite banking app. Swift, excellent UX, fintech experience. Join the team revolutionizing mobile banking.', 4000, 7000, 3, 0, 0, 1, NOW(), NOW()),
(16, 'Android Developer', 'Develop Monobank Android app loved by millions. Kotlin, Material Design, performance optimization. Fintech innovation.', 4000, 7000, 3, 0, 0, 1, NOW(), NOW()),
(16, 'Backend Developer - Scala', 'Build core banking infrastructure. Scala, functional programming, high-load systems. Process millions of transactions daily.', 4500, 7500, 4, 0, 0, 1, NOW(), NOW()),
(16, 'Security Engineer', 'Protect one of Ukraine''s largest banks. Penetration testing, security architecture, incident response. Critical role.', 5000, 8000, 4, 0, 2, 1, NOW(), NOW()),
(16, 'Product Analyst', 'Analyze user behavior and product metrics. SQL, analytics tools, A/B testing. Data-driven product decisions.', 2500, 4500, 2, 0, 1, 1, NOW(), NOW());

-- =============================================
-- GSC Game World (company_id = 23)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(23, 'Unreal Engine Developer', 'Work on S.T.A.L.K.E.R. 2. C++, Unreal Engine, game systems. Join legendary Ukrainian game studio.', 3500, 6500, 3, 0, 0, 1, NOW(), NOW()),
(23, 'Game Designer', 'Design gameplay systems and mechanics. Game design documentation, balancing, player experience. AAA game development.', 2500, 4500, 2, 0, 5, 1, NOW(), NOW()),
(23, '3D Artist', 'Create stunning environments and assets. 3ds Max, Substance, PBR workflows. Work on next-gen game graphics.', 2000, 4000, 2, 0, 5, 1, NOW(), NOW()),
(23, 'QA Tester', 'Test game builds and report issues. Gaming passion required, attention to detail. Be first to play new content.', 1000, 2000, 0, 0, 3, 1, NOW(), NOW()),
(23, 'Technical Artist', 'Bridge between art and engineering. Shaders, tools, performance optimization. Unique technical-creative role.', 3000, 5000, 3, 0, 5, 1, NOW(), NOW());

-- =============================================
-- MacPaw (company_id = 26)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(26, 'macOS Developer', 'Build premium Mac applications. Swift, AppKit, system programming. Work on CleanMyMac and Setapp.', 4000, 7000, 3, 0, 0, 1, NOW(), NOW()),
(26, 'Product Marketing Manager', 'Launch and promote Mac software products. Marketing strategy, campaigns, analytics. Tech-savvy marketing role.', 3000, 5000, 3, 0, 6, 1, NOW(), NOW()),
(26, 'Customer Support Specialist', 'Help users get the most from MacPaw products. Technical troubleshooting, customer communication. English fluency required.', 1500, 2500, 1, 0, 11, 1, NOW(), NOW()),
(26, 'QA Engineer - macOS', 'Test Mac applications across OS versions. Manual and automation testing, regression, performance. Mac ecosystem expertise.', 2500, 4000, 2, 0, 3, 1, NOW(), NOW());

-- =============================================
-- Ciklum (company_id = 31)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(31, 'Full Stack JavaScript Developer', 'Build web applications for European clients. Node.js, React, TypeScript. Agile teams, interesting projects.', 3000, 5000, 2, 0, 0, 1, NOW(), NOW()),
(31, 'PHP Developer - Laravel', 'Develop and maintain e-commerce platforms. Laravel, MySQL, REST APIs. Long-term client projects.', 2500, 4500, 2, 0, 0, 1, NOW(), NOW()),
(31, 'Business Analyst', 'Gather requirements and translate to technical specifications. Client communication, documentation, Agile. Bridge business and tech.', 2500, 4000, 2, 0, 4, 1, NOW(), NOW()),
(31, 'HR Business Partner', 'Support engineering teams with HR needs. Employee relations, performance management, culture. Tech HR experience preferred.', 2000, 3500, 3, 0, 8, 1, NOW(), NOW());

-- =============================================
-- GitLab (company_id = 12)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(12, 'Senior Backend Engineer - Ruby', 'Build GitLab''s core platform. Ruby, Rails, PostgreSQL. 100% remote, async-first culture. Open source impact.', 6000, 10000, 5, 0, 0, 1, NOW(), NOW()),
(12, 'Frontend Engineer - Vue.js', 'Develop GitLab''s web interface. Vue.js, JavaScript, accessibility. Remote work, global team collaboration.', 5000, 8500, 3, 0, 0, 1, NOW(), NOW()),
(12, 'Site Reliability Engineer', 'Keep GitLab.com running for millions of users. Kubernetes, observability, incident management. High-impact infrastructure role.', 6000, 9500, 4, 0, 2, 1, NOW(), NOW()),
(12, 'Technical Writer', 'Create world-class documentation. Technical writing, developer experience, open source. Docs-as-code approach.', 3500, 5500, 2, 0, 11, 1, NOW(), NOW());

-- =============================================
-- Intellias (company_id = 10)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(10, 'Embedded C++ Developer', 'Automotive software development. C++, AUTOSAR, real-time systems. Work with leading car manufacturers.', 4000, 7000, 4, 0, 0, 1, NOW(), NOW()),
(10, 'Golang Developer', 'Build fintech platform backend. Go, microservices, high-load. Green field project opportunity.', 4000, 6500, 3, 0, 0, 1, NOW(), NOW()),
(10, 'Scrum Master', 'Facilitate agile teams and remove impediments. Scrum certification, coaching, continuous improvement.', 2500, 4000, 2, 0, 4, 1, NOW(), NOW()),
(10, 'Middle Python Developer', 'Location-based services development. Python, Django, GIS. Maps and navigation projects.', 2500, 4500, 2, 0, 0, 1, NOW(), NOW());

-- =============================================
-- Rozetka (company_id = 18)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(18, 'PHP Developer', 'Build Ukraine''s largest e-commerce platform. PHP, high-load, millions of users. Impactful work.', 2500, 4500, 2, 0, 0, 1, NOW(), NOW()),
(18, 'Data Analyst', 'Analyze e-commerce metrics and trends. SQL, Python, BI tools. Drive business decisions with data.', 2000, 3500, 1, 0, 1, 1, NOW(), NOW()),
(18, 'SEO Specialist', 'Optimize marketplace for search engines. Technical SEO, content strategy, analytics. High-traffic website.', 1500, 3000, 2, 0, 6, 1, NOW(), NOW()),
(18, 'Logistics System Analyst', 'Optimize warehouse and delivery operations. Process analysis, system requirements, operations. Tech meets logistics.', 2000, 3500, 2, 0, 10, 1, NOW(), NOW());

-- =============================================
-- Preply (company_id = 14)
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(14, 'Senior Python Developer', 'Build language learning marketplace. Python, Django, microservices. EdTech with global scale.', 5000, 8000, 4, 0, 0, 1, NOW(), NOW()),
(14, 'React Native Developer', 'Develop cross-platform mobile app. React Native, TypeScript, mobile UX. Millions of learners worldwide.', 4000, 6500, 3, 0, 0, 1, NOW(), NOW()),
(14, 'Growth Marketing Manager', 'Drive user acquisition and engagement. Performance marketing, analytics, experimentation. Scale EdTech startup.', 3500, 6000, 3, 0, 6, 1, NOW(), NOW()),
(14, 'Finance Manager', 'Manage financial operations and reporting. Financial planning, budgeting, analysis. Tech company finance.', 3000, 5000, 4, 0, 9, 1, NOW(), NOW());

-- =============================================
-- Some PAUSED and CLOSED vacancies for variety
-- =============================================
INSERT INTO vacancies (company_id, title, description, salary_min, salary_max, min_experience_years, employment_type, category, status, created_at, updated_at) VALUES
(1, 'Junior Developer Program', 'Entry-level program for recent graduates. Training, mentorship, rotation. Applications closed for this cohort.', 2000, 3000, 0, 0, 0, 3, NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days'),
(6, 'Contract React Developer', 'Short-term project engagement. React expertise needed. Position currently on hold.', 4000, 5000, 3, 2, 0, 2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '2 days'),
(11, 'Summer Internship - Engineering', 'Summer 2025 internship program. Applications opening soon.', 2500, 3500, 0, 4, 0, 0, NOW(), NOW()),
(16, 'Part-time Support Agent', 'Weekend support coverage. Customer service experience. Currently fully staffed.', 1000, 1500, 0, 1, 11, 3, NOW() - INTERVAL '60 days', NOW() - INTERVAL '45 days');
