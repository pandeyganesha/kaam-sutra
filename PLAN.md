# Roadmap to create this project

I don't have any SRS or strict image of how the project would look like. Hence, I am creating small tasks here and will make incremental changes.

Task 1:

On home page, let us first add an `+` button on bottom right side which creates a card with two fields i.e., Task name and Money.
These tasks will be daily tasks, that we need to do everyday. It will have the option to check or uncheck the task.

On top of the screen, there will be total screen which gets updated as we check/uncheck tasks.

## Sub steps to get above home page screen

- Add total money card on the top of the screen.
- Add button on bottom right side.
- On button press show a card with two fields with edit text
- On save, create a task with option to check or uncheck the task.
- Update the Net Worth when tasks/checked on unchecked.
- Persist the net worth in local storage
- After every new day, reset all the tasks to unchecked state and mark each task as not done and check/uncheck would again add/subtract the net worth.


Things I want to track:

- For recurring tasks ( daily/weekly/monthly) we earn points if done else gets deducted from our net worth.
- For streak, we give extra points.
- There can be two types of Goals, time bounded and unbounded. Or we can say we only have time bounded goals, and by setting "Indefinite" as time we can make a task unbounded, hence no penalty applies on it.
- But time bounded goals, if we do not achieve it, it's value starts decaying every week/day (have not thought which to choose yet) until it reaches a bare minimum and that point it is converted to unbounded goal and with still some value.

---
As some computer scientist said ( Forgot the name) that we must first design the Data Structure to support our problem and then only proceed ahead.
So the time has come when I need to think how to handle all this data.

Questions I am facing:
- Should Tasks and goals be two different entities or same? A task goal is task with no recurrence?
- Should time bounded and unbounded goals be two different things or as I have thought that unbounded goals are time bounded goals with indefinite time period?

Let us start with some example and try to handle the data for that.

1. Simple daily task. It needs to have a `name`, `pointsDelta`, `status`
2. Weekly task. It is same like daily task, just the recurrence is set to week.

Right now I am feeling to intuition to make everything a task and see how it goes. Hence by observing above two tasks I can add one more attribute to a task, that is recurrence.

3. Monthly task. It can again be handled with `recurrence`.
4. Goals. These are just like tasks, without `recurrence` but have time limit. Which means we we want to fit it in the definition of `task` we need to track if it is recurring in nature or deadline based.

After observing `Goals`, we have two path. One is to treat it differently or to treat it as `task` and hence we need to label it with `isRecurring` or `isDeadlineBased` option.
So either we simply have different class to represent each or to make them fall under same category and then handle the logic to make the data consistent, like can not choose `recurring` and `isDeadlineBased` options together.

And after analyzing both things, I feel like keeping goals as different entity seems better option. Why?
- Because first of all we won't have to handle the logic to avoid conflicting states.
- Second and then we can keep another categories to simply work on goals as they are different from tasks.
- In our life we treat both as different things. Tasks seems to be the procedure and Goals seems to be the result.
- We set tasks according to goal, hence each should be treated differently.

Conclusion. We have Goals as separate category.

Earlier I was not sure if I should create it as todo app or what, but now the image is getting clearer. I want to build habit tracker. It is just an application that I want to build for myself ( and can be used by others as well ) to help me in doing things that improves me as a person with good habits and metrics. No one is stopping me/you from adding a simple TODO task in it and get points or by claiming 1000 points for no reason.

Within goals, I have two options, time bounded goals or unbounded goals. Let me first think of an example for each so that it is easier to picture the difference.
I want to learn handstand this year before my birthday. I want to give it to myself as my birthday gift. Before 25 November 2026, I want to learn handstand. Where I am able to perform handstand for 30 seconds. This is time bounded goal.
I need to add time boundation for the things I care. Hence after that day, I must not get same amount of points. Points must decay. Like if I learn handstand before my birthday, I get 100 points, but after every week it is starts decaying at the rate of 1% per week ( I am not sure the exact number to use. Topic for later )
And if I don't do it suppose like for another 2 years, it still would be something I learned, just did not do it within the time, hence will still get some minimum points like 30.
So, after a point a time bounded goal can become same goal.

