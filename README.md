Toy Robot Kotlin Edition
=============================

This is an attempt at [Toy Robot](https://joneaves.wordpress.com/2014/07/21/toy-robot-coding-test/) using [Kotlin](http://kotlin)

I wanted to solve it with some Kotlin features

- Provide the implementation in a single file in the `root` package.  The rough idea was to see how you could start an 
   implementation in a single file and see if it would be still readable as it went.  Can you have clean code in a 
   single file of 200 lines long?
   The other aspect was to ensure that functions & classes stayed small and compartmentalised.
- Focus on a command loop that took only a InputStream, parser/tokenizer lambda(s) to delegate to command execution
- Try out sealed classes for the Command Hierarchy. Use reflection and Kotlin classes
- Use Extension Functions on base JDK classes like String to add a method to convert a string to a Direction enum
- Use the `check(Boolean, msgSupplier)` assertions to make code readable
- Used ranges to test a co-ordinate fit within the table range
- companion objects for loggers, and also for a command factory

In this instance, I've seen ToyRobot so many times, I didn't worry too much about unit tests.  The command loop / parser-function
idea came out of wanting to this via TDD initially, but I left this project for many weeks, and then just wanted to see
what I could acheive on a lazy Sunday afternoon.

### Experimental Thoughts
Things I thought I'd try differently just to see how they fared
- Start with CommandLoop (command line evaluation) instead of Tabletop/Robot method implementation.  This worked
   slightly better in that I could see a more TDD like evolution of the classes and collaborators.
- Used `java.awt.Point` for the Robots location as its already got a transform method. The only annoyance its not
  immutable.  
  _Future_: More extension methods to create an ImmutablePoint and convenience transform method
- Also used j.u.l out of the box
- Robot and RobotPosition are two different fields under the tabletop.
- Robot only knows its facing somewhere.  The tabletop knows where to position the robot, and the robot knows
   how to turn left and right - tabletop delegates its left and right commands through to the robots left and 
   right.  _Future_: consider the applicability of the command loop having a direct hold on this
- Robot's facing field is mutable.  _Could this be improved?_
