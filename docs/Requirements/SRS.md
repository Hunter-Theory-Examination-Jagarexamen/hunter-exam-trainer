# Software Requirements Specification

| Field                | Value               |
|----------------------|---------------------|
| **Project Name**     | Hunter Exam Trainer |
| **Document Version** | 1.0                 |
| **Prepared By**      | Sangeetha           |
| **Reviewed By**      | Paul and Gregory    |
| **Approved By**      | Klas Åkerskog       |
| **Date**             | 29 June 2026        |
| **Status**           | Draft               |     

---
# Revision History

| Version | Date         | Author    | Description     |
|---------|--------------|-----------|-----------------|
| 1.0     | 29 June 2026 | Sangeetha | Initial version |

---

# Table of Contents

1. Introduction
2. System Overview
3. Functional Requirements
4. Non-functional Requirements
5. User Interface Requirements
6. Acceptance Criteria
7. Future Enhancements

---

# 1 Introduction

## 1.1 Purpose

The purpose of this Software Requirements Specification (SRS) is to define the functional and non-functional requirements for the Hunter Exam Trainer, a Progressive Web Application (PWA) designed to help learners prepare for the Swedish Hunter Examination (Jägarexamen) theoretical knowledge test.

This document serves as the primary reference for the development team, project coordinator, and other stakeholders throughout the project lifecycle. It establishes a common understanding of the project's objectives, system functionality, user requirements, and project scope before development begins.

The SRS also provides a foundation for system design, implementation, testing, and future maintenance of the application.

## 1.2 Project Scope

The Hunter Exam Trainer is a web-based learning application that enables users to practice and prepare for the theoretical portion of the Swedish Hunter Examination.

The application will provide multiple learning modes to accommodate different study preferences, including:

* Practice questions by subject area
* Randomized practice sessions
* Full exam simulation (Mock Exam) based on the official examination format

The system will provide immediate feedback after each quiz, allowing users to review their answers and improve their understanding of the subject matter. User performance will be stored to generate statistics and monitor learning progress over time.

An administration interface will allow instructors or course leaders to manage the question bank and review participant performance.

The application will be developed as a **Progressive Web Application (PWA)**, enabling access from desktop computers, tablets, and mobile devices using a single codebase.

## 1.3 Project Goals

The project aims to develop a responsive Progressive Web Application that supports students preparing for the Swedish Hunter Examination by providing interactive practice quizzes, realistic exam simulations (mock exams), performance tracking, and an administration interface for managing educational content.

## 1.4 Objectives

The primary objectives are:

- Provide an easy-to-use digital platform for preparing for the Swedish Hunter Examination.
- Support all eleven theoretical subject areas included in the examination.
- Improve learning through immediate feedback and performance tracking.
- Simulate the official examination experience.
- Enable instructors to manage questions and monitor learner progress.
- Deliver a responsive application that works across desktop and mobile devices.

## 1.5 Definitions

| Term     | Description                         |
|----------|-------------------------------------|
| PWA      | Progressive Web Application         |
| REST API | Representational State Transfer API |
| JWT      | JSON Web Token                      |
| MVP      | Minimum Viable Product              |

## 1.6 References

The following documents were used while preparing this Software Requirements Specification:

- Internship Project Description
- Hunter Examination course material provided by the project coordinator
- System architecture sketch

---

# 2 System Overview

## 2.1 Product Overview

The application is a web-based educational application developed to support students preparing for the Swedish Hunter Examination. It complements traditional classroom instruction by providing an interactive platform where learners can practice theoretical questions at their own pace.

Unlike printed study materials, the application offers immediate feedback, performance tracking, and realistic exam simulations (mock exams), helping learners identify strengths and areas for improvement before taking the official examination.

The application follows a client-server architecture, where the frontend communicates with the backend through a REST API. The backend manages user authentication, retrieves questions from the database, evaluates submitted answers, calculates scores, and stores user progress.

## 2.2 Intended Users

The application supports two primary user groups.

### Student

Students use the application to prepare for the theoretical hunter examination.

Students can:

- Create an account and log in
- Practice questions by category
- Start randomized practice sessions
- Take simulated examinations
- Review previous quiz attempts
- View learning statistics and progress

### Administrator / Instructor

Administrators manage the educational content and monitor learner performance.

Administrators can:

- Add new questions
- Edit existing questions
- Delete outdated questions
- Organize questions into categories
- Review learner performance
- Monitor overall statistics

## 2.3 Product Features

The application provides the following core features:

