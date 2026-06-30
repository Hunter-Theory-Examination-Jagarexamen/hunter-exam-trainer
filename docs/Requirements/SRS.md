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
* Full exam simulation based on the official examination format

The system will provide immediate feedback after each quiz, allowing users to review their answers and improve their understanding of the subject matter. User performance will be stored to generate statistics and monitor learning progress over time.

An administration interface will allow instructors or course leaders to manage the question bank and review participant performance.

The application will be developed as a **Progressive Web Application (PWA)**, enabling access from desktop computers, tablets, and mobile devices using a single codebase.

## 1.3 Project Goals

The project aims to develop a responsive Progressive Web Application that supports students preparing for the Swedish Hunter Examination by providing interactive practice quizzes, realistic exam simulations, performance tracking, and an administration interface for managing educational content.

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

Unlike printed study materials, the application offers immediate feedback, performance tracking, and realistic exam simulations, helping learners identify strengths and areas for improvement before taking the official examination.

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
- Full exam simulation
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