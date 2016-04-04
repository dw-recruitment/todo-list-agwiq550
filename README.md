# anon-todo-2

A stylish todo list.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

Create the db:

    createdb anon-todo

note: for testing, it is essential the db be named anon-todo
This can be changed in the src/anon_todo/model.clj file.
Also, the user should have access to the db to be able to create new tables.

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2016 anon-todo
