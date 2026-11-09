# Notification Board - Fullstack Interview Project

## Context

You're building a notification management system - backend and frontend - used by platform
operators to track and resolve service incidents.

An external monitoring system (the alert simulator) is provided and will continuously push
new notifications to your backend while you work.

---

## Getting started

The alert simulator requires Java 21 and Maven. 
It starts an embedded JMS broker on port `61616` that your backend must connect to:

```bash
cd alert-simulator
mvn spring-boot:run
```

The simulator will publish a new notification event every 15-45 seconds.

---

## What to build

### Backend

- An API of your design
- Notifications can be created, listed, and have their status advanced
- Status transitions are one-way: `OPEN → IN_PROGRESS → RESOLVED`
- Advancing to `RESOLVED` requires a resolution note
- Connected clients must receive new and updated notifications without a manual refresh

#### Incoming notification contract

The alert simulator runs an embedded JMS broker and publishes notification events to a queue.

**Broker:** `tcp://localhost:61616`
**Queue:** `alert.notifications`

Messages arrive as JSON text:

```json
{
  "title": "High CPU usage spike detected on eu-west-1",
  "description": "Detected by automated monitoring. Requires operator review.",
  "severity": "HIGH"
}
```

| Field         | Type   | Rules                                             |
|---------------|--------|---------------------------------------------------|
| `title`       | string | Required, non-empty, max 100 characters           |
| `description` | string | Optional, may be omitted or null                  |
| `severity`    | string | Required, one of `LOW` `MEDIUM` `HIGH` `CRITICAL` |

Incoming notifications must be created with status `OPEN`.

Invalid messages must be rejected and routed to the dead letter queue (`DLQ`) - not
silently ignored or stored as-is.

### Frontend

You can keep the UI functional - we're evaluating the full stack, not the design.

- Display notifications - how you handle volume and pagination is your call
- New notifications must appear without a manual refresh
- Allow advancing the status of a notification inline
- When advancing to `RESOLVED`, prompt for a resolution note
- Errors must be handled - how you surface them is your call

---

## Requirements

- Backend: Spring Boot and Java - version is your call
- Frontend: React + TypeScript

---

## Notes

- Persistence is not specified - your call, document your reasoning
- Data does not need to survive a server restart unless you choose otherwise
- CORS is your responsibility

---

## Deliverable

Don't over-engineer it - we value good engineering judgment as much as implementation
quality: the what and the why matter equally.

Your submission should include:

- Instructions on how to run your app
- Tests - what you test and what type of tests you write is your call. We're looking for intentional choices, not coverage metrics
- A document covering the decisions you made and the reasoning behind them. Format and
  length are up to you - what matters is that your thinking is visible. We're not looking
  for a specific answer; we're looking for evidence that you made conscious choices rather
  than default ones.
