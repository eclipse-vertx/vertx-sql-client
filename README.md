# Vertx shell

## Documentation

- Java : http://vertx.io/docs/vertx-shell/java/
- Groovy :http://vertx.io/docs/vertx-shell/groovy/
- Ruby : http://vertx.io/docs/vertx-shell/ruby/
- JavaScript : http://vertx.io/docs/vertx-shell/js/

## Informal roadmap / todo

- todo in termd
   - switch to a demand scheme for stream
   - decouple readline from TtyConnection (the callbacks)
- event bus connector : execute shell commands using event bus
- composite commands _bus send_ instead of _bus-send_
- http client command
- make telnet configurable with a remote hosts white list
- make builtin shell commands completable
- fg/bg with id : fg 3, bg 4
- stream redirection : echo abc >toto.txt
- pipe command : a | b
- process management
- REPL ?
- more OOTB commands
- stream more than just text : any T should be streamable (in particular json)
- advanced option configuration (beyond host/port)
