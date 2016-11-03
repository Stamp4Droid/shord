mkdir -p results/
java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$2 -Dchord.model.path=$1 shord.jar
mv ../pjbench/dacapo/benchmarks/chord_output/log.txt results/log_$2_$3.txt