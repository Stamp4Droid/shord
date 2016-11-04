echo java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$1 -Dchord.model.path=$2 shord.jar
java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$1 -Dchord.model.path=$2 shord.jar
echo mkdir -p results
mkdir -p results
echo mv ../pjbench/dacapo/benchmarks/$1/chord_output/log.txt results/log_$1_$3.txt
mv ../pjbench/dacapo/benchmarks/$1/chord_output/log.txt results/log_$1_$3.txt