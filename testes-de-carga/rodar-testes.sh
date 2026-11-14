#!/bin/bash
# Mesmo que o rodar-testes.bat, para Linux ou Mac (foi o usado na medicao 2, na nuvem).
# Uso: ./rodar-testes.sh 2
cd "$(dirname "$0")"
MEDICAO=${1:-1}
PASTA=resultados/medicao-$MEDICAO
mkdir -p $PASTA/bruto
for s in login eventos presenca feed; do
  echo "===== Medicao $MEDICAO, servico: $s ====="
  k6 run --out csv=$PASTA/bruto/$s.csv --summary-export $PASTA/$s.json $s.js
done
python3 graficos.py $MEDICAO