- Secure user authentication
- Practice by subject area
- Random practice mode
- Full exam simulation (Mock exam)
- Automatic answer validation
- Performance statistics
- Quiz history
- Responsive user interface
- Progressive Web Application support
- Administration panel for question management

## 2.4 Operating Environment

The application will operate as a Progressive Web Application (PWA) and will be accessible through modern web browsers.

Supported platforms include:

- Desktop computers
- Tablets
- Mobiles

Supported browsers include:

- Google Chrome
- Microsoft Edge
- Mozilla Firefox
- Safari

The backend will expose RESTful APIs over HTTPS, while data will be stored in a relational database.

## 2.5 Assumptions and Dependencies

The development of the application is based on the following assumptions:

- The project coordinator will provide the official question bank.
- Users will have access to a stable internet connection.
- Users will access the system using a modern web browser.
- The application will primarily focus on the theoretical examination.
- The internship period provides sufficient time to complete the planned MVP.

## 2.6 Project Constraints

The development of the Hunter Exam Trainer is subject to the following constraints:

- The project must be completed within a 12-week internship period.
- Development is carried out by a team of three interns.
- The application will be developed as a Progressive Web Application (PWA) instead of separate native mobile applications.
- The official question bank will be provided by the project coordinator.
- The initial release focuses only on the theoretical part of the Swedish Hunter Examination.

---

# 3 Functional Requirements

This chapter describes the functional requirements of the application. These requirements define the core functionalities that the system shall provide to support students preparing for the Swedish Hunter Examination and administrators responsible for managing the application.

Each requirement is assigned a unique identifier to support implementation, testing, and future maintenance.

## 3.1 User Authentication

The system shall provide secure user authentication to allow students and administrators to access features based on their assigned roles.

| ID     | Requirement                                                                                  | Priority |
|--------|----------------------------------------------------------------------------------------------|----------|
| FR-001 | The system shall allow new users to create an account using an email address and password.   | High     |  
| FR-002 | The system shall allow registered users to log in using valid credentials.                   | High     |
| FR-003 | The system shall allow users to log out securely.                                            | High     |
| FR-004 | The system shall prevent unauthorized access to protected pages.                             | High     |
| FR-005 | The system shall distinguish between Student and Administrator roles after successful login. | High     |

## 3.2 Dashboard

After successful authentication, users shall be redirected to a dashboard that provides access to the application's main features.

| ID     | Requirement                                                  | Priority |
|--------|--------------------------------------------------------------|----------|
| FR-006 | The system shall display a dashboard after successful login. | High     |  
| FR-007 | The dashboard shall provide access to all practice modes.    | High     |
| FR-008 | The dashboard shall display the user's recent activity.      | Medium   |
| FR-009 | The dashboard shall provide quick access to user statistics. | Medium   |

## 3.3 Practice Mode (Subject-wise)

The application shall provide flexible practice modes to help users prepare for the theoretical examination.

| ID     | Requirement                                                          | Priority |
|--------|----------------------------------------------------------------------|----------|
| FR-010 | The system shall allow users to practice questions by subject area.  | High     |
| FR-011 | The system shall display one question at a time.                     | High     |
| FR-012 | The system shall allow users to select an answer before proceeding.  | High     |
| FR-013 | The system shall provide immediate feedback after each question.     | High     |
| FR-014 | The system shall explain the correct answer when appropriate.        | Medium   |
| FR-015 | The system shall allow users to restart a practice session.          | Medium   |

## 3.4 Exam Simulation (Mock Exam)

The system shall provide a simulation of the official Swedish Hunter Examination, allowing users to experience the test environment before taking the real examination.

| ID     | Requirement                                                                                    | Priority |
|--------|------------------------------------------------------------------------------------------------|----------|
| FR-016 | The system shall allow users to start a full exam simulation.                                  | High     |
| FR-017 | The system shall randomly select 70 questions for each exam attempt.                           | High     |
| FR-018 | The system shall allow users to navigate between questions before submission.                  | Medium   |
| FR-019 | The system shall allow the user to submit the exam.                                            | High     |
| FR-020 | The system shall calculate the final score after submission.                                   | High     |
| FR-021 | The system shall indicate whether the user has passed or failed the simulated exam.            | High     |
| FR-022 | The system shall display a detailed summary of correct and incorrect answers after completion. | High     |

## 3.5 Results and Statistics

The system shall record user performance and present meaningful statistics to help users monitor their learning progress.