Example for unbounded goal is to run 5km in 30 min. This is something I would want to do, but I am fine If I don't do it. Hence, I will just set points to it, without time limit.
Thinking of it, I realized that I might also want to upgrade unbounded goal to to time bounded. For these I have two options, either to delete existing goal and create new with time bound or treat both of them as same in our code, hence we can just give option to use to just upgrade it as same goal becomes time bounded. Still not sure if to treat both of them different or same.

I think for now I can skip this point, because I will first implement the task logic in our code and will add support for Goals later. Since I know I will be treating both of them differently, I can work on them independently ( Another perk of keeping them separate )

---

### Next confusion: how do "done/not done" and "reset every day" actually work?
My rough plan said: after every new day, reset all tasks to unchecked, and re-enable
checking/unchecking to again add/subtract net worth. But the moment I said this out loud, it felt
wrong. A "10,000 steps" done today is not the same event as "10,000 steps" done tomorrow — they're
different occurrences of the same task. I don't want to *uncheck* anything each day — I want a
fresh checklist to exist naturally, with every task starting unchecked by default, without any
explicit reset step.

That led me to a bigger realization: **changing the task list itself** (add/edit/delete a task)
and **changing the state of a task on a given day** (done, not done, points earned) are two
completely different kinds of operations, and I was conflating them into one entity.

