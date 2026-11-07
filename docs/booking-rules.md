# GH-05 Accommodation Booking Policy

Owner: Pasang Gyeltshen
Sprint: 1 — Requirements and design

## Proposed rules

- A booking covers nights from check-in up to, but excluding, check-out. Check-out must be after check-in. A guest can arrive on the day the previous guest checks out.
- Availability depends on the requested dates, guest count, room capacity, and whether the room is in service.
- A successful request in Pending status holds a specific room. Approved and Checked In bookings continue to block overlapping requests for that room.
- Rejecting or cancelling a request releases its hold. Staff record check-in and check-out against the approved booking.
- If two guests request the same room for overlapping dates, at most one request may obtain the hold. The other guest should receive a clear message and be able to search again.

## Acceptance examples

| Scenario | Expected result |
| --- | --- |
| A pending request holds a room from 10 to 12 November | A request for the same room from 11 to 13 November cannot hold it |
| The next request starts on 12 November | The shared check-out and check-in date does not cause a conflict |
| Staff approve the pending request | The room remains held for those dates |
| Staff reject the request or the guest cancels it | The room can appear in availability again if no other restriction applies |
| Two overlapping requests arrive at the same time | The database permits at most one hold for that room and date range |

Sprint 1 records the policy and acceptance examples. The booking implementation and concurrency checks belong to later sprints. Confirm any property-specific capacity and cancellation details with staff before release.