| ID     | Requirement                                                               | Priority |
|--------|---------------------------------------------------------------------------|----------|
| FR-023 | The system shall store the results of completed quizzes and exams.        | High     |
| FR-024 | The system shall display the user's quiz history.                         | Medium   |
| FR-025 | The system shall calculate the user's overall accuracy.                   | High     |
| FR-026 | The system shall display subject-wise performance statistics.             | High     |
| FR-027 | The system shall display the user's progress over time.                   | Medium   |
| FR-028 | The system shall identify the user's strongest and weakest subject areas. | Medium   |
| FR-029 | The system shall allow users to review previously completed quizzes.      | Medium   |

## 3.6 User Profile

The system shall provide each user with a personal profile containing account information and learning progress.

| ID     | Requirement                                                               | Priority |
|--------|---------------------------------------------------------------------------|----------|
| FR-030 | The system shall display the user's profile information.                  | Medium   |
| FR-031 | The system shall allow users to update their personal information.        | Low      |
| FR-032 | The system shall allow users to change their password.                    | Medium   |
| FR-033 | The system shall display the total number of quizzes and exams completed. | Medium   |

## 3.7 Administration Panel

The system shall provide an administration interface that enables instructors to manage the application content and monitor learner performance.

| ID     | Requirement                                                                      | Priority |
|--------|----------------------------------------------------------------------------------|---------|
| FR-034 | The system shall provide a secure administrator login.                           | High    |
| FR-035 | The system shall allow administrators to add new questions.                      | High    |
| FR-036 | The system shall allow administrators to edit existing questions.                | High    |
| FR-037 | The system shall allow administrators to delete questions.                       | High    |
| FR-038 | The system shall allow administrators to assign questions to subject categories. | High    |
| FR-039 | The system shall allow administrators to search and filter questions.            | Medium  |
| FR-040 | The system shall display learner performance statistics.                         | Medium  |
| FR-041 | The system shall display the number of registered users.                         | Low     |
| FR-048 | The system shall allow administrators to view each learner's progress across subjects. | Medium  |


## 3.8 General System Functions

The system shall provide general functionality required to support usability, accessibility, and overall application behavior.

| ID     | Requirement                                                                              | Priority |
|--------|------------------------------------------------------------------------------------------|----------|
| FR-042 | The system shall provide a responsive interface for desktop, tablet, and mobile devices. | High     |
| FR-043 | The system shall display meaningful error messages when an operation fails.              | High     |
| FR-044 | The system shall display confirmation messages after successful actions.                 | Medium   |
| FR-045 | The system shall securely store user data in the database.                               | High     |
| FR-046 | The system shall maintain user sessions until logout or session expiration.              | Medium   |
| FR-047 | The system shall support future expansion without significant architectural changes.     | Low      |

### Summary

The Hunter Exam Trainer includes functional requirements grouped into eight functional areas. These requirements define the expected behavior of the application from both the student and administrator perspectives. Each requirement will serve as a reference during system design, implementation, testing, and project evaluation.

---

# 4. Non-Functional Requirements

This chapter defines the quality attributes and constraints that the Hunter Exam Trainer shall satisfy. These requirements ensure that the application is reliable, secure, responsive, and easy to use across different platforms.

## 4.1 Performance

| ID      | Requirement                                                                           | Priority |
|---------|---------------------------------------------------------------------------------------|----------|
| NFR-001 | The system shall load pages within 3 seconds under normal network conditions.         | High     |
| NFR-002 | The system shall process quiz submissions.                                            | High     |
| NFR-003 | The system shall support concurrent users without noticeable performance degradation. | Medium   |

## 4.2 Security

| ID      | Requirement                                                                    | Priority |
|---------|--------------------------------------------------------------------------------|----------|
| NFR-004 | User passwords shall be securely encrypted before storage.                     | High     |
| NFR-005 | All communication between the client and server shall use HTTPS.               | High     |
| NFR-006 | Only authenticated users shall access protected resources.                     | High     |
| NFR-007 | Administrator functions shall only be accessible to authorized administrators. | High     |

## 4.3 Reliability

| ID      | Requirement                                                          | Priority |
|---------|----------------------------------------------------------------------|----------|
| NFR-008 | The application shall save completed quiz results without data loss. | High     |
| NFR-009 | The application shall recover gracefully from unexpected errors.     | Medium   |
| NFR-010 | System failures shall not corrupt stored user data.                  | High     |

## 4.4 Usability

