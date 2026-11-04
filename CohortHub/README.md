# CohortHub

A simple Java console app for managing learners, study groups, and mentor support.

This version keeps to the core requirements from the Java Weekend Project Challenge and avoids extra stretch features so it stays clear and beginner-friendly.

## Required features

- Register learners with a unique ID, name, and email.
- Find, update, and remove registered learners.
- Reject duplicate learner IDs and duplicate email addresses (ignoring case).
- Display learners sorted by name, then by ID when names match.
- Create study groups.
- Add learners to groups without duplicate membership.
- Show a learner's groups.
- Add learners to a mentor-support queue in first-in, first-out order.
- Attend to the next person in the queue.
- Display totals such as total learners, total groups, membership count per group, and number waiting for support.
- Handle invalid or unknown inputs with helpful messages.

## Collection choices used

- Map: lookup learners by unique ID.
- Set: maintain unique membership in each group.
- Queue: store mentor-support requests in arrival order.
- List: display sorted learner records and report output.

## Acceptance checks covered

- Duplicate learner IDs are rejected.
- Duplicate emails are rejected ignoring case.
- The same learner cannot be added to the same group twice.
- Support requests are handled in arrival order.
- Removing a learner removes their group memberships and waiting request.
- Learners are sorted by name, then ID, without breaking lookup by ID.

## Notes

- This version is intentionally simpler than a full production system.
- Persistence is not required for this challenge.
- The optional stretch comparison feature between two groups was left out to stay aligned with the core PDF requirements.
