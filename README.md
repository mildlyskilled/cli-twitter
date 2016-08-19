A Console Based Twitter Client
==============================

This is based on the documentation from the Akka official site, with a 
couple of mixed in sources of inspiration from 
[Here](https://github.com/AL333Z/akka-stream-twitter) I basically used the 
model from there. and the publisher/subscriber implementation. 

I have included some commandline argument stuff that allows you to filter 
the twitter stream on hash tags (there are a couple of holes on detecting 
hash tags that I will look into later)

You can also cleanly shut down the stream by typing *exit* at any point.

It also comes with SBT assembly so you can build it out into a fat jar 
and run it like that

The easiest way to do this though is to simply run `sbt run`