| ID      | Requirement                                                          | Priority |
|---------|----------------------------------------------------------------------|----------|
| NFR-011 | The application shall provide a simple and intuitive user interface. | High     |
| NFR-012 | Navigation shall remain consistent across all pages.                 | High     |
| NFR-013 | Users shall be able to complete a quiz without prior training.       | Medium   |
| NFR-014 | Important actions shall provide visual feedback to the user.         | Medium   |

## 4.5 Compatibility

| ID      | Requirement                                                                             | Priority |
|---------|-----------------------------------------------------------------------------------------|----------|
| NFR-015 | The application shall support the latest versions of Chrome, Edge, Firefox, and Safari. | High     |
| NFR-016 | The application shall function correctly on desktop, tablet, and mobile devices.        | High     |

## 4.6 Maintainability

| ID      | Requirement                                                          | Priority |
|---------|----------------------------------------------------------------------|----------|
| NFR-017 | The source code shall follow consistent coding standards.            | High     |
| NFR-018 | The project shall use version control through GitHub.                | High     |
| NFR-019 | The application architecture shall support future feature additions. | Medium   |
| NFR-020 | Technical documentation shall be maintained throughout the project.  | High     |

## 4.7 Availability

| ID      | Requirement                                                                         | Priority |
|---------|-------------------------------------------------------------------------------------|----------|
| NFR-021 | The application shall be available whenever the hosting environment is operational. | Medium   |
| NFR-022 | Scheduled maintenance shall be communicated to users in advance when applicable.    | Low      |

### Summary

The non-functional requirements define the expected quality characteristics of the Hunter Exam Trainer. These requirements ensure that the application remains secure, reliable, maintainable, and user-friendly throughout its lifecycle.

---

# 5 User Interface Requirements

## 5.1 Overview

The Hunter Exam Trainer shall provide a responsive and intuitive user interface that supports desktop, tablet, and mobile devices. The interface shall be designed to minimize the learning curve and provide easy access to all major features of the application.

The application shall follow a consistent layout, navigation structure, and visual design across all pages to provide a seamless user experience.

## 5.2 Login Page

### Purpose

The Login page allows registered users to securely access the application.

### Components

- Application logo
- Email field
- Password field
- Login button
- "Create Account" link
- "Forgot Password" link (optional for MVP)

### User Actions

- Enter login credentials
- Login to the application
- Navigate to the registration page

### Navigation

Successful login redirects the user to the Dashboard.

## 5.3 Registration Page

### Purpose

Allows new users to create an account.

### Components

- Full Name
- Email
- Password
- Confirm Password
- Register button

### User Actions

- Enter registration information
- Create a new account
- Return to the Login page

## 5.4 Dashboard

### Purpose

The Dashboard serves as the main entry point after login.

### Components

- Welcome message
- Practice by Subject
- Random Practice
- Exam Simulation
- Statistics
- Profile
- Logout

### User Actions

- Select a practice mode
- View statistics
- Start an exam
- Navigate to profile

## 5.5 Practice Quiz Page

### Purpose

Allows users to practice theoretical questions.

### Components

- Question number
- Subject category
- Question text
- Answer options
- Submit button
- Next Question button
- Progress indicator

### User Actions

- Select an answer
- Submit the answer
- View immediate feedback
- Continue to the next question

## 5.6 Exam Simulation Page

### Purpose

Provides a realistic simulation of the official Swedish Hunter Examination.

### Components

- Question number
- Question text
- Answer options
- Previous button
- Next button
- Submit Exam button

### User Actions

- Answer questions
- Navigate between questions
- Submit the completed examination

## 5.7 Results Page

### Purpose

Displays the user's performance after completing a quiz or examination.

### Components

- Final score
- Pass/Fail status
- Correct answers
- Incorrect answers
- Percentage score
- Review Answers button
- Return to Dashboard button

### User Actions

- Review results
- Return to dashboard
- Review incorrect answers

## 5.8 Statistics Page

### Purpose

Provides users with an overview of their learning progress.

### Components

- Overall accuracy
- Subject-wise performance
- Progress chart
- Quiz history
- Strongest subject
- Weakest subject

### User Actions

- View statistics
- Review previous quizzes

## 5.9 User Profile

### Purpose

Displays user account information.

### Components

- Name
- Email
- Change Password
- Quiz Summary
- Logout

### User Actions

- Update profile
- Change password
- Logout

## 5.10 Administrator Dashboard

### Purpose

Allows administrators to manage questions and monitor learner performance.

### Components

- Question Management
- Add Question
- Edit Question
- Delete Question
- User Statistics
- Search Questions
- Category Management

