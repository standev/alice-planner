# alice-planner

The solution is based on [Critical path method](https://en.wikipedia.org/wiki/Critical_path_method).

# Public API

`GET /api/plan`
Publicly accessible API endpoint at  serving a [JSON document](./src/main/resources/tasks.json) containing:
* The total time to complete the whole project.
* The highest sum of crew members (regardless of crew type) utilized at any given moment of the construction.

`GET /api/task`
Publicly accessible API endpoint  serving a JSON document containing all the tasks from the input with 2 added properties:
startInterval and endInterval representing the starting and ending intervals of given task.
