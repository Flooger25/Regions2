echo OFF

if "%1%" == "test" (
    javac src/*.java test/*.java -d bin/
) else (
    javac src/*.java -d bin/
)