### User Actions

- Create questions
- Update questions
- Delete questions
- View learner statistics

## 5.11 Responsive Design

The application shall provide a responsive interface that adapts to different screen sizes.

### Desktop

- Sidebar navigation
- Multi-column layout
- Larger content area

### Tablet

- Collapsible sidebar
- Optimized touch controls

### Mobile

- Bottom navigation or hamburger menu
- Single-column layout
- Large touch-friendly buttons

## 5.12 Accessibility

The user interface should follow basic accessibility principles.

- Readable font sizes
- Sufficient color contrast
- Keyboard navigation support
- Clear error messages
- Responsive layouts

### Summary

The user interface requirements define the expected layout and interaction behavior for the Hunter Exam Trainer. The interface is designed to provide a consistent and user-friendly experience across desktop and mobile devices while supporting both students and administrators.

---

# 6. Acceptance Criteria

## 6.1 Overview

The Hunter Exam Trainer shall be considered complete when all mandatory functional and non-functional requirements have been implemented, tested, and accepted by the project coordinator. The following acceptance criteria define the minimum requirements for successful project completion.

## 6.2 Functional Acceptance Criteria

| ID     | Acceptance Criteria                                                  | Status |
|--------|----------------------------------------------------------------------|--------|
| AC-001 | Users can create an account and log in successfully.                 |        |
| AC-002 | Users can practice questions by selecting a subject area.            |        |
| AC-003 | The system provides immediate feedback after each practice question. |        |
| AC-004 | Users can complete a full exam simulation containing 70 questions.   |        |
| AC-006 | The system calculates and displays the final exam score.             |        |
| AC-007 | Users can view their quiz history and statistics.                    |        |
| AC-008 | Administrators can add, edit, and delete questions.                  |        |
| AC-009 | Administrators can organize questions by subject category.           |        |
| AC-010 | The application is fully responsive on desktop and mobile devices.   |        |

## 6.3 Technical Acceptance Criteria

| ID     | Acceptance Criteria                                            | Status |
|--------|----------------------------------------------------------------|--------|
| AC-011 | REST API communication works correctly.                        |        |
| AC-012 | User information is stored securely in the database.           |        |
| AC-013 | Quiz results are stored successfully.                          |        |
| AC-014 | The application is installable as a Progressive Web App (PWA). |        |
| AC-015 | Authentication prevents unauthorized access.                   |        |

## 6.4 Quality Acceptance Criteria

| ID     | Acceptance Criteria                                              | Status |
|--------|------------------------------------------------------------------|--------|
| AC-016 | The application provides a consistent user interface.            |        |
| AC-017 | Navigation is intuitive and easy to use.                         |        |
| AC-018 | Error messages are clear and understandable.                     |        |
| AC-019 | The application performs without major bugs during user testing. |        |
| AC-020 | All mandatory project documentation has been completed.          |        |
| AC-021 | Administrators can view each learner's progress across subjects. | |

## 6.5 Documentation Deliverables

The following documents shall be completed before project delivery:

- Software Requirements Specification (SRS)
- Database Design
- API Documentation
- Test Plan
- Test Report
- User Guide
- Final Project Presentation

## 6.6 Final Approval

The project shall be considered successfully completed when:

- All mandatory acceptance criteria have been satisfied.
- The application has been demonstrated successfully.
- The project coordinator has reviewed the final deliverables.
- All required documentation has been submitted.

---

# 7. Future Enhancements

## 7.1 Overview

The Hunter Exam Trainer is designed with future scalability in mind. Although the current project focuses on delivering the Minimum Viable Product (MVP), several additional features may be implemented in future versions to improve the user experience and extend the application's functionality.

## 7.2 Planned Enhancements

The following features are considered potential improvements for future releases:

- Random Practice Mode with questions selected from all subject areas.
- Offline mode to allow users to practice without an internet connection.
- Question explanations to help users understand the correct answers.
- Bookmark or favorite questions for later review.
- Push notifications to remind users to continue practicing.
- Dark mode for improved accessibility and user preference.
- Multi-language support.

## 7.3 Scalability

The system architecture should support future expansion without requiring major structural changes. Additional features should be implemented using the existing frontend, backend, and database architecture wherever possible.

Future enhancements should follow the same development standards and coding practices established during the initial project.

### Summary

The future enhancements described in this chapter are outside the scope of the current internship project. They represent possible improvements that can be implemented in later versions based on user feedback, project requirements, and available development time.