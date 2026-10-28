#!/usr/bin/env bash
# Resume los resultados JUnit (XML) de cada suite: pruebas ejecutadas y fallidas.
# Funciona en cualquier rama o commit, aunque este archivo no exista ahí:
#   git show main:scripts/resumen-pruebas.sh | bash
for s in test integrationTest acceptanceTest e2eTest; do
  cat build/test-results/$s/TEST-*.xml 2>/dev/null |
    grep -o '<testsuite [^>]*' |
    awk -v s=$s '{ match($0, /tests="[0-9]+"/);    t += substr($0, RSTART + 7, RLENGTH - 8)
                   match($0, /failures="[0-9]+"/); f += substr($0, RSTART + 10, RLENGTH - 11)
                   match($0, /errors="[0-9]+"/);   e += substr($0, RSTART + 8, RLENGTH - 9) }
                 END { if (t) printf "%-15s %3d pruebas, %3d fallidas\n", s, t, f + e
                       else   printf "%-15s sin resultados\n", s }'
done
