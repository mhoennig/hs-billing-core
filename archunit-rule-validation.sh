#!/bin/sh
./gradlew test --info 2>/dev/null |
    sed \
        -e '/^[ \t]*$/d' \
        -e '/^Gradle Test Executor/d' \
        -e '/^> Task :app:test FAILED/d' \
        -e '/^ArchitectureTest /,/^        at /!d' \
    >archunit-rule-validation.txt

git diff --unified=0 archunit-rule-validation.txt >build/archunit-rule-validation.diff

    echo
if [ -s build/archunit-rule-validation.diff ]; then
    echo "========= the following ArchUnit rules issued different results: ========"
    cat build/archunit-rule-validation.diff
    echo "========================================================================="
    echo
    echo "If these changes are deliberate, commit the new version of 'archunit-rule-validation.txt'!"
else
    echo "All ArchUnit rules have still issued the same results."
fi
