# TaskFlow — Developer & User Documentation

Source of truth is [taskflow-source-materials.md](taskflow-source-materials.md)'s "Actual current
code" section, verified line-by-line against the team's own Slack/wiki notes — see
[prompts.md](prompts.md) for how the four discrepancies below were found. Where code and notes
disagreed, this document follows the code.

---

## 1. Getting Started

TaskFlow is a task management API: log in, then create, update, and close out tasks for your
project.

### Logging in

Every request after this needs the token you get back here.

```http
POST /api/auth/login
Content-Type: application/json

{ "email": "you@company.com", "password": "your-password" }
```

- **Success (200):** `{ "accessToken": "...", "expiresIn": 3600 }` — send this token as
  `Authorization: Bearer <accessToken>` on every request below. It expires after 3600 seconds
  (1 hour); log in again to get a new one.
- **Wrong email or password (401):** `{ "error": "Invalid email or password", "code": "AUTH_INVALID" }`

> The field is called `accessToken`, not `token` — an older onboarding note says `token`; that's
> out of date (see Troubleshooting).

### Creating a task

```http
POST /api/tasks
Authorization: Bearer <accessToken>
Content-Type: application/json

{ "title": "Fix login bug", "description": "...", "assigneeId": "u_123", "dueDate": "2026-08-01" }
```

`title` is required (1–200 characters); everything else is optional. A successful call returns
`201` with the created task, which starts in `todo` status.

### Moving a task forward

```http
PATCH /api/tasks/{id}
Authorization: Bearer <accessToken>
Content-Type: application/json

{ "status": "in_progress" }
```

Send only the fields you want to change. Valid `status` values are `todo`, `in_progress`, and
`done` — that's the complete list (see Troubleshooting for why an older wiki page says otherwise).

### Removing a task

```http
DELETE /api/tasks/{id}
Authorization: Bearer <accessToken>
```

Returns `204 No Content` — an empty response body means it worked. There's no undo; if you need
the task's data afterward, `GET` it before you delete it.

### Finding tasks

```http
GET /api/tasks?status=todo&assigneeId=u_123
Authorization: Bearer <accessToken>
```

Both query parameters are optional filters; omit either (or both) to get every task in your
project.

---

## 2. API Reference

### `POST /api/auth/login`

| | |
|---|---|
| Auth required | No |
| Request body | `{ email: string, password: string }` |
| `200` | `{ accessToken: string, expiresIn: number }` — `expiresIn` is seconds |
| `401` | `{ error: string, code: "AUTH_INVALID" }` |

### `GET /api/tasks`

| | |
|---|---|
| Auth required | Yes |
| Query params | `status` (optional, one of `todo`/`in_progress`/`done`), `assigneeId` (optional) |
| `200` | Array of task objects, scoped to the caller's project |

### `POST /api/tasks`

| | |
|---|---|
| Auth required | Yes |
| Request body | `{ title: string (required, ≤200 chars), description?: string, assigneeId?: string, dueDate?: string }` |
| `201` | The created task, `status: "todo"` |
| `400` | `{ error: string, code: "VALIDATION" }` — missing/oversized `title` |

### `PATCH /api/tasks/{id}`

| | |
|---|---|
| Auth required | Yes |
| Request body | Any subset of `{ title, description, status, assigneeId, dueDate }` |
| `200` | The updated task |
| `400` | `{ error: string, code: "VALIDATION" }` — `status` outside `todo`/`in_progress`/`done` |
| `404` | `{ error: "Task not found", code: "NOT_FOUND" }` |

### `DELETE /api/tasks/{id}`

| | |
|---|---|
| Auth required | Yes |
| `204` | Empty body — no deleted-object payload |
| `404` | `{ error: "Task not found", code: "NOT_FOUND" }` |

### Rate limiting

All `/api/*` routes are limited to **100 requests per minute per API key** (`middleware/rateLimit.js`).
Not mentioned in any onboarding material before this document — see Troubleshooting.

---

## 3. Troubleshooting

**"I'm getting a 401 on every request, even though I logged in."**
Check the field name you're reading from the login response — it's `accessToken`, not `token`.
An old `#eng-onboarding` Slack thread says `token`; that's incorrect for the current API. Also
confirm you're sending it as `Authorization: Bearer <accessToken>`, not as a raw value or a
different header.

**"I tried hitting `GET /api/tasks` without logging in first, to see the data shape, and got a 401."**
That's expected — `requireAuth` applies to every route under `/api/tasks`, with no exceptions.
The same onboarding thread suggesting you can skip auth "just to look" is out of date; there's no
unauthenticated path to any task data.

**"I set a task's status to `blocked` and got a 400."**
`blocked` isn't a real status — the current code only recognizes `todo`, `in_progress`, and
`done`. The "Task Lifecycle" wiki page describes a four-state flow including `blocked`; that page
hasn't been updated since the status list was last changed in code. If you need a way to flag a
task as waiting on another team, that's a product gap, not a bug — raise it with the team rather
than trying to set it via the API.

**"I called `DELETE` and expected the deleted task back, but got nothing."**
`DELETE /api/tasks/{id}` returns `204 No Content` with no body by design — the "API Quick
Reference" wiki page claiming it "returns the deleted object" is wrong. If you need the task's
data for an undo feature, `GET` it before deleting.

**"I'm getting 429 errors during load testing."**
There's a global rate limit of 100 requests/minute per API key (see API Reference above). This
was added for demo stability and was never written up anywhere a new developer would see it before
now — if your integration needs a higher limit, that's a conversation to have with whoever owns
the rate-limit config, not something you can raise from the client side.

---

## Known Issues (code vs. documented behavior)

Found by checking every claim in the team's Slack/wiki notes against the actual route code in
[taskflow-source-materials.md](taskflow-source-materials.md), not by assumption:

1. **Login response field name.** Onboarding Slack thread says `token`; the code
   (`routes/auth.js`) returns `accessToken`. Anyone following the Slack thread literally would
   read `undefined` and get confused by every subsequent 401.
2. **No unauthenticated access to `/api/tasks`.** The same Slack thread suggests testing without
   auth first; `requireAuth` is applied to the whole router with no exception, so this has never
   actually worked.
3. **Task status list is out of date on the wiki.** "Task Lifecycle" documents four states
   (including `blocked`); `TASK_STATUSES` in code only has three (`todo`, `in_progress`, `done`).
4. **`DELETE` response body is documented wrong.** "API Quick Reference" says it returns the
   deleted object; the code sends `204 No Content` with an empty body.
5. **Rate limiting exists but was never documented anywhere onboarding-facing** before this
   document — the PR that added it explicitly said "no docs needed," which is how a new developer
   ends up debugging an undocumented 429 during their first load test.
