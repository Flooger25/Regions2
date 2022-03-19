echo OFF

if "%1%" == "" (
    java -classpath "C:\Users\Flooger\Documents\D&D\Regions2" Panel
) else (
    if "%2%" == "test" (
        java -classpath "C:\Users\Flooger\Documents\D&D\Regions2;C:\Program Files\Java\junit4.10\junit-4.10.jar" %1 "test"
    ) else (
        java -classpath "C:\Users\Flooger\Documents\D&D\Regions2" %1
    )
)
