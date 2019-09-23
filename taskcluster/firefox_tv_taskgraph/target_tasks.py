from __future__ import absolute_import, print_function, unicode_literals

from taskgraph.target_tasks import _target_task, standard_filter


@_target_task('default')
def target_tasks_default(full_task_graph, parameters, graph_config):
    """Target the tasks which have indicated they should be run on this project
    via the `run_on_projects` attributes."""
    def filter(task, params):
        return standard_filter(task, params)

    return [l for l, task in full_task_graph.tasks.iteritems() if filter(task, parameters)]
