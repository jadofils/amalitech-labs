# TaskFlow — Raw Source Material (the "messy" starting point)

This is the scattered input the lab's scenario describes: old developer notes, a couple of Slack
message paraphrases, a wiki page nobody's updated in a while, and the actual current API code. It's
something to point the AI at, and something to fact-check its output against. The deliverable is
generated *from this*, not from generic training-data knowledge of "what a task app API usually
looks like."

Everything under "Actual current code" is ground truth. Everything under "Scattered
notes/Slack/wiki" is what the team has lying around describing it — and, deliberately, not all of
it agrees with the code below. Finding those disagreements is the point of the exercise.

---

## Actual current code (ground truth)

```js
// routes/auth.js
router.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  const user = await User.findByEmail(email);
  if (!user || !(await user.checkPassword(password))) {
    return res.status(401).json({ error: 'Invalid email or password', code: 'AUTH_INVALID' });
  }
  const accessToken = signToken(user.id);
  return res.status(200).json({ accessToken, expiresIn: 3600 });
});
```

```js
// routes/tasks.js
router.use(requireAuth); // every route below needs "Authorization: Bearer <accessToken>"

const TASK_STATUSES = ['todo', 'in_progress', 'done'];

router.get('/api/tasks', async (req, res) => {
  const { status, assigneeId } = req.query;
  const tasks = await Task.find({ status, assigneeId, projectId: req.user.projectId });
  return res.status(200).json(tasks);
});

router.post('/api/tasks', async (req, res) => {
  const { title, description, assigneeId, dueDate } = req.body;
  if (!title || title.length > 200) {
    return res.status(400).json({ error: 'title is required and must be <= 200 chars', code: 'VALIDATION' });
  }
  const task = await Task.create({ title, description, assigneeId, dueDate, status: 'todo' });
  return res.status(201).json(task);
});

router.patch('/api/tasks/:id', async (req, res) => {
  const updates = req.body;
  if (updates.status && !TASK_STATUSES.includes(updates.status)) {
    return res.status(400).json({ error: `status must be one of ${TASK_STATUSES.join(', ')}`, code: 'VALIDATION' });
  }
  const task = await Task.findByIdAndUpdate(req.params.id, updates);
  if (!task) return res.status(404).json({ error: 'Task not found', code: 'NOT_FOUND' });
  return res.status(200).json(task);
});

router.delete('/api/tasks/:id', async (req, res) => {
  const deleted = await Task.findByIdAndDelete(req.params.id);
  if (!deleted) return res.status(404).json({ error: 'Task not found', code: 'NOT_FOUND' });
  return res.status(204).send();
});
```

```js
// middleware/rateLimit.js
module.exports = rateLimit({ windowMs: 60_000, max: 100 }); // 100 req/min per API key
// applied globally in app.js — not mentioned in any wiki page or onboarding note
```

---

## Scattered notes / Slack / wiki (what the team believes — not all of it is true)

**#eng-onboarding Slack thread, ~4 months old:**
> "hey the login endpoint just returns a `token`, stick that in your Authorization header and
> you're good. easiest way to test locally is to just hit `/api/tasks` without auth first to see
> the shape of the data, then add the header once you've got a real task to update."

**Wiki page "Task Lifecycle" (last edited 7 months ago):**
> Tasks move through four states: `todo` → `in_progress` → `blocked` → `done`. Use `blocked` when
> a task is waiting on another team.

**Wiki page "API Quick Reference" (last edited 5 months ago):**
> `POST /api/tasks` — title, description, assignee, due date. Returns the created task.
> `DELETE /api/tasks/:id` — removes a task and returns the deleted object so you can undo it
> client-side if needed.

**Dev note in a old PR description:**
> "Added basic rate limiting so we don't get hammered in the demo. Nothing fancy, no docs needed,
> everyone just needs to know it exists if they see a weird error during load testing."
