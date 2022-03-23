echo OFF

if "%1%" == "" (
    java -classpath bin Panel
) else (
    if "%2%" == "test" (
        java -classpath "bin;C:\Program Files\Java\junit4.10\junit-4.10.jar" %1 "test"
    ) else (
        java -classpath bin %1
    )
)
