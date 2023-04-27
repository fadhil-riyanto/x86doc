@echo off
set PATH=%PATH%;C:\java\jdk1.8\bin
del index.html signature.txt
javac -encoding UTF-8 Main.java
java Main
hhc ..\html\main.hhp