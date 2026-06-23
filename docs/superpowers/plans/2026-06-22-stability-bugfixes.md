# Stability Bugfixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent invalid approval and email input from causing low-level exceptions, and make optional DataEase synchronization quiet and organization-isolated.

**Architecture:** Validate inputs at the service boundary and translate them to existing business exceptions. Treat absent optional integration configuration as a skipped sync, while the scheduler catches failures per organization and continues processing.

**Tech Stack:** Java, Spring Boot, JUnit 5, Mockito, Maven

---

### Task 1: Boundary validation

**Files:**
- Modify: `backend/crm/src/main/java/cn/cordys/crm/approval/service/ApprovalFlowService.java`
- Modify: `backend/crm/src/main/java/cn/cordys/crm/system/utils/MailSender.java`
- Test: `backend/crm/src/test/java/cn/cordys/crm/system/utils/MailSenderTest.java`

- [ ] Add regression tests for null and non-numeric mail ports.
- [ ] Run the focused tests and confirm the current parsing exception.
- [ ] Validate approval form types and mail ports with `GenericException`.
- [ ] Re-run focused tests.

### Task 2: DataEase scheduled synchronization

**Files:**
- Modify: `backend/crm/src/main/java/cn/cordys/crm/integration/dataease/service/DataEaseSyncService.java`
- Test: `backend/crm/src/test/java/cn/cordys/crm/integration/dataease/service/DataEaseSyncServiceTest.java`

- [ ] Add a regression test where the first organization fails and the second is still invoked.
- [ ] Run the test and confirm fail-fast behavior.
- [ ] Catch failures per organization, count outcomes, and log missing configuration as a warning.
- [ ] Run focused tests and the CRM module test suite.
