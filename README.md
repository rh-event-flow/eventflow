# cloudevent-flow
Simple dataflow for CloudEvents

## Manager/Watcher Installation

Todo

## Operations Installation

Todo

## Apache Qpid Dispatch Router Installation

To install a single Dispatch Router run the `build.sh` from the `dispatch/router` directory.

To install a mesh of routers (two) run the `build.sh` from the `dispatch/routerA` and `dispatch\routerB` directories.

Then add an image to your project.
Add To Project ->  Deploy Image. 
Namespace: myproject, ImageStream: dispatch, tag: latest. 
This will create a Dispatch Router with service name `dispatch.myproject.svc` which can be used with the endpoint `amqp://dispatch.myproject.svc:5672`.
This endpoint is set as default in the `API#getGlobalProperties()` method.

If you have deployed multiple routers follow the same procedure but the services will be `dispatch-a.myproject.svc` and `dispatch-b.myproject.svc`.