I reached for an analogy to explain this to myself: Docker images vs containers. The image
(task definition) is the template — rarely changes. The container (a specific day's occurrence)
is created *from* that image, and is temporary/instance-specific. I should never delete anything
from the "database" outright — I should only ever append new records, like event-driven
architecture — so that even if I remove a task from my active list later, the history of it being
done on specific past days still exists, untouched.

### Landing on the shape
This gave me two entities instead of one:
- **`TaskDefinition`** — the reusable template: `name`, `pointsDelta`, `recurrence`. Freely
  editable/deletable, since it's just configuration.
- **`TaskLog`** — one record per `(taskId, date)`: whether it was done, and the points awarded
  (snapshotted, not recalculated later from the definition — so editing a task's points later
  doesn't rewrite past history).

"Is this task done today?" becomes something I **derive** by checking whether a `TaskLog` exists
for today — not something I store as a flag and manually reset. If no log exists yet for today,
it's simply unchecked, because that's the honest default — not because I actively unchecked it.
Net worth becomes a sum computed from today's logs, not a separately stored number that could
drift out of sync.

### One more open question: what about mistakes — checking, unchecking, re-checking the same day?
If I mis-tap a checkbox by accident, then uncheck it, then check it again later once actually
done — should I record all of that as separate append-only events, or just keep one entry per
day and overwrite it?

Strict event sourcing says: append every toggle, never overwrite, keep the full trail. That's
appealing for consistency, and would let me analyze things like "how often do I fumble this
checkbox," but honestly, that's noise I don't care about — I only care what the final state was
by the end of the day.

So I decided on a middle ground: **within a single day, the log entry can be overwritten freely**
(mistakes get corrected in place) — but **once a day has passed, that entry is frozen forever**,
same as full event sourcing. This keeps the one guarantee that actually matters — past days never
silently change when I edit today's settings or task list — while avoiding pointless storage
noise from accidental taps.

### Where this leaves me
- Tasks and Goals: separate entities, Goals deferred until Task logic is proven out.
- Task data: split into `TaskDefinition` (template, editable) and `TaskLog` (per-day record,
  mutable only within the current day, immutable afterward).
- Nothing is "reset" — everything is derived by querying the log against the current date.
- Net worth, "is it done," and (later) streaks are all computed from logs, never stored as
  separate fields that could drift out of sync.

---
30 July 2026 | 08:43
On my 3rd or 4th of this project. I have decided to use AI as little as possible for my personal projects.
It leaves me even dumber and more dependent on it. It is guiding the thought process.
I first created this project a few days ago and without even writing a single line of code. Claude created and published it all within 4-5 hours. But I learnt nothing. Moreover I did not feel the sense of achievement at all. The project did not feel personal, it did not feel "Mine".

Hence I deleted the repo and started fresh once again with a new Rule:
> Don't let claude write even a single line for you. You can ask it, but you will have to manually make each change.

And this rules actually helped. After 3 days, I have made very little progress when compared to the last shipped project. But I have learnt more and I know my project.

But it is still sublty guiding my thought process, which I don't want. I want to be confused and then think of a solution instead of sharing my process with it and it then serving the answers directly.
If I don't understand something it is saying and accept to do, then starts the trap. Then for each next step I become more dependent on it.
It again sucks me in.

Hence new rule.
> Only ask conceptual question without giving it the context to my project. But don't ask questions which solves my actual project issues. Instead go out and search google. Like we used to do.

Let us see how it goes.

---
31 July 2026 | 22:13

Implented Rooms ( new concept I have learnt ( not quite while, but was able to work with it )) today.
We create Entity which represents table, we create Dao, which shows interface to the table. And have an AppProvider that actually handles the database. ( How this all works, I don't know, but it works )

---

More features that I feel I need for myself
- It should also host my to do task, as I want all of the things I want to do in the same location.
- Tasks can hold description, because I felt like adding description to my tasks. Like, for the handstand Goal. I want to write notes, about the process and all.
- While writing above point, I felt, Goals can have sub-goals as well. Like chest-to-wall handstand for 1 min can be a checkpoint in learning handstand.
- Can also add option to attach photos to the goals/tasks.
- Need to set notification as well.
- Need to calculate correct `worth` for each task and goal. Can't just give random worth values as I suit. They must be logical and must represent my goals and importance.
- One another feature I can think of is to visualize my progress ( other than charts ) like github/leetcode green profile. It looks cool.

---

6 Aug 2026 | 8:56

I hit a bug today.
When I pressed check button on a task, we insert a row into the database with `done=1` and `pointsAwarded=5` ( 5 is just for example ). If we uncheck it, we mark it `done=0` but do not update the `pointsAwarded`, which still claims to have awarded 5 points for a task we have not done.
We could easily mitigate this bug by always checking `done` with `pointsAwarded`. But the question is, is this right approach?
These two values are logically tied, and must always be in sync to represent the truth. But we have not enforced that.
And if we always have to adjust each of them together, I made me think, are both of the col truly needed? if yes, then we enforce the sync, if not, we get rid of the one.
If we make `pointsAwarded` signed integer, then if it is greater than 0 -> Task is done, if less than 0 -> task is not done.
But then we had the edge case of worth being zero. But what kind of tasks can have 0 value? It made no sense to me. `worth=0` means it bears no significance in your life, and if so, there is no point of tracking it everyday. Hence I decided to enforce a rule that a task can never have 0 worth.

Hence we get rid of `done` col as now we can compress this value in `pointsAwarded` only.
I will make think like calculating `netWorth` quite easy. Just sum over the rows for `pointsAwarded` and it must represent the true value.

---

I have two things in my mind to proceed with
1. To add history option
2. To add goals section

History is lookup on the existing data and goals are something that will generate data.
Goals are the part of core functionality of the application. Hence I think I must go for goals first. The principle here followed is, "Lay the foundation first, then the building".

---

Now the questions arises, how to show goals. One the same page with Tasks in one part and Goals in another.
Or have a always on top horizontal nav bar at the bottom ( or somewhere ) and have different sections there.
I don't want collapsable sidebar for sure, that feel to many features, and I want to keep it minimal.

We can use `+` as single source to add task or goal. But how to show goals is still a question.

For now, to me, a nav var on the bottom feels better. That nav var and net worth remains on the top of all screens ( two screens for now ), and tasks list and goals list change.

One idea I got is this, we create three screens, home screen, tasks screen and goals screen instead of keep net worth on top for each screen.
On home screen we will only have net worth for now, but can add insights and other actionable items. And tasks will keep tasks, and goals will have goals.

This seems nice design choice ( Just like popular application, nothing new ). Let us proceed with this. Later we can also add profile section in that nav var as well.

---
16 Aug 2026 | 13:36

Resuming this project after a break as I was not able to decide how should I treat Goals and tasks.
Hence the final conclusion I reached is to satisfy my current needs however possible, and then later look for UI or backend optimzation or refactoring instead trying to know everything beforehand.

Some others things I need to do now are:
- Rename Tasks to Habit as they are not task but habit that we want to track.
- Then we will add logic to add goals
- And I also need a page to add my to-do goals, like "Read Article on Free Will" which is neither habit nor goal.
- Also since I have all the history, instead of creating a nice UI to show it, let me first just dump the history on some page and later figure out how to present it.
- And for sure I should start adding apk to my site, so that I can let others test my app without having to build it themselves.

---
17 Aug 2026 | 8:50

I was starting to implement TODO page, but for that I now needed to freeze input output structure, to create table and show Dialog Box on UI.
I was wondering if I could reuse existing Dialog Boxes just without `worthDelta`. Then i would need to pass var handle this. And also if I could incorporate todo tasks in existing table with `worthDelta=0`  but existing one is called `Habit` and I really like to follow the meaning of the words. So `Habit` should never store `todo` because todo is not habit.
But both of them share most of the cols, which means I can store them at one place and name it something else like `tracker` ( still haven'e found a good name to encapsulate Goals, todo and habits with one name ).
The common table will share all of the common ingredients and anything special would have its own table and we would have a foreign key for that in our main table.
I think I can use `Task` as the common name as now I have not named anything else task and Task kind of encapsulates each idea.
Habit is a reccuring task.
ToDo is a task to be done once.
Goal is also a task to be done once, it just carries different emotion. And since our just not track that emotion, by definition, TODO and Goals can be same. But we would stlil treat them as if they were different.
Being able to keep them separate is also one of the ideas of this application.

But now I am also reconsidering my idea of `NetWorth`. This net worth idea was to give some dopamine hit to the user. The problem is that it has no significance of itself. So we need to give it some meaning. For example we can assign 1000 worth to a task or 10, it doesn't make a difference. Because the user is itself giving the value. Infinite supply, no demand.
He doesn't care how much worth he/she has collected.
To give it meaning, either we will have to control that or user must be well educated to assign nice relative worth to each task and then also aim for getting higher number hence He gets meaning.
But latter is definitely not a good idea. Humans have always struggled to self regulate, that is why they will be using this app as well.
But if we assign values to task, then there is still another problem, It would still me meaningless ( in a way ). Some users might like to have higher score, but most would still not care as I does not convert to anything. Doing `workout` everyday converts to his health, but the numbre we are assigning to it has no direct relationship with it.

So Again, either we will have to do a nice study and assign value to a wide range of habits which directly corresponds to something real. But it seems to be very challenging, especially in this initial phase of this app. Or we can have arbitrary worth but standard for different type of tasks so that people can compare each other. And people really like comparing with each other. People do all sorts of things for "Number of Likes". But this is also not an good idea for two reasons.
1. We don't have so many users to start comparing. We need a nice userbase before we starting seeing effects of this.
2. This is more important as per my own philosophy that It is not the right way even when we have more users. Because then people start comparing themselves with others without considering all the variables and may also start to optimize for the number instead of his own well being, which is the purpose of this project. We just want to give some kind of psycological reward to the user when he completes something, that's it.

For me, one of such rewards is Github's green graph. I really get that hit everyday when I see another day with green color. So I might proceed with such approaches. It should just feel good to see a graph or some quanity of things in form of some stats, which user can see and feel better that how much progress they have made. This is the better way than to comparing yourself with others.
Hence I now decide to drop this idea of `worthDelta` and would think of something else later.