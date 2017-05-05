The directory structure under: {suis-msghandler}src/test/resources/complete/ is used for a complete test. 

Files will be placed in the MH outbox and processed until they'll be in the Sedex outbox.

There exists two Unit Tests which will work under this directory: 

CompleteSimpleTest.java:
------------------------
Just works with one file and one MH outbox directory.

CompleteFullTest.java:
----------------------
This is a complex test with multiple MH outboxes and with multiple MH signing outboxes. Inclusive multiple files which also will generate filename conflicts. 


At running the tests, it's also important to check the log file ./log/suis-msg-handler-app.log for follwing entries: FATAL, ERROR, WARN. These kind of message shouldn't appear during the unit tests. If one of them will appear you need to search the bug. The application (Unit Test) will not scan this log file. There's only one ERROR message which is allowed:
<<...[DbLogService:187] [ERROR] Table not found in statement...>>>

CompleteMultiReceiverTest.java:
-------------------------------
Testet Nachrichten mit mehreren Empfängern